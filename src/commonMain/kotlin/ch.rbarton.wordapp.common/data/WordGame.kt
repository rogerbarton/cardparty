package ch.rbarton.wordapp.common.data

import kotlinx.serialization.Serializable

@Serializable
data class Card(val text: String, val categoryId: Int, val userId: Int)

@Serializable
data class CardCategory(val text: String, val colorId: Int)

@Serializable
class GameStateShared(
    var stage: GameStage = GameStage.Setup,
    var settings: GameSettings = GameSettings(),
    val categories: MutableMap<Int, CardCategory> = mutableMapOf(),
    var cards: MutableMap<Int, Card> = mutableMapOf()
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