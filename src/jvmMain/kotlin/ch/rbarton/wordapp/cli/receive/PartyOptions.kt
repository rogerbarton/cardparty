package ch.rbarton.wordapp.cli.receive

import ch.rbarton.wordapp.cli.party
import ch.rbarton.wordapp.common.data.PartyMode
import ch.rbarton.wordapp.common.request.PartyOptions

fun onRequestReceived(response: PartyOptions.SetPartyModeBroadcast)
{
    if (party == null) return

    party!!.mode = response.mode
    party!!.stateShared = response.state

    when (party!!.mode)
    {
        PartyMode.Idle -> println("Party is idle.")
        PartyMode.WordGame -> println("Word game started.")
    }
}