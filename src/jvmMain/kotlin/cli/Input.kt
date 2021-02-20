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
                    if (args.size <= 1)
                    {
                        println("Use: setname [new name]")
                        continue
                    }

                    name = args[1]
                    send(SetNameJson(name)) { response ->
                        when
                        {
                            response !is StatusJson -> println("SetName: $response")
                            response.status != StatusCode.Success -> println("SetName: ${response.status}")
                            else -> println("Successfully set name")
                        }
                    }
                }
                "create" -> send(ActionType.CreateParty)
                "join" ->
                {
                    if (args.size <= 1)
                    {
                        println("Use: join [party code]")
                        continue
                    }

                    send(JoinPartyJson(args[1])) { response ->
                        if (response is JoinPartyResponseJson)
                        {
                            partyCode = args[1]
                            users = response.userToNames.toMutableMap()
                            println("Joined party with ${users!!.size} users: ${users!!.values.joinToString(", ")}")
                        }
                        else if (response is StatusJson)
                            println("Join Party: ${response.status}")
                        else
                            println("Error in JoinParty: $response")
                    }
                }
                "chat" ->
                {
                    if (args.size <= 1)
                    {
                        println("Use: chat [message]")
                        continue
                    }

                    send(ChatJson(args[1]))
                }
                "leave" ->
                {
                    send(ActionType.LeaveParty) { response ->
                        if (response is StatusJson && response.status == StatusCode.Success) {
                            users = null
                            puid = null
                            partyCode = null
                            println("Left party")
                        }
                        else
                            println("Error in LeaveParty: $response")
                    }
                }
                "get" ->
                {
                    when (if (args.size > 1) args[1] else "")
                    {
                        "users", "names" -> println("users: ${users?.values?.joinToString(", ")}")
                        "party", "code" -> println("partyCode: $partyCode")
                        else -> println(
                            "Use: get [attribute]\n" +
                                    "  1. users | names\n" +
                                    "  2. party | code"
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