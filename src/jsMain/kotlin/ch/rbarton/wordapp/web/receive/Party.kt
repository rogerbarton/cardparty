package ch.rbarton.wordapp.web.receive

import ch.rbarton.wordapp.common.request.Party
import ch.rbarton.wordapp.web.App
import react.setState

fun App.onRequestReceived(response: Party.LeaveBroadcast)
{
    if (state.party == null) return

    val log = "[${response.userId}:${state.party!!.users[response.userId]?.name}] Left party" +
            if (response.newHost != null) ", ${state.party!!.users[response.newHost]?.name} is now host" else ""
    println(log)

    setState {
        party!!.users.remove(response.userId)
        if (response.newHost != null)
            party!!.hostId = response.newHost
        chatHistory.add(log)
    }
}

fun App.onRequestReceived(response: Party.JoinBroadcast)
{
    if (state.party == null) return

    val log = "[${response.userId}:${response.userInfo.name}] Joined party"
    println(log)

    setState {
        party!!.users[response.userId] = response.userInfo
        chatHistory.add(log)
    }
}