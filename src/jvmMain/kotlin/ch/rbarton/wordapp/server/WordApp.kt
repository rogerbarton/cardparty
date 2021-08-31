package ch.rbarton.wordapp.server

import ch.rbarton.wordapp.common.*

/**
 * Handles requests made by users in a word game session
 */

suspend fun Connection.onRequestReceived(json: SetGameSettingsJson)
{
    if(requireParty()) return

    party!!.game.settings = json.settings
    party!!.broadcast(this, SetGameSettingsJson(party!!.game.settings))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(json: AddCategoryJson)
{
    if(requireParty()) return

    party!!.game.categories += json.value
    party!!.broadcast(this, AddCategoryBroadcastJson(json.value))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(json: AddWordJson)
{
    if(requireParty()) return

    party!!.game.interviewWords += Word(json.value)
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(json: SetGameStageJson)
{
    if(requireParty()) return
    if(requireHost()) return

    party!!.broadcast(this, json)
    party!!.game.stage = json.stage
    send(StatusCode.Success)
}