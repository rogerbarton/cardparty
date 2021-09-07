package ch.rbarton.wordapp.cli.data

import ch.rbarton.wordapp.common.data.Word

data class GameStateClient(
    var myWords: List<Word>? = null
)
