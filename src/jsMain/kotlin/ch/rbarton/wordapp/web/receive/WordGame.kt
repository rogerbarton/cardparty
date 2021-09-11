package ch.rbarton.wordapp.web.receive

import ch.rbarton.wordapp.common.request.WordGame
import ch.rbarton.wordapp.web.App
import react.setState

fun App.onRequestReceived(response: WordGame.SetGameStageRequest)
{
    if (state.party == null || state.party!!.stateShared == null) return

    val log = "Game stage: ${state.party!!.stateShared!!.stage}"
    println(log)

    setState {
        party!!.stateShared!!.stage = response.stage
        chatHistory.add(log)
    }
}

fun App.onRequestReceived(response: WordGame.SetGameSettingsRequest)
{
    if (state.party == null || state.party!!.stateShared == null) return

    setState {
        party!!.stateShared!!.settings = response.settings
    }
}


fun App.onRequestReceived(response: WordGame.AddCategoryBroadcast)
{
    if (state.party == null || state.party!!.stateShared == null) return

    val log = "Category ${response.category.text} added"
    println(log)

    setState {
        party!!.stateShared!!.categories[response.categoryId] = response.category
        chatHistory.add(log)
    }
}

fun App.onRequestReceived(response: WordGame.RemoveCategoryRequest)
{
    if (state.party == null || state.party!!.stateShared == null ||
        !state.party!!.stateShared!!.categories.contains(response.categoryId)
    ) return

    val log = "Removing category: ${state.party!!.stateShared!!.categories[response.categoryId]!!.text}"
    println(log)

    setState {
        party!!.stateShared!!.categories.remove(response.categoryId)
        party!!.stateShared!!.cards =
            state.party!!.stateShared!!.cards.filter { it.value.categoryId != response.categoryId }.toMutableMap()
        chatHistory.add(log)
    }
}

fun App.onRequestReceived(response: WordGame.AddCardBroadcast)
{
    if (state.party == null || state.party!!.stateShared == null) return

    val log = "${response.card.text} added to ${response.card.categoryId}"
    println(log)

    setState {
        party!!.stateShared!!.cards[response.cardId] = response.card
        chatHistory.add(log)
    }
}

fun App.onRequestReceived(response: WordGame.RemoveCardRequest)
{
    if (state.party == null || state.party!!.stateShared == null || !state.party!!.stateShared!!.cards.contains(response.cardId)) return

    val log = "Removing card: ${state.party!!.stateShared!!.cards[response.cardId]!!.text}"
    println(log)

    setState {
        party!!.stateShared!!.cards.remove(response.cardId)
        chatHistory.add(log)
    }
}

fun App.onRequestReceived(response: WordGame.AssignWordsScatter)
{
    if (state.party == null || state.party!!.stateShared == null) return

    val log = "Assigned words:\n${response.cards.joinToString { " - $it\n" }}"
    println(log)

    setState {
        party!!.stateClient!!.myCards = response.cards
        chatHistory.add(log)
    }
}
