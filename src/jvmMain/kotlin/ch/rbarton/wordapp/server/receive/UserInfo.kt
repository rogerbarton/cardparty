package ch.rbarton.wordapp.server.receive

import ch.rbarton.wordapp.common.data.colors
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.common.request.UserInfo
import ch.rbarton.wordapp.server.Connection
import ch.rbarton.wordapp.server.send
import ch.rbarton.wordapp.server.userInfos

suspend fun Connection.onRequestReceived(request: UserInfo.SetNameRequest)
{
    if (request.name.isEmpty() || request.name.length > 20)
    {
        send(StatusCode.InvalidValue)
        return
    }

    userInfo.name = request.name
    party?.broadcast(this, UserInfo.SetNameBroadcast(userId, userInfo.name))
    send(StatusCode.Success)
}

suspend fun Connection.onRequestReceived(request: UserInfo.SetColorRequest)
{
    if (request.colorId < 0 || request.colorId >= colors.size)
    {
        send(StatusCode.InvalidValue)
        return
    }

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