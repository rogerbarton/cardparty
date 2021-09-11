package ch.rbarton.wordapp.server.receive

import ch.rbarton.wordapp.common.request.*
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.send

/**
 * Handles the received request and sends a response
 * Multiplexer of different json types
 */
suspend fun Connection.onBaseRequestReceived(request: BaseRequest)
{
    currentRequestId = request.requestId
    when (request)
    {
        is ActionRequest -> when (request.action)
        {
            ActionType.PartyCreate -> createParty()
            ActionType.PartyLeave -> leaveParty()
        }
        is UserInfo.SetNameRequest -> onRequestReceived(request)
        is UserInfo.SetColorRequest -> onRequestReceived(request)

        is Party.JoinRequest -> onRequestReceived(request)
        is PartyOptions.SetPartyModeRequest -> onRequestReceived(request)
        is Chat.MessageRequest -> onRequestReceived(request)

        is WordGame.SetGameSettingsRequest -> onRequestReceived(request)
        is WordGame.SetGameStageRequest -> onRequestReceived(request)
        is WordGame.AddCategoryRequest -> onRequestReceived(request)
        is WordGame.RemoveCategoryRequest -> onRequestReceived(request)
        is WordGame.AddCardRequest -> onRequestReceived(request)
        is WordGame.RemoveCardRequest -> onRequestReceived(request)

        else -> send(StatusCode.InvalidRequestType)
    }
    currentRequestId = null
}

