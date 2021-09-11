package ch.rbarton.wordapp.server

import ch.rbarton.wordapp.common.connection.send
import ch.rbarton.wordapp.common.request.InitResponse
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.server.data.Party
import ch.rbarton.wordapp.server.receive.onBaseRequestReceived
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
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

val connections: MutableMap<Int, Connection> = Collections.synchronizedMap(LinkedHashMap())
val parties: MutableMap<String, Party> = Collections.synchronizedMap(LinkedHashMap())
val userInfos get() = connections.mapValues { it.value.userInfo }

fun main(args: Array<String>) = EngineMain.main(args)

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
                println("Adding user ${thisConnection.userId}.")
                connections[thisConnection.userId] = thisConnection
                try
                {
                    send(
                        InitResponse(
                            thisConnection.userId,
                            connections.size,
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
                    println("Removing ${thisConnection.userId}:${thisConnection.userInfo.name}")
                    launch { thisConnection.party?.remove(thisConnection) }
                    connections.remove(thisConnection.userId)
                }
            }
        }
    }.start(wait = true)
}


private suspend fun onFrameReceived(rawText: String, thisConnection: Connection)
{
    println("<-[${thisConnection.userId}:${thisConnection.userInfo.name}] $rawText")

    try
    {
        thisConnection.onBaseRequestReceived(Json.decodeFromString(rawText))
    }
    catch (e: SerializationException)
    {
        println("<~[${thisConnection.userId}:${thisConnection.userInfo.name}] SerializationException: ${e.localizedMessage}\n")
        thisConnection.send(StatusCode.InvalidRequest)
    }
    catch (e: Exception)
    {
        thisConnection.send(StatusCode.ServerError)
    }
}
