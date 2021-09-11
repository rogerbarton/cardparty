package ch.rbarton.wordapp.cli.receive

import ch.rbarton.wordapp.cli.party
import ch.rbarton.wordapp.common.request.UserInfo

fun onRequestReceived(response: UserInfo.SetColorBroadcast)
{
    if (party == null || !party!!.users.contains(response.userId)) return

    println("[${response.userId}:${party!!.users[response.userId]?.name}] Changed color to ${response.colorId}")
    party!!.users[response.userId]!!.colorId = response.colorId
}

fun onRequestReceived(response: UserInfo.SetNameBroadcast)
{
    if (party == null || !party!!.users.contains(response.userId)) return

    println("[${response.userId}:${party!!.users[response.userId]?.name}] Changed name to ${response.name}")
    party!!.users[response.userId]!!.name = response.name
}