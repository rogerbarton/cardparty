package ch.rbarton.wordapp.server.data

import ch.rbarton.wordapp.common.data.GameStateShared
import ch.rbarton.wordapp.common.data.PartyBase
import ch.rbarton.wordapp.common.data.PartyMode
import ch.rbarton.wordapp.common.data.PartyOptions
import ch.rbarton.wordapp.common.request.ActionType
import ch.rbarton.wordapp.common.request.BaseRequest
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.broadcast
import ch.rbarton.wordapp.server.parties
import ch.rbarton.wordapp.common.request.Party as PartyRequest

/**
 * Holds all data generic to a party, a group of connections.
 */
class Party(
    var host: Connection,
    code: String,
    options: PartyOptions = PartyOptions(),
    gameMode: PartyMode = PartyMode.Idle,
    stateShared: GameStateShared? = GameStateShared()
) : PartyBase(code, host.guid, options, gameMode, stateShared)
{
    val connections: MutableSet<Connection> = mutableSetOf(host)

    suspend fun broadcast(origin: Connection?, message: BaseRequest) = connections.broadcast(origin, message)
    suspend fun broadcast(origin: Connection?, actionType: ActionType) = connections.broadcast(origin, actionType)

    suspend fun remove(connection: Connection)
    {
        if (connections.size == 1) // Remove party
        {
            parties -= code
            return
        }

        connections -= connection

        val hostChanged = host == connection
        if (hostChanged)
        {
            host = connections.random()
            hostGuid = host.guid
        }

        broadcast(connection, PartyRequest.LeaveBroadcast(connection.guid, if (hostChanged) hostGuid else null))
    }
}