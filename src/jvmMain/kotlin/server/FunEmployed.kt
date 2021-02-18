package server

import common.*

/**
 * Handles requests made by users in a FunEmployed game session
 */

suspend fun Connection.onRequestReceived(json: AddWordJson)
{
    if(party == null)
    {
        send(StatusCode.InvalidPartyCode)
        return
    }

    party!!.state.interviewWords += Word(json.value, false, 0)
    send(StatusCode.Success)
}


suspend fun Connection.onRequestReceived(json: SetGameStageJson)
{
    if(party == null)
    {
        send(StatusCode.InvalidPartyCode)
        return
    }

    TODO()
}