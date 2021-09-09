package ch.rbarton.wordapp.server

import ch.rbarton.wordapp.common.data.UserInfo
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.server.data.Party
import io.ktor.http.cio.websocket.*
import java.util.concurrent.atomic.AtomicInteger

class Connection(val session: DefaultWebSocketSession)
{
    val userId = lastId.getAndIncrement()
    var userInfo: UserInfo = UserInfo("user${userId}")

    var partyCode: String? = null
    val party: Party? get() = parties[partyCode]

    // Current id of request being processed, assumes that only one is handled at a time
    var currentRequestId: Int? = null

    companion object
    {
        var lastId = AtomicInteger(0)
        var lastCategoryId = AtomicInteger(0)
        var lastCardId = AtomicInteger(0)
    }
}

suspend fun Connection.requireParty(): Boolean
{
    if(party == null)
    {
        send(StatusCode.NotInAParty)
        return true
    }
    return false
}

suspend fun Connection.requireHost(): Boolean
{
    if (this != party!!.host)
    {
        send(StatusCode.NotHost)
        return true
    }
    return false
}

suspend fun Connection.requireGameState(): Boolean
{
    if (party!!.stateShared == null)
    {
        send(StatusCode.NotInAGame)
        return true
    }
    return false
}
