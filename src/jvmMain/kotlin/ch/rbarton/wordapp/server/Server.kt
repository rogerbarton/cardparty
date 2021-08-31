package ch.rbarton.wordapp.server

import ch.rbarton.wordapp.common.InitJson
import ch.rbarton.wordapp.common.StatusCode
import ch.rbarton.wordapp.common.send

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.collections.*

val allConnections: MutableSet<Connection> = Collections.synchronizedSet<Connection?>(LinkedHashSet())
val parties: MutableMap<String, Party> = Collections.synchronizedMap<String, Party>(LinkedHashMap())

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module()
{
    embeddedServer(Netty, host = "127.0.0.1", port = 8080) {
        install(WebSockets)
        install(Compression) {
            gzip()
        }

        routing {
            get("/") {
                call.respondText(
                    this::class.java.classLoader.getResource("index.html")!!.readText(),
                    ContentType.Text.Html
                )
            }
            static("/") {
                resources()
            }

            webSocket("/") {
                val thisConnection = Connection(this)
                println("Adding user ${thisConnection.guid}.")
                allConnections += thisConnection
                try
                {
                    send(
                        InitJson(
                            thisConnection.guid,
                            allConnections.size,
                            parties.mapValues { it.value.connections.size })
                    )
                    for (frame in incoming)
                    {
                        frame as? Frame.Text ?: continue
                        onFrameReceived(frame.readText(), thisConnection)
                    }
                }
                catch (e: Exception)
                {
                    println("Connection fatal error: ${e.localizedMessage}")
                }
                finally
                {
                    println("Removing ${thisConnection.guid}:${thisConnection.name}")
                    allConnections -= thisConnection
                }
            }
        }
    }.start(wait = true)
}


private suspend fun onFrameReceived(rawText: String, thisConnection: Connection)
{
    println("<-[${thisConnection.guid}:${thisConnection.name}] $rawText")

    try
    {
        thisConnection.onJsonReceived(Json.decodeFromString(rawText))
    }
    catch (e: SerializationException)
    {
        println("<~[${thisConnection.guid}:${thisConnection.name}] SerializationException: ${e.localizedMessage}\n")
        thisConnection.send(StatusCode.InvalidRequest)
    }
    catch (e: Exception)
    {
        thisConnection.send(StatusCode.ServerError)
    }
}
