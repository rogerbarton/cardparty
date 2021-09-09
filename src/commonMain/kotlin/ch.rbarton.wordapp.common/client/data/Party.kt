package ch.rbarton.wordapp.common.client.data

import ch.rbarton.wordapp.common.data.GameStateShared
import ch.rbarton.wordapp.common.data.PartyBase
import ch.rbarton.wordapp.common.data.PartyMode
import ch.rbarton.wordapp.common.data.PartyOptions

class Party(
    var users: MutableMap<Int, String>,
    code: String,
    hostGuid: Int,
    options: PartyOptions = PartyOptions(),
    gameMode: PartyMode = PartyMode.Idle,
    stateShared: GameStateShared? = GameStateShared(),  // guid: name
    var stateClient: GameStateClient? = GameStateClient(),
) : PartyBase(code, hostGuid, options, gameMode, stateShared)
{
    constructor(
        partyBase: PartyBase,
        users: MutableMap<Int, String>,
        stateClient: GameStateClient? = GameStateClient()
    ) : this(
        users,
        partyBase.code,
        partyBase.hostGuid,
        partyBase.options,
        partyBase.mode,
        partyBase.stateShared,
        stateClient
    )
}