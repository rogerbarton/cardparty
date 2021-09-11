package ch.rbarton.wordapp.web.receive

import ch.rbarton.wordapp.common.request.UserInfo
import ch.rbarton.wordapp.web.App
import ch.rbarton.wordapp.web.components.add
import react.setState

fun App.onRequestReceived(response: UserInfo.SetNameBroadcast)
{
    if (state.party == null || !state.party!!.users.contains(response.userId)) return

    val log = "[${response.userId}:${state.party!!.users[response.userId]?.name}] " +
            "Changed name to ${response.name}"
    println(log)

    setState {
        party!!.users[response.userId]?.name = response.name
        chatHistory.add(log)
    }
}

fun App.onRequestReceived(response: UserInfo.SetColorBroadcast)
{
    if (state.party == null || !state.party!!.users.contains(response.userId)) return

    val log = "[${response.userId}:${state.party!!.users[response.userId]?.name}] " +
            "Changed color to ${response.colorId}"
    println(log)

    setState {
        party!!.users[response.userId]!!.colorId = response.colorId
        chatHistory.add(log)
    }
}