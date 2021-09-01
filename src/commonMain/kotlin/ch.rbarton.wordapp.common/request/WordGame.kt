package ch.rbarton.wordapp.common.request

import ch.rbarton.wordapp.common.data.GameState
import kotlinx.serialization.Serializable

@Serializable
data class SetGameSettingsJson(val settings: GameSettings) : BaseJson()

@Serializable
data class GameSettings(
    var cardsPerPlayer: Int = 4,
    var intervieweeCount: Int = 1,
    var interviewerCount: Int = 1,
    var interviewCategories: MutableSet<String> = mutableSetOf(),
    var intervieweeCategories: MutableSet<String> = mutableSetOf(),
)

@Serializable
data class SetGameStageJson(val stage: GameState.Stage) : BaseJson()

@Serializable
data class AddWordJson(val value: String, val category: String) : BaseJson()

@Serializable
data class AddWordBroadcastJson(val value: String, val category: String) : BaseJson()

@Serializable
data class AddCategoryJson(val value: String) : BaseJson()

@Serializable
data class AddCategoryBroadcastJson(val value: String) : BaseJson()