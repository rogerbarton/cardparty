package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.data.Word
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.common.request.WordGame
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.requireHost
import ch.rbarton.wordapp.server.requireParty
import ch.rbarton.wordapp.server.send

/**
 * Handles requests made by users in a word game session
 */

suspend fun Connection.onRequestReceived(json: WordGame.SetGameSettingsRequest)
{
    if (requireParty()) return

    party!!.game.settings = json.settings
    party!!.broadcast(this, WordGame.SetGameSettingsRequest(party!!.game.settings))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(json: WordGame.AddCategoryRequest)
{
    if (requireParty()) return

    party!!.game.categories += json.value
    party!!.broadcast(this, WordGame.AddCategoryBroadcast(json.value))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(json: WordGame.AddWordRequest)
{
    if (requireParty()) return

    party!!.game.interviewWords += Word(json.value)
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(json: WordGame.SetGameStageRequest)
{
    if (requireParty()) return
    if (requireHost()) return

    party!!.broadcast(this, json)
    party!!.game.stage = json.stage
    send(StatusCode.Success)
}