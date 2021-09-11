package ch.rbarton.wordapp.web.receive

import ch.rbarton.wordapp.common.data.PartyMode
import ch.rbarton.wordapp.common.request.PartyOptions
import ch.rbarton.wordapp.web.App
import react.setState

fun App.onRequestReceived(response: PartyOptions.SetPartyModeBroadcast)
{
    if (state.party == null) return

    setState {
        party!!.mode = response.mode
        party!!.stateShared = response.state
    }

    when (state.party!!.mode)
    {
        PartyMode.Idle -> println("Party is idle.")
        PartyMode.WordGame -> println("Word game started.")
    }
}