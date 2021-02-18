package common

import io.ktor.http.cio.websocket.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

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
    send(Json.encodeToString(ActionJson(actionType) as BaseJson))