package ch.rbarton.wordapp.common.data

import kotlinx.serialization.Serializable

/**
 * Options specific to the party, regardless of the game
 */
@Serializable
class PartyOptions(var gameMode: PartyMode = PartyMode.Idle, var chatEnabled: Boolean = true)

enum class PartyMode
{
    Idle,
    WordGame
}