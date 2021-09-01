package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.request.PartyOptions
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.requireParty
import ch.rbarton.wordapp.server.send

suspend fun Connection.onRequestReceived(json: PartyOptions.SetPartyModeRequest)
{
    if (requireParty()) return

    if (party!!.options.gameMode == json.mode)
    {
        send(StatusCode.AlreadySet)
        return
    }

    party!!.options.gameMode = json.mode
    party!!.broadcast(this, PartyOptions.SetPartyModeRequest(party!!.options.gameMode))
    send(StatusCode.Success)
}