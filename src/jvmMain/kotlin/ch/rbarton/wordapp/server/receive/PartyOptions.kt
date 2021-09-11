package ch.rbarton.wordapp.server.receive

import ch.rbarton.wordapp.common.data.GameStateShared
import ch.rbarton.wordapp.common.data.PartyMode
import ch.rbarton.wordapp.common.request.PartyOptions
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.requireHost
import ch.rbarton.wordapp.server.requireParty
import ch.rbarton.wordapp.server.send

suspend fun Connection.onRequestReceived(request: PartyOptions.SetPartyModeRequest)
{
    if (requireParty() || requireHost()) return

    if (party!!.mode == request.mode)
    {
        send(StatusCode.AlreadySet)
        return
    }

    party!!.mode = request.mode
    when (party!!.mode)
    {
        PartyMode.WordGame -> party!!.stateShared = GameStateShared()
        else -> party!!.stateShared = null
    }

    party!!.broadcast(this, PartyOptions.SetPartyModeBroadcast(party!!.mode, party!!.stateShared))
    send(StatusCode.Success)
}