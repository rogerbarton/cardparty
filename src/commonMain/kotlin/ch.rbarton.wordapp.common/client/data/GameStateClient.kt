package ch.rbarton.wordapp.common.client.data

import ch.rbarton.wordapp.common.data.Word

data class GameStateClient(
    var myWords: List<Word>? = null
)
