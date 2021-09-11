package ch.rbarton.wordapp.cli.receive

import ch.rbarton.wordapp.cli.connection
import ch.rbarton.wordapp.cli.party
import ch.rbarton.wordapp.common.request.*

/**
 * Called when an unidentified request was received.
 */
fun onBaseRequestReceived(response: BaseRequest, rawText: String)
{
    when (response)
    {
        is InitResponse ->
        {
            connection.userId = response.userId
            println("Hi, there are ${response.userCount} people and ${response.parties.size} parties here.")
            var i = 1
            response.parties.forEach {
                println("\t${i++.toString().padStart(2)}. ${it.key} (${it.value})")
            }
        }
        is StatusResponse -> println("${response.status.name}${if (response.message != null) ": ${response.message}" else ""}")
        is ActionRequest -> println(response.action.name)
        is Chat.MessageBroadcast ->
        {
            if (party != null)
                println("[${response.userId}:${party!!.users[response.userId]?.name}]: ${response.message}")
        }

        is UserInfo.SetNameBroadcast -> onRequestReceived(response)
        is UserInfo.SetColorBroadcast -> onRequestReceived(response)

        is Party.JoinBroadcast -> onRequestReceived(response)
        is Party.LeaveBroadcast -> onRequestReceived(response)

        is PartyOptions.SetPartyModeBroadcast -> onRequestReceived(response)

        is WordGame.SetGameStageRequest -> onRequestReceived(response)
        is WordGame.SetGameSettingsRequest -> onRequestReceived(response)
        is WordGame.AddCardBroadcast -> onRequestReceived(response)
        is WordGame.RemoveCardRequest -> onRequestReceived(response)
        is WordGame.AddCategoryBroadcast -> onRequestReceived(response)
        is WordGame.RemoveCategoryRequest -> onRequestReceived(response)
        is WordGame.AssignWordsScatter -> onRequestReceived(response)

        else -> println("-> Unhandled Message: $rawText")
    }
}