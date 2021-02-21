package common

import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.launch
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.coroutines.CoroutineContext

/**
 * Helper extension methods
 */

/**
 *  Allow us to send a payload directly.
 *  Wraps it in a [WrapperJson]
 */
suspend fun WebSocketSession.send(payload: BaseJson) =
    send(Json.encodeToString(payload))

/**
 * Use this for RPCs without data, signal only.
 */
suspend fun WebSocketSession.send(actionType: ActionType) =
    send(ActionJson(actionType))

suspend fun WebSocketSession.send(status: StatusCode) =
    send(StatusJson(status))

val responseHandlerQueue: MutableMap<Int, (BaseJson) -> Unit> = mutableMapOf()

suspend fun WebSocketSession.send(payload: BaseJson, onResponse: (BaseJson) -> Unit)
{
    payload.requestId = genRequestId()
    responseHandlerQueue[payload.requestId!!] = onResponse
    send(payload)
}

suspend fun WebSocketSession.send(actionType: ActionType, onResponse: (BaseJson) -> Unit) =
    send(ActionJson(actionType), onResponse)
