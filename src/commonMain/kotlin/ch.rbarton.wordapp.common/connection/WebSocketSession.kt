package ch.rbarton.wordapp.common.connection

import ch.rbarton.wordapp.common.request.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Helper extension methods
 */

/**
 *  Allow us to send a payload directly.
 *  Wraps it in a Json
 */
suspend fun WebSocketSession.send(payload: BaseRequest) =
    send(Json.encodeToString(payload))

/**
 * Use this for RPCs without data, signal only.
 */
suspend fun WebSocketSession.send(actionType: ActionType) =
    send(ActionRequest(actionType))

suspend fun WebSocketSession.send(status: StatusCode) =
    send(StatusResponse(status))

val responseHandlerQueue: MutableMap<Int, (BaseRequest) -> Unit> = mutableMapOf()

suspend fun WebSocketSession.send(payload: BaseRequest, onResponse: (BaseRequest) -> Unit)
{
    payload.requestId = genRequestId()
    responseHandlerQueue[payload.requestId!!] = onResponse
    send(payload)
}

suspend fun WebSocketSession.send(actionType: ActionType, onResponse: (BaseRequest) -> Unit) =
    send(ActionRequest(actionType), onResponse)

fun WebSocketSession.launchSend(payload: BaseRequest) = launch { send(payload) }
fun WebSocketSession.launchSend(actionType: ActionType) = launch { send(actionType) }
fun WebSocketSession.launchSend(status: StatusCode) = launch { send(status) }
fun WebSocketSession.launchSend(payload: BaseRequest, onResponse: (BaseRequest) -> Unit) =
    launch { send(payload, onResponse) }

fun WebSocketSession.launchSend(actionType: ActionType, onResponse: (BaseRequest) -> Unit) =
    launch { send(actionType, onResponse) }