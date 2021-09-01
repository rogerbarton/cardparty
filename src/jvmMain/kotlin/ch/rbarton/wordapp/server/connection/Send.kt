package ch.rbarton.wordapp.server.connection

import ch.rbarton.wordapp.common.request.*

import io.ktor.http.cio.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Contains helper functions to send and broadcast
 */

/**
 *  Allow us to send a payload directly. Sets correct requestId
 */
suspend fun Connection.send(payload: BaseJson, debugOutput: String? = null)
{
    payload.requestId = currentRequestId
    val payloadText: String = Json.encodeToString(payload)
    session.send(payloadText)
    println("->[${guid}:${name}] ${debugOutput ?: payloadText}")
}

/**
 * Send an empty payload. Use this for RPCs without data.
 */
suspend fun Connection.send(actionType: ActionType) = send(ActionJson(actionType) as BaseJson, actionType.name)

/**
 * Helper for sending a status code
 */
suspend fun Connection.send(status: StatusCode) = send(StatusJson(status) as BaseJson, status.name)

/**
 * Broadcast message to all other members, except the origin connection
 */
suspend fun Collection<Connection>.broadcast(origin: Connection, payload: BaseJson)
{
    val payloadText = Json.encodeToString(payload)
    forEach {
        if (it != origin)
            it.session.send(payloadText)
    }

    println(">-[${origin.guid}:${origin.name}] $payloadText")
}

suspend fun Collection<Connection>.broadcast(origin: Connection?, actionType: ActionType)
{
    val payloadText = Json.encodeToString(ActionJson(actionType) as BaseJson)
    forEach {
        if (it != origin)
            it.session.send(payloadText)
    }

    println(">-[${origin?.guid}:${origin?.name ?: "-"}] $actionType")
}