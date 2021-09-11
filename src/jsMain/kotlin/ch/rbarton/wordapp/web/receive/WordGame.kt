package ch.rbarton.wordapp.web.receive

import ch.rbarton.wordapp.common.request.WordGame
import ch.rbarton.wordapp.web.App
import react.setState

fun App.onRequestReceived(response: WordGame.SetGameSettingsRequest)
{
    if (state.party == null || state.party!!.stateShared == null) return

    setState {
        party!!.stateShared!!.settings = response.settings
    }
}