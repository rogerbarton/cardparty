package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.data.GameState
import ch.rbarton.wordapp.common.data.PartyMode
import ch.rbarton.wordapp.common.request.PartyOptions
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.requireHost
import ch.rbarton.wordapp.server.requireParty
import ch.rbarton.wordapp.server.send

suspend fun Connection.onRequestReceived(json: PartyOptions.SetPartyModeRequest)
{
    if (requireParty() || requireHost()) return

    if (party!!.gameMode == json.mode)
    {
        send(StatusCode.AlreadySet)
        return
    }

    party!!.gameMode = json.mode
    when (party!!.gameMode)
    {
        PartyMode.WordGame -> party!!.gameState = GameState()
        else -> party!!.gameState = null
    }

    party!!.broadcast(this, PartyOptions.SetPartyModeBroadcast(party!!.gameMode, party!!.gameState!!))
    send(StatusCode.Success)
}