package ch.rbarton.wordapp.server.request

import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.common.request.UserInfo
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.send
import ch.rbarton.wordapp.server.userInfos

suspend fun Connection.onRequestReceived(request: UserInfo.SetNameRequest)
{
    userInfo.name = request.name
    party?.broadcast(this, UserInfo.SetNameBroadcast(userId, userInfo.name))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(request: UserInfo.SetColorRequest)
{
    if (party != null)
        for (userId in party!!.connections.map { it.userId })
            if (userInfos[userId]?.colorId == request.colorId)
            {
                send(StatusCode.AlreadyExists)
                return
            }

    userInfo.colorId = request.colorId
    party?.broadcast(this, UserInfo.SetColorBroadcast(userId, userInfo.colorId))
    send(StatusCode.Success)
}