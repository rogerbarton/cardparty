package ch.rbarton.wordapp.server.receive

import ch.rbarton.wordapp.common.data.colors
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.data.Party
import ch.rbarton.wordapp.server.parties
import ch.rbarton.wordapp.server.send
import kotlin.collections.*
import kotlin.random.Random
import kotlin.random.nextInt
import ch.rbarton.wordapp.common.request.Party as PartyRequest

suspend fun Connection.createParty()
{
    // Check if we are already in a party
    if (partyCode != null)
    {
        send(StatusCode.AlreadyInAParty)
        return
    }

    partyCode = if (parties.isEmpty()) "0" else (0..10).random().toString()
    val party = Party(this, partyCode!!)
    parties[partyCode!!] = party

    send(PartyRequest.CreateResponse(partyCode!!))
}

/**
 * Contains all functions for handling requests sent by a user related to the party/admin
 */

suspend fun Connection.onRequestReceived(request: PartyRequest.JoinRequest)
{
    // Check if we are already in a party
    if (partyCode != null)
    {
        send(StatusCode.AlreadyInAParty)
        return
    }

    // Check if the party code is valid
    if (request.partyCode !in parties)
    {
        send(StatusCode.InvalidValue)
        return
    }

    // Accept: add to party and notify others
    val party = parties[request.partyCode]!!
    var colorChanged = false
    if (party.connections.map { it.userInfo.colorId }.contains(userInfo.colorId))
    {
        // Assign unique color
        val unusedColors = (0..colors.size).toMutableList()
        unusedColors.removeAll(party.connections.map { it.userInfo.colorId })
        userInfo.colorId = unusedColors.shuffled().getOrElse(0) { Random.nextInt(IntRange(0, colors.size - 1)) }
        colorChanged = true
    }

    party.connections += this
    partyCode = request.partyCode

    party.broadcast(this, PartyRequest.JoinBroadcast(userId, userInfo))
    send(
        PartyRequest.JoinResponse(
            party,
            party.connections.associateBy({ it.userId }, { it.userInfo }),
            if (colorChanged) userInfo.colorId else null
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