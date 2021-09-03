package ch.rbarton.wordapp.common.data

import kotlinx.serialization.Serializable

@Serializable
data class Word(val value: String, val visible: Boolean = false)

@Serializable
class GameState(
    var stage: Stage = Stage.Setup,
    var settings: GameSettings = GameSettings(),
    val words: MutableMap<String, MutableSet<Word>> = mutableMapOf(Pair("General", mutableSetOf())),
)
{
    enum class Stage
    {
        Setup,
        CreateWords,
        Main,
    }
}

@Serializable
data class GameSettings(
    var cardsPerPlayer: Int = 4,
    var intervieweeCount: Int = 1,
    var interviewerCount: Int = 1,
    var interviewCategories: MutableSet<String> = mutableSetOf(),
    var intervieweeCategories: MutableSet<String> = mutableSetOf(),
)