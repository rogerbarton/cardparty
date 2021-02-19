package server

import common.*

import io.ktor.http.cio.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Holds all data generic to a party, a group of connections.
 */
class Party(val code: String, var host: Connection)
{
    val connections = mutableSetOf(host)
    var settings: Settings = Settings()
    var state: GameState = GameState()

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

        if (host == connection)
            host = connections.random()

        broadcast(connection, LeavePartyBroadcastJson(connection.guid))
    }
}

/**
 * Broadcast message to all other members, except the origin connection
 */
suspend fun Collection<Connection>.broadcast(origin: Connection, payload: BaseJson)
{
    val payloadText = Json.encodeToString(payload)
    forEach {
        if (it != origin)
            it.session.send(payloadText)
    }

    println(">-[${origin.guid}:${origin.name}] $payloadText")
}

suspend fun Collection<Connection>.broadcast(origin: Connection?, actionType: ActionType)
{
    val payloadText = Json.encodeToString(ActionJson(actionType) as BaseJson)
    forEach {
        if (it != origin)
            it.session.send(payloadText)
    }

    println(">-[${origin?.guid}:${origin?.name ?: "-"}] $actionType")
}