package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.request.ChatBroadcastJson
import ch.rbarton.wordapp.common.request.ChatJson
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.broadcast
import ch.rbarton.wordapp.server.send


/**
 * Chat within the party or with all not in a party
 */
suspend fun Connection.onRequestReceived(data: ChatJson)
{
    if (partyCode == null)
    {
        send(StatusCode.NotInAParty)
        return
    }

    party!!.connections.broadcast(this, ChatBroadcastJson(guid, data.message))
    send(StatusCode.Success)
}