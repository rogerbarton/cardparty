package ch.rbarton.wordapp.server

import ch.rbarton.wordapp.common.data.GameState
import ch.rbarton.wordapp.common.data.PartyOptions
import ch.rbarton.wordapp.common.request.ActionType
import ch.rbarton.wordapp.common.request.BaseJson
import ch.rbarton.wordapp.common.request.LeavePartyBroadcastJson
import ch.rbarton.wordapp.server.connection.Connection
import ch.rbarton.wordapp.server.connection.broadcast

/**
 * Holds all data generic to a party, a group of connections.
 */
class Party(
    val code: String,
    var host: Connection,
    val connections: MutableSet<Connection> = mutableSetOf(host),
    var options: PartyOptions = PartyOptions(),
    var game: GameState = GameState()
)
{
    suspend fun broadcast(origin: Connection, message: BaseJson) = connections.broadcast(origin, message)
    suspend fun broadcast(origin: Connection, actionType: ActionType) = connections.broadcast(origin, actionType)

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
            host = connections.random()

        broadcast(connection, LeavePartyBroadcastJson(connection.guid, if (hostChanged) host.guid else null))
    }
}