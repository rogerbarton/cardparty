package ch.rbarton.wordapp.server

import ch.rbarton.wordapp.common.request.StatusCode
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
