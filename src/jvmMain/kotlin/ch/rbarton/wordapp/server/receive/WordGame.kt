package ch.rbarton.wordapp.server.receive

import ch.rbarton.wordapp.common.data.Card
import ch.rbarton.wordapp.common.data.Category
import ch.rbarton.wordapp.common.data.GameStage
import ch.rbarton.wordapp.common.data.colors
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.common.request.StatusResponse
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

    // Validation
    when (request.stage)
    {
        GameStage.Playing ->
        {
            val groupedCards = party!!.stateShared!!.cards.values.groupBy { it.categoryId }
            if (party!!.stateShared!!.categories.isEmpty() ||
                party!!.stateShared!!.categories.any {
                    (groupedCards[it.key]?.count() ?: 0) < party!!.connections.count()
                }
            )
            {
                send(StatusResponse(StatusCode.InvalidRequest, "Not enough cards per player"))
                return
            }
        }
    }

    // Accept and update stage
    party!!.stateShared!!.stage = request.stage
    party!!.broadcast(this, request)

    when (request.stage)
    {
        GameStage.Playing -> scatterCards()
    }

    send(StatusCode.Success)
}

/**
 * Shuffle and scatter words to players
 */
private suspend fun Connection.scatterCards()
{
    val cards = party!!.stateShared!!.cards
    val categories = party!!.stateShared!!.categories

    // Get Map<categoryId, List<cardId>> shuffled
    val grouped: MutableMap<Int, MutableList<Map.Entry<Int, Card>>> = mutableMapOf()
    val shuffledIds = cards.asIterable().groupByTo(grouped) { card -> card.value.categoryId }
        .mapValues { cardList -> cardList.value.map { it.key }.shuffled() }

    for ((i, conn) in party!!.connections.withIndex())
    {
        val selectedCards = mutableSetOf<Int>()
        for (categoryId in categories.keys)
            selectedCards += shuffledIds[categoryId]!![i]

        conn.send(WordGame.AssignWordsScatter(selectedCards))
    }
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

    val colorId = request.colorId ?: getUnusedId(colors.indices.toMutableList(), categories.map { it.value.colorId })
    val category = Category(request.text, colorId)
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
