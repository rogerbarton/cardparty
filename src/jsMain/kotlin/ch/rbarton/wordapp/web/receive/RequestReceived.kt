package ch.rbarton.wordapp.web.receive

import ch.rbarton.wordapp.common.request.*
import ch.rbarton.wordapp.web.App
import react.setState

fun App.onBaseRequestReceived(response: BaseRequest)
{
    when (response)
    {
        is InitResponse ->
        {
            println("Hi, there are ${response.userCount - 1} people and ${response.parties.size} parties here.")
            var i = 1
            response.parties.forEach {
                println("\t${i++.toString().padStart(2)}. ${it.key} (${it.value})")
            }

            setState {
                connection.userId = response.userId
                globalUserCount = response.userCount - 1
                globalPartyCount = response.parties.size
            }
        }
        is StatusResponse -> println("${response.status.name}${if (response.message != null) ": ${response.message}" else ""}")
        is ActionRequest -> println(response.action.name)

        is UserInfo.SetNameBroadcast -> onRequestReceived(response)
        is UserInfo.SetColorBroadcast -> onRequestReceived(response)

        is Party.JoinBroadcast -> onRequestReceived(response)
        is Party.LeaveBroadcast -> onRequestReceived(response)
        is PartyOptions.SetPartyModeBroadcast -> onRequestReceived(response)
        is Chat.MessageBroadcast -> onRequestReceived(response)

        is WordGame.SetGameSettingsRequest -> onRequestReceived(response)
        is WordGame.SetGameStageRequest -> onRequestReceived(response)
        is WordGame.AddCardBroadcast -> onRequestReceived(response)
        is WordGame.RemoveCardRequest -> onRequestReceived(response)
        is WordGame.AddCategoryBroadcast -> onRequestReceived(response)
        is WordGame.RemoveCategoryRequest -> onRequestReceived(response)
        is WordGame.AssignWordsScatter -> onRequestReceived(response)
        else -> println("-> $response")
    }
}