package ch.rbarton.wordapp.common.data

abstract class PartyBase(
    val code: String,
    var options: PartyOptions = PartyOptions(),
    var mode: PartyMode = PartyMode.Idle,
    var gameState: GameState? = GameState()
)