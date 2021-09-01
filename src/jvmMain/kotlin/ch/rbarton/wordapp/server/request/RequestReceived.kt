package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.request.*
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.send

/**
 * Handles the received request and sends a response
 * Multiplexer of different json types
 */
suspend fun Connection.onJsonReceived(json: BaseJson)
{
    currentRequestId = json.requestId
    when (json)
    {
        is ActionJson -> onRequestReceived(json)
        is SetNameJson -> onRequestReceived(json)
        is JoinPartyJson -> onRequestReceived(json)
        is ChatJson -> onRequestReceived(json)

        is SetGameSettingsJson -> onRequestReceived(json)
        is AddWordJson -> onRequestReceived(json)
        else -> send(StatusCode.InvalidRequestType)
    }
    currentRequestId = null
}

/**
 * Multiplexer of different actions without a backing json data class
 */
suspend fun Connection.onRequestReceived(json: ActionJson)
{
    when (json.action)
    {
        ActionType.CreateParty -> createParty()
        ActionType.LeaveParty -> leaveParty()
    }
}
