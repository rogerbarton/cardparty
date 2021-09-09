package ch.rbarton.wordapp.common.client.data

import ch.rbarton.wordapp.common.data.*

class Party(
    var users: MutableMap<Int, UserInfo>,
    code: String,
    hostId: Int,
    options: PartyOptions = PartyOptions(),
    gameMode: PartyMode = PartyMode.Idle,
    stateShared: GameStateShared? = GameStateShared(),  // userId: name
    var stateClient: GameStateClient? = GameStateClient(),
) : PartyBase(code, hostId, options, gameMode, stateShared)
{
    constructor(
        partyBase: PartyBase,
        users: MutableMap<Int, UserInfo>,
        stateClient: GameStateClient? = GameStateClient()
    ) : this(
        users,
        partyBase.code,
        partyBase.hostId,
        partyBase.options,
        partyBase.mode,
        partyBase.stateShared,
        stateClient
    )
}