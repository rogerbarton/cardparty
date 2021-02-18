package ch.rbarton.websockets.shared

import kotlinx.serialization.Serializable
import ch.rbarton.websockets.shared.*

@Serializable
data class Word(val value: String, val visible: Boolean, val votes: Int)

@Serializable
data class Settings(
    var cardCountPerPlayer: Int = 4,
    var intervieweeCount: Int = 1,
    var interviewerCount: Int = 1,
    var interviewCategories: MutableSet<String> = mutableSetOf(),
    var intervieweeCategories: MutableSet<String> = mutableSetOf(),
)

@Serializable
data class GameState(
    var stage: GameStage = GameStage.Lobby,
    val interviewWords: MutableSet<Word> = mutableSetOf(),
    val intervieweeWords: MutableSet<Word> = mutableSetOf(),
)

enum class GameStage{
    Lobby,
    CreateWords,
}