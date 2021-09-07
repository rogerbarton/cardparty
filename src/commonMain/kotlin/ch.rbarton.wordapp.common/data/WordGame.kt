package ch.rbarton.wordapp.common.data

import kotlinx.serialization.Serializable

@Serializable
data class Word(val value: String, val visible: Boolean = false)

@Serializable
class GameStateShared(
    var stage: GameStage = GameStage.Setup,
    var settings: GameSettings = GameSettings(),
    val words: MutableMap<String, MutableSet<Word>> = mutableMapOf(Pair("General", mutableSetOf())),
)

enum class GameStage
{
    Setup,
    Playing,
}

@Serializable
data class GameSettings(
    var cardsPerPlayer: Int = 4,
    var intervieweeCount: Int = 1,
    var interviewerCount: Int = 1,
    var playersCanEditCategories: Boolean = true,
)