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
}

/**
 * Handles the received request and sends a response
 * Multiplexer of different json types
 */
suspend fun Connection.onJsonReceived(json: BaseJson)
{
    when (json)
    {
        is ActionJson -> onRequestReceived(json)
        is SetNameJson -> onRequestReceived(json)
        is JoinPartyJson -> onRequestReceived(json)
        is ChatJson -> onRequestReceived(json)

        is AddWordJson -> onRequestReceived(json)
        else -> send(StatusCode.InvalidRequestType)
    }
}

/**
 *  Allow us to send a payload directly.
 */
suspend fun Connection.send(payload: BaseJson)
{
    val payloadText: String = Json.encodeToString(payload)
    session.send(payloadText)
    println("->[${guid}:${name}] $payloadText")
}

/**
 * Send an empty payload. Use this for RPCs without data.
 */
suspend fun Connection.send(actionType: ActionType)
{
    val payloadText: String = Json.encodeToString(ActionJson(actionType) as BaseJson)
    session.send(payloadText)
    println("->[${guid}:${name}] $actionType")
}

/**
 * Helper for sending a status code
 */
suspend fun Connection.send(status: StatusCode)
{
    val payloadText: String = Json.encodeToString(StatusJson(status) as BaseJson)
    session.send(payloadText)
    println("->[${guid}:${name}] $status")
}
