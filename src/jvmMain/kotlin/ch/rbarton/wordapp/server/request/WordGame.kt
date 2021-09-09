package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.data.Card
import ch.rbarton.wordapp.common.data.CardCategory
import ch.rbarton.wordapp.common.data.GameStage
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.common.request.WordGame
import ch.rbarton.wordapp.server.*
import ch.rbarton.wordapp.server.Connection.Companion.lastCardId
import ch.rbarton.wordapp.server.Connection.Companion.lastCategoryId

/**
 * Handles requests made by users in a word game session
 */

suspend fun Connection.onRequestReceived(request: WordGame.SetGameSettingsRequest)
{
    if (requireParty() || requireHost() || requireGameState()) return

    party!!.stateShared!!.settings = request.settings
    party!!.broadcast(this, WordGame.SetGameSettingsRequest(party!!.stateShared!!.settings))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(request: WordGame.SetGameStageRequest)
{
    if (requireParty() || requireHost() || requireGameState()) return

    if (party!!.stateShared!!.stage == request.stage)
    {
        send(StatusCode.AlreadySet)
        return
    }

    party!!.stateShared!!.stage = request.stage
    party!!.broadcast(this, request)

    when (request.stage)
    {
        GameStage.Setup ->
        {
        }
        GameStage.Playing ->
        {
            // TODO: check enough words per player
            // Shuffle and scatter words to players
            val cards = party!!.stateShared!!.cards
            val categories = party!!.stateShared!!.categories
            //   - select one word per player
            val shuffled = mutableMapOf<Int, List<Card>>()
            for (categoryId in categories.keys)
                shuffled[categoryId] = cards.filter { it.value.categoryId == categoryId }.values.shuffled()

            for ((i, conn) in party!!.connections.withIndex())
            {
                val selectedCards = mutableListOf<Card>()
                for (categoryId in categories.keys)
                    selectedCards += shuffled[categoryId]!![i]

                conn.send(WordGame.AssignWordsScatter(selectedCards))
            }
        }
    }

    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(request: WordGame.AddCategoryRequest)
{
    if (requireParty() || requireGameState()) return

    val categories = party!!.stateShared!!.categories
    if (categories.filter { it.value.text == request.text }.isNotEmpty())
    {
        send(StatusCode.AlreadyExists)
        return
    }

    val category = CardCategory(request.text, request.colorId ?: 0)
    val categoryId = lastCategoryId.getAndIncrement()
    categories[categoryId] = category

    party!!.broadcast(null, WordGame.AddCategoryBroadcast(category, categoryId))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(request: WordGame.RemoveCategoryRequest)
{
    if (requireParty() || requireGameState()) return

    if (party!!.stateShared!!.categories.remove(request.categoryId) != null)
        party!!.broadcast(null, request)

    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(request: WordGame.AddCardRequest)
{
    if (requireParty() || requireGameState()) return

    if (!party!!.stateShared!!.categories.contains(request.categoryId))
    {
        send(StatusCode.InvalidRequest)
        return
    }

    val card = Card(request.text, request.categoryId, userId)
    val cardId = lastCardId.getAndIncrement()
    party!!.stateShared!!.cards[cardId] = card

    party!!.broadcast(null, WordGame.AddCardBroadcast(card, cardId))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(request: WordGame.RemoveCardRequest)
{
    if (requireParty() || requireGameState()) return

    if (party!!.stateShared!!.cards.remove(request.cardId) != null)
        party!!.broadcast(null, request)

    send(StatusCode.Success)
}
