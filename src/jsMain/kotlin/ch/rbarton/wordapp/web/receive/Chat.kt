package ch.rbarton.wordapp.web.receive

import ch.rbarton.wordapp.common.request.Chat
import ch.rbarton.wordapp.web.App
import react.setState

fun App.onRequestReceived(response: Chat.MessageBroadcast)
{
    if (state.party == null) return

    val log = "[${response.userId}:${state.party!!.users[response.userId]?.name}] ${response.message}"
    println(log)

    setState {
        chatHistory.add(log)
    }
}