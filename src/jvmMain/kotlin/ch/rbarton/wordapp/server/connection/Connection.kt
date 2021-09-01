package ch.rbarton.wordapp.server.connection

import ch.rbarton.wordapp.common.request.*
import ch.rbarton.wordapp.server.Party
import ch.rbarton.wordapp.server.onRequestReceived
import ch.rbarton.wordapp.server.parties
import io.ktor.http.cio.websocket.*
import java.util.concurrent.atomic.AtomicInteger

class Connection(val session: DefaultWebSocketSession)
{
    val guid = lastId.getAndIncrement()
    var name = "user${guid}"
    var partyCode: String? = null

    val party: Party? get() = parties[partyCode]

    // Current id of request being processed, assumes that only one is handled at a time
    var currentRequestId: Int? = null

    companion object
    {
        var lastId = AtomicInteger(0)
    }
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

suspend fun Connection.requireParty(): Boolean
{
    if(party == null)
    {
        send(StatusCode.InvalidPartyCode)
        return true
    }
    return false
}

suspend fun Connection.requireHost(): Boolean
{
    if(this != party!!.host)
    {
        send(StatusCode.NotHost)
        return true
    }
    return false
}
