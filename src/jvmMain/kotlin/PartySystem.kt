import kotlin.collections.*

/**
 * Contains all functions for handling requests sent by a user related to the party/admin
 */

suspend fun Connection.onRequestReceived(data: SetNameJson)
{
    name = data.name
    party?.broadcast(this, SetNameBroadcastJson(guid, name))
    send(StatusCode.Success)
}

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
    send(JoinPartyResponseJson(party.connections.associateBy({ it.guid }, { it.name })))
}

/**
 * Chat within the party or with all not in a party
 */
suspend fun Connection.onRequestReceived(data: ChatJson)
{
    val peerConnections = if (partyCode == null)
        allConnections.filter { it.partyCode == null }
    else
        party!!.connections

    peerConnections.broadcast(this, ChatBroadcastJson(guid, data.message))
    send(StatusCode.Success)
}

// --- Actions with no data
/**
 * Multiplexer of different actions without a backing json data class
 */
suspend fun Connection.onRequestReceived(json: ActionJson)
{
    when (json.action)
    {
        ActionType.CreateParty -> createParty()
        ActionType.LeaveParty -> leaveParty()
    }
}

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
    parties[partyCode!!] = Party(partyCode!!, this)

    send(CreatePartyResponseJson(partyCode!!))
}

suspend fun Connection.leaveParty()
{
    // Remove from party and notify others
    party?.remove(this)
    partyCode = null

    send(StatusCode.Success)
}