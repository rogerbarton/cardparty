package ch.rbarton.wordapp.cli

import ch.rbarton.wordapp.common.client.data.ConnectionData
import ch.rbarton.wordapp.common.client.data.Party
import ch.rbarton.wordapp.common.data.UserInfo
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.ConnectException

var connection = ConnectionData()
var userInfo = UserInfo()
var party: Party? = null

fun main()
{
    print("Enter name:")
    var input = readLine()
    while (input == null)
        input = readLine()
    userInfo.name = input

    while (true)
    {
        try
        {
            val client = HttpClient {
                install(WebSockets)
            }
            runBlocking {
                client.webSocket(
                    method = HttpMethod.Get,
                    host = connection.serverAddress,
                    port = connection.serverPort,
                    path = "/"
                ) {
                    val messageOutputRoutine = launch { receiveFrames() }
                    val userInputRoutine = launch { inputMessages(messageOutputRoutine) }

                    userInputRoutine.join() // wait for exit
                    messageOutputRoutine.cancelAndJoin()
                }
            }

            client.close()
            println("Connection closed. Press enter to reconnect.")
            readLine()
        }
        catch (e: ConnectException)
        {
            println("Cannot connect to ${connection.serverAddress}:${connection.serverPort}. Enter a new address or skip to try again:")
            print("Server IP address (e.g. 127.0.0.1:8080): ")
            val newAddr = readLine()
            if (newAddr != null && newAddr.isNotEmpty())
            {
                val splitAddr = newAddr.split(':')
                connection.serverAddress = splitAddr[0]
                if (splitAddr.size > 1 && splitAddr[1].toIntOrNull() != null)
                    connection.serverPort = splitAddr[1].toInt()
            }
        }
    }
}