package ch.rbarton.wordapp.cli

import ch.rbarton.wordapp.common.data.PartyOptions
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.ConnectException

var serverAddress = "127.0.0.1"
var serverPort = 8080

var guid: Int? = null
var puid: Int? = null
var partyCode: String? = null
var partyOptions: PartyOptions? = null

var name: String = ""
var users: MutableMap<Int, String>? = null

fun main()
{
    print("Enter name:")
    var input = readLine()
    while (input == null)
        input = readLine()
    name = input

    while (true)
    {
        try
        {
            val client = HttpClient {
                install(WebSockets)
            }
            runBlocking {
                client.webSocket(method = HttpMethod.Get, host = serverAddress, port = serverPort, path = "/") {
                    val messageOutputRoutine = launch { outputMessages() }
                    val userInputRoutine = launch { inputMessages() }

                    userInputRoutine.join() // wait for exit
                    messageOutputRoutine.cancelAndJoin()
                }
            }

            client.close()
            println("Connection closed. Press enter to reconnect.")
            readLine()
        }
        catch (e: ConnectException){
            println("Cannot connect to $serverAddress. Enter a new address or skip to try again:")
            print("Server IP address (e.g. 127.0.0.1): ")
            val newAddr = readLine()
            if(newAddr != null && newAddr.isNotEmpty())
                serverAddress = newAddr
        }
    }
}