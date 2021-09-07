package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.data.Party
import ch.rbarton.wordapp.server.parties
import ch.rbarton.wordapp.server.send
import kotlin.collections.*
import ch.rbarton.wordapp.common.request.Party as PartyRequest

suspend fun Connection.createParty()
{
    // Check if we are already in a party
    if (partyCode != null)
    {
        send(StatusCode.AlreadyInAParty)
        return
    }

//    partyCode = (0..10).random().toString()
    partyCode = "6"
    val party = Party(this, partyCode!!)
    parties[partyCode!!] = party

    send(PartyRequest.CreateResponse(partyCode!!, party.options))
}

/**
 * Contains all functions for handling requests sent by a user related to the party/admin
 */

suspend fun Connection.onRequestReceived(data: PartyRequest.JoinRequest)
{
    // Check if we are already in a party
    if (partyCode != null)
    {
        send(StatusCode.AlreadyInAParty)
        return
    }

    // Check if the party code is valid
    if (data.partyCode !in parties)
    {
        send(StatusCode.InvalidPartyCode)
        return
    }

    // Accept: add to party and notify others
    val party = parties[data.partyCode]!!
    party.connections += this
    partyCode = data.partyCode

    party.broadcast(this, PartyRequest.JoinBroadcast(guid, name))
    send(
        PartyRequest.JoinResponse(
            party.connections.size,
            party.connections.associateBy({ it.guid }, { it.name }),
            party.host.guid,
            party.options,
            party.stateShared
        )
    )
}

suspend fun Connection.leaveParty()
{
    // Remove from party and notify others
    party?.remove(this)
    partyCode = null

    send(StatusCode.Success)
}