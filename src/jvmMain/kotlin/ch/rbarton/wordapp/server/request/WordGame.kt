package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.data.GameStage
import ch.rbarton.wordapp.common.data.Word
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.common.request.WordGame
import ch.rbarton.wordapp.server.*

/**
 * Handles requests made by users in a word game session
 */

suspend fun Connection.onRequestReceived(json: WordGame.SetGameSettingsRequest)
{
    if (requireParty() || requireHost() || requireGameState()) return

    party!!.stateShared!!.settings = json.settings
    party!!.broadcast(this, WordGame.SetGameSettingsRequest(party!!.stateShared!!.settings))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(json: WordGame.SetGameStageRequest)
{
    if (requireParty() || requireHost() || requireGameState()) return

    if (party!!.stateShared!!.stage == json.stage)
    {
        send(StatusCode.AlreadySet)
        return
    }

    party!!.stateShared!!.stage = json.stage
    party!!.broadcast(this, json)

    when (json.stage)
    {
        GameStage.Setup ->
        {
        }
        GameStage.Playing ->
        { // TODO: check enough words per player
            // Shuffle and scatter words to players
            val words = party!!.stateShared!!.words
            //   - select one word per player
            val shuffled = mutableMapOf<String, List<Word>>()
            for (category in words.keys)
                shuffled[category] = words[category]!!.shuffled()

            for ((i, conn) in party!!.connections.withIndex())
            {
                val selectedWords = mutableListOf<Word>()
                for (category in words.keys)
                    selectedWords += shuffled[category]!![i]

                conn.send(WordGame.AssignWordsScatter(selectedWords))
            }
        }
    }

    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(json: WordGame.AddCategoryRequest)
{
    if (requireParty() || requireGameState()) return

    if (party!!.stateShared!!.words.keys.contains(json.value))
    {
        send(StatusCode.AlreadyExists)
        return
    }

    party!!.stateShared!!.words += Pair(json.value, mutableSetOf())
    party!!.broadcast(null, WordGame.AddCategoryBroadcast(json.value))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(json: WordGame.AddWordRequest)
{
    if (requireParty() || requireGameState()) return

    if (!party!!.stateShared!!.words.keys.contains(json.category))
        party!!.stateShared!!.words += Pair(json.category, mutableSetOf())

    party!!.stateShared!!.words[json.category]!! += Word(json.value)
    party!!.broadcast(null, WordGame.AddWordBroadcast(json.value, json.category))
    send(StatusCode.Success)
}
