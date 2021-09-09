package ch.rbarton.wordapp.common.client.data

import ch.rbarton.wordapp.common.data.Card

data class GameStateClient(
    var myCards: List<Card>? = null
)
