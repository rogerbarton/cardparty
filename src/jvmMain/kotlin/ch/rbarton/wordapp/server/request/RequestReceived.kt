package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.request.*
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.send

/**
 * Handles the received request and sends a response
 * Multiplexer of different json types
 */
suspend fun Connection.onJsonReceived(json: BaseRequest)
{
    currentRequestId = json.requestId
    when (json)
    {
        is ActionRequest -> onRequestReceived(json)
        is UserInfo.SetNameRequest -> onRequestReceived(json)
        is Party.JoinRequest -> onRequestReceived(json)
        is Chat.MessageRequest -> onRequestReceived(json)

        is WordGame.SetGameSettingsRequest -> onRequestReceived(json)
        is WordGame.AddWordRequest -> onRequestReceived(json)
        else -> send(StatusCode.InvalidRequestType)
    }
    currentRequestId = null
}

/**
 * Multiplexer of different actions without a backing json data class
 */
suspend fun Connection.onRequestReceived(json: ActionRequest)
{
    when (json.action)
    {
        ActionType.PartyCreate -> createParty()
        ActionType.PartyLeave -> leaveParty()
    }
}
