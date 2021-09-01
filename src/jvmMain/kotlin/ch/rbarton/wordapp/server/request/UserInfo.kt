package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.request.SetNameBroadcastJson
import ch.rbarton.wordapp.common.request.SetNameJson
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.send

suspend fun Connection.onRequestReceived(data: SetNameJson)
{
    name = data.name
    party?.broadcast(this, SetNameBroadcastJson(guid, name))
    send(StatusCode.Success)
}