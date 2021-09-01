package ch.rbarton.wordapp.cli.data

import ch.rbarton.wordapp.common.data.GameState
import ch.rbarton.wordapp.common.data.PartyBase
import ch.rbarton.wordapp.common.data.PartyMode
import ch.rbarton.wordapp.common.data.PartyOptions

class Party(
    var puid: Int,
    var users: MutableMap<Int, String>,
    code: String,
    options: PartyOptions = PartyOptions(),
    gameMode: PartyMode = PartyMode.Idle,
    gameState: GameState? = GameState()
) : PartyBase(code, options, gameMode, gameState)