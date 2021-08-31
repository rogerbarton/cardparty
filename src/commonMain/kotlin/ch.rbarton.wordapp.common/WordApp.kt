package ch.rbarton.wordapp.common

import kotlinx.serialization.Serializable

@Serializable
data class Word(val value: String, val visible: Boolean = false)

@Serializable
class GameState(
    var stage: Stage = Stage.Lobby,
    var settings: GameSettings = GameSettings(),
    val categories: MutableSet<String> = mutableSetOf(),
    val interviewWords: MutableSet<Word> = mutableSetOf(),
    val intervieweeWords: MutableSet<Word> = mutableSetOf()
)
{
    enum class Stage
    {
        Lobby,
        CreateWords,
        Main,
    }
}