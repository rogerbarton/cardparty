package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.common.request.UserInfo
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.send

suspend fun Connection.onRequestReceived(data: UserInfo.SetNameRequest)
{
    name = data.name
    party?.broadcast(this, UserInfo.SetNameBroadcast(guid, name))
    send(StatusCode.Success)
}