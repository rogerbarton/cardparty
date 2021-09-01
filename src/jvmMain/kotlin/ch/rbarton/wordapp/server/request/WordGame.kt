package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.data.Word
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.common.request.WordGame
import ch.rbarton.wordapp.server.*

/**
 * Handles requests made by users in a word game session
 */

suspend fun Connection.onRequestReceived(json: WordGame.SetGameSettingsRequest)
{
    if (requireParty() || requireGameState()) return

    party!!.gameState!!.settings = json.settings
    party!!.broadcast(this, WordGame.SetGameSettingsRequest(party!!.gameState!!.settings))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(json: WordGame.AddCategoryRequest)
{
    if (requireParty() || requireGameState()) return

    party!!.gameState!!.categories += json.value
    party!!.broadcast(this, WordGame.AddCategoryBroadcast(json.value))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(json: WordGame.AddWordRequest)
{
    if (requireParty() || requireGameState()) return

    party!!.gameState!!.interviewWords += Word(json.value)
    party!!.broadcast(this, WordGame.AddWordBroadcast(json.value, json.category))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(json: WordGame.SetGameStageRequest)
{
    if (requireParty() || requireHost() || requireGameState()) return

    party!!.gameState!!.stage = json.stage
    party!!.broadcast(this, json)
    send(StatusCode.Success)
}