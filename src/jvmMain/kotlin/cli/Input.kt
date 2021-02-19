package cli

import common.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*

suspend fun DefaultClientWebSocketSession.inputMessages()
{
    send(SetNameJson(name))

    while (true)
    {
        val message = readLine() ?: ""
        val args = message.split(" ", limit = 2)

        try
        {
            when (args[0])
            {
                "setname" ->
                {
                    if (args.size > 1)
                    {
                        name = args[1]
                        send(SetNameJson(name)) { response ->
                            if (response !is StatusJson)
                                println("SetName: $response")
                            else if (response.status != StatusCode.Success)
                                println("SetName: ${response.status}")
                            else
                                println("Successfully set name")
                        }
                    }
                    else
                        println("Use: setname [new name]")
                }
                "create" -> send(ActionType.CreateParty)
                "join" ->
                {
                    if (args.size > 1)
                        send(JoinPartyJson(args[1])) { response ->
                            if (response is JoinPartyResponseJson)
                            {
                                users = response.userToNames.toMutableMap()
                                println("Joined party with ${users!!.size} users: ${users!!.values.joinToString(", ")}")
                            }
                            else if (response is StatusJson)
                                println("Join Party: ${response.status}")
                        }
                    else
                        println("Use: join [party code]")
                }
                "chat" ->
                {
                    if (args.size > 1)
                        send(ChatJson(args[1]))
                    else
                        println("Use: chat [message]")
                }
                "leave" ->
                {
                    send(ActionType.LeaveParty)
                    users = null  // TODO: Only clear once response is successful
                    puid = null
                }
                "get" ->
                {
                    when (if (args.size > 1) args[1] else "")
                    {
                        "users", "names" -> println("users: ${users?.values?.joinToString(", ")}")
                        else -> println(
                            "Use get [attribute]\n" +
                                    "  1. users | names"
                        )
                    }

                }
                "exit" -> break
                else -> println("Invalid command")
            }
        }
        catch (e: Exception)
        {
            println("Error while sending: ${e.localizedMessage}")
            return
        }
    }
}

val responseHandlerQueue: MutableMap<Int, (BaseJson) -> Unit> = mutableMapOf()

suspend fun WebSocketSession.send(payload: BaseJson, onResponse: (BaseJson) -> Unit)
{
    payload.requestId = genRequestId()
    responseHandlerQueue[payload.requestId!!] = onResponse
    send(payload)
}

suspend fun WebSocketSession.send(actionType: ActionType, onResponse: (BaseJson) -> Unit)
{
    send(ActionJson(actionType), onResponse)
}