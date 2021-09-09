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
                connection.guid = response.guid
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
                "[${response.userGuid}:${state.party!!.users[response.userGuid]}] Changed name to ${response.name}"
            println(log)
            setState {
                party!!.users[response.userGuid] = response.name
                chatHistory.add(log)
            }
        }
        is Party.JoinBroadcast ->
        {
            if (state.party == null) return
            val log = "[${response.userGuid}:${response.name}] Joined party"
            setState {
                party!!.users[response.userGuid] = response.name
                chatHistory.add(log)
            }
            println(log)
        }
        is Party.LeaveBroadcast ->
        {
            if (state.party == null) return
            val log = "[${response.userGuid}:${state.party!!.users[response.userGuid]}] Left party" +
                    if (response.newHost != null) ", ${state.party!!.users[response.newHost]} is now host" else ""
            println(log)
            setState {
                party!!.users.remove(response.userGuid)
                if (response.newHost != null)
                    party!!.hostGuid = response.newHost
                chatHistory.add(log)
            }
        }
        is Chat.MessageBroadcast ->
        {
            if (state.party == null) return
            val message = "[${response.userGuid}:${state.party!!.users[response.userGuid]}] ${response.message}"
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