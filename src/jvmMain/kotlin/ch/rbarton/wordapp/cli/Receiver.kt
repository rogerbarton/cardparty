package ch.rbarton.wordapp.cli

import ch.rbarton.wordapp.cli.receive.onBaseRequestReceived
import ch.rbarton.wordapp.common.connection.responseHandlerQueue
import ch.rbarton.wordapp.common.request.BaseRequest
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

suspend fun DefaultClientWebSocketSession.receiveFrames()
{
    try
    {
        for (frame in incoming)
        {
            when (frame)
            {
                is Frame.Text -> onFrameReceived(frame.readText())
                is Frame.Close ->
                {
                    println("Disconnected: ${frame.readReason()}")
                    return
                }
            }
        }
    }
    catch (e: CancellationException)
    {
    }
    catch (e: Exception)
    {
        println("Error while receiving: ${e.localizedMessage}")
    }
}

private fun onFrameReceived(rawText: String)
{
//    println(rawText)

    try
    {
        // Handle incoming messages differently depending on if they have a matching requestId
        val json = Json.decodeFromString<BaseRequest>(rawText)
        if (json.requestId == null)
            onBaseRequestReceived(json, rawText)
        else
            if (responseHandlerQueue.contains(json.requestId))
                responseHandlerQueue[json.requestId!!]!!.invoke(json)
            else
                println(
                    "<~[server] InvalidRequestId: BaseRequest.requestId = ${json.requestId} " +
                            "has no corresponding responseHandler in responseHandlerQueue.\n"
                )
    }
    catch (e: SerializationException)
    {
        println("<~[server] SerializationException: ${e.localizedMessage}\n")
    }
}