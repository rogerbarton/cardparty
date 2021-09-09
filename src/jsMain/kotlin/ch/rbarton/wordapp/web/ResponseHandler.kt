package ch.rbarton.wordapp.web

import ch.rbarton.wordapp.common.request.*
import react.setState

fun App.handleUnidentifiedResponse(response: BaseRequest)
{
    when (response)
    {
        is InitResponse ->
        {
            setState {
                connection.userId = response.userId
                globalUserCount = response.userCount - 1
                globalPartyCount = response.parties.size
            }
            println("Hi, there are ${response.userCount - 1} people and ${response.parties.size} parties here.")
            var i = 1
            response.parties.forEach {
                println("\t${i++.toString().padStart(2)}. ${it.key} (${it.value})")
            }
        }
        is StatusResponse -> println("${response.status.name}${if (response.message != null) ": ${response.message}" else ""}")
        is ActionRequest -> println(response.action.name)
        is UserInfo.SetNameBroadcast ->
        {
            if (state.party == null) return
            val log =
                "[${response.userId}:${state.party!!.users[response.userId]?.name}] Changed name to ${response.name}"
            println(log)
            setState {
                party!!.users[response.userId]?.name = response.name
                chatHistory.add(log)
            }
        }
        is Party.JoinBroadcast ->
        {
            if (state.party == null) return
            val log = "[${response.userId}:${response.userInfo.name}] Joined party"
            setState {
                party!!.users[response.userId] = response.userInfo
                chatHistory.add(log)
            }
            println(log)
        }
        is Party.LeaveBroadcast ->
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
        is Chat.MessageBroadcast ->
        {
            if (state.party == null) return
            val message = "[${response.userId}:${state.party!!.users[response.userId]?.name}] ${response.message}"
            setState {
                chatHistory.add(message)
            }
            println(message)
        }
        is WordGame.SetGameSettingsRequest ->
        {
            if (state.party == null || state.party!!.stateShared == null) return
            setState {
                party!!.stateShared!!.settings = response.settings
            }
        }
        else -> println("-> $response")
    }
}