package ch.rbarton.wordapp.web

import ch.rbarton.wordapp.common.connection.responseHandlerQueue
import ch.rbarton.wordapp.common.request.BaseRequest
import ch.rbarton.wordapp.web.receive.onBaseRequestReceived
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Handles messages as they arrive
 */
suspend fun App.receiveWebsocketFrames()
{
    try
    {
        for (frame in state.ws!!.incoming)
        {
            when (frame)
            {
                is Frame.Text -> onFrameReceived(frame.readText())
                is Frame.Close ->
                {
                    // TODO: set state
                    println("Server Disconnected: ${frame.readReason()}")
                    return
                }
            }
        }
    }
    catch (e: CancellationException)
    {
        println("receiveWebsocketFrames cancelled")
    }
    catch (e: Exception)
    {
        println("Fatal Websocket Error: [${e::class.simpleName}] ${e.message}")
    }
}

/**
 * Deserializes frame and passes the result on.
 * If requests have a matching id, they are handled by the function saved in the responseHandlerQueue.
 */
private fun App.onFrameReceived(rawText: String)
{
//    println(rawText)

    try
    {
        val request = Json.decodeFromString<BaseRequest>(rawText)
        if (request.requestId == null)
        {
            onBaseRequestReceived(request)
        }
        else
        {
            if (responseHandlerQueue.contains(request.requestId))
                responseHandlerQueue[request.requestId!!]!!.invoke(request)
            else
            {
                println(
                    "<~[server] InvalidRequestId: BaseRequest.requestId = ${request.requestId} " +
                            "has no corresponding responseHandler in responseHandlerQueue.\n"
                )
                onBaseRequestReceived(request)
            }
        }
    }
    catch (e: SerializationException)
    {
        println("<~[server] SerializationException: ${e.message}\n")
    }
}

