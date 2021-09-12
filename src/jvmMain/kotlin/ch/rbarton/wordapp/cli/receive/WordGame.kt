package ch.rbarton.wordapp.cli.receive

import ch.rbarton.wordapp.cli.party
import ch.rbarton.wordapp.common.request.WordGame

fun onRequestReceived(response: WordGame.SetGameStageRequest)
{
    if (party == null || party!!.stateShared == null) return

    party!!.stateShared!!.stage = response.stage
    println("Game stage: ${party!!.stateShared!!.stage}")
}

fun onRequestReceived(response: WordGame.SetGameSettingsRequest)
{
    if (party == null || party!!.stateShared == null) return

    party!!.stateShared!!.settings = response.settings
    println("Game settings: ${party!!.stateShared!!.settings}")
}

fun onRequestReceived(response: WordGame.AddCategoryBroadcast)
{
    if (party == null || party!!.stateShared == null) return

    party!!.stateShared!!.categories[response.categoryId] = response.category

    println("Added category: ${response.category.text}")
}

fun onRequestReceived(response: WordGame.RemoveCategoryRequest)
{
    if (party == null || party!!.stateShared == null || !party!!.stateShared!!.categories.contains(response.categoryId)) return

    println("Removed category: ${party!!.stateShared!!.categories[response.categoryId]!!.text}")

    party!!.stateShared!!.categories.remove(response.categoryId)
    party!!.stateShared!!.cards =
        party!!.stateShared!!.cards.filter { it.value.categoryId != response.categoryId }.toMutableMap()
}

fun onRequestReceived(response: WordGame.AddCardBroadcast)
{
    if (party == null || party!!.stateShared == null) return

    party!!.stateShared!!.cards[response.cardId] = response.card

    println("${response.card.text} added to ${response.card.categoryId}")
}

fun onRequestReceived(response: WordGame.RemoveCardRequest)
{
    if (party == null || party!!.stateShared == null || !party!!.stateShared!!.cards.contains(response.cardId)) return

    println("Removed card: ${party!!.stateShared!!.cards[response.cardId]!!.text}")
    party!!.stateShared!!.cards.remove(response.cardId)
}

fun onRequestReceived(response: WordGame.AssignWordsScatter)
{
    if (party == null || party!!.stateShared == null) return

    party!!.stateClient!!.myCardIds = response.cards

    println(
        "Assigned words:\n${
            party!!.stateClient!!.myCardIds!!.map { party!!.stateShared!!.cards[it] }
                .joinToString(separator = "\n") { " - ${it?.text}" }
        }"
    )
}

