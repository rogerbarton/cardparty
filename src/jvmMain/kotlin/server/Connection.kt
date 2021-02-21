package server

import common.*

import io.ktor.http.cio.websocket.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.util.concurrent.atomic.AtomicInteger

class Connection(val session: DefaultWebSocketSession)
{
    companion object
    {
        var lastId = AtomicInteger(0)
    }

    val guid = lastId.getAndIncrement()
    var name = "user${guid}"
    var partyCode: String? = null

    val party: Party? get() = parties[partyCode]

    // Current id of request being processed, assumes that only one is handled at a time
    var currentRequestId: Int? = null
}

/**
 * Handles the received request and sends a response
 * Multiplexer of different json types
 */
suspend fun Connection.onJsonReceived(json: BaseJson)
{
    currentRequestId = json.requestId
    when (json)
    {
        is ActionJson -> onRequestReceived(json)
        is SetNameJson -> onRequestReceived(json)
        is JoinPartyJson -> onRequestReceived(json)
        is ChatJson -> onRequestReceived(json)

        is SetGameSettingsJson -> onRequestReceived(json)
        is AddWordJson -> onRequestReceived(json)
        else -> send(StatusCode.InvalidRequestType)
    }
    currentRequestId = null
}

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
