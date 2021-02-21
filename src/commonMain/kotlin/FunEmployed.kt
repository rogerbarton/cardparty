package common

import kotlinx.serialization.Serializable

@Serializable
data class Word(val value: String, val visible: Boolean, val votes: Int)

@Serializable
class GameState(
    var stage: Stage = Stage.Lobby,
    var settings: GameSettings = GameSettings(),
    val interviewWords: MutableSet<Word> = mutableSetOf(),
    val intervieweeWords: MutableSet<Word> = mutableSetOf()
)
{
    enum class Stage
    {
        Lobby,
        CreateWords,
    }
}