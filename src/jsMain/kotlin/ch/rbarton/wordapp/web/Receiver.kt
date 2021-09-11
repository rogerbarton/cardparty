package ch.rbarton.wordapp.web

import ch.rbarton.wordapp.common.connection.responseHandlerQueue
import ch.rbarton.wordapp.common.request.*
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
            frame as? Frame.Text ?: continue
            onFrameReceived(frame.readText())
        }
    }
    catch (e: CancellationException)
    {
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
            handleUnidentifiedResponse(request)
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
                handleUnidentifiedResponse(request)
            }
        }
    }
    catch (e: SerializationException)
    {
        println("<~[server] SerializationException: ${e.message}\n")
    }
}

