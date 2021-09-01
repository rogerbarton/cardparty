package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.request.*
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.Party
import ch.rbarton.wordapp.server.parties
import ch.rbarton.wordapp.server.send
import kotlin.collections.*

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
    val party = Party(partyCode!!, this)
    parties[partyCode!!] = party

    send(CreatePartyResponseJson(partyCode!!, party.options))
}

/**
 * Contains all functions for handling requests sent by a user related to the party/admin
 */

suspend fun Connection.onRequestReceived(data: JoinPartyJson)
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

    party.broadcast(this, JoinPartyBroadcastJson(guid, name))
    send(JoinPartyResponseJson(party.connections.associateBy({ it.guid }, { it.name }), party.host.guid, party.game))
}

suspend fun Connection.leaveParty()
{
    // Remove from party and notify others
    party?.remove(this)
    partyCode = null

    send(StatusCode.Success)
}