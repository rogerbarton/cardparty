package ch.rbarton.wordapp.common.connection

import ch.rbarton.wordapp.common.request.*
import io.ktor.http.cio.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Helper extension methods
 */

/**
 *  Allow us to send a payload directly.
 *  Wraps it in a Json
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
