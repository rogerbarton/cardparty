package ch.rbarton.wordapp.common.data

import kotlinx.serialization.Serializable

abstract class PartyBase(
    val code: String,
    var options: PartyOptions = PartyOptions(),
    var mode: PartyMode = PartyMode.Idle,
    var stateShared: GameStateShared? = GameStateShared()
)

/**
 * Options specific to the party, regardless of the game
 */
@Serializable
class PartyOptions(var allowRename: Boolean = true)

enum class PartyMode
{
    Idle,
    WordGame
}