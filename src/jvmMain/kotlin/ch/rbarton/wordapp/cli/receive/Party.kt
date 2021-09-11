package ch.rbarton.wordapp.cli.receive

import ch.rbarton.wordapp.cli.party
import ch.rbarton.wordapp.common.request.Party


fun onRequestReceived(response: Party.LeaveBroadcast)
{
    if (party == null) return

    println(
        "[${response.userId}:${party!!.users[response.userId]?.name}] Left party" +
                if (response.newHost != null) ", ${party!!.users[response.newHost]} is now host" else ""
    )

    party!!.users.remove(response.userId)
    if (response.newHost != null)
        party!!.hostId = response.newHost
}

fun onRequestReceived(response: Party.JoinBroadcast)
{
    if (party == null) return

    party!!.users[response.userId] = response.userInfo
    println("[${response.userId}:${response.userInfo.name}] Joined party")
}