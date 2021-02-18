package cli

import common.*
import io.ktor.client.features.websocket.*

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
                        send(SetNameJson(name))
                    }
                    else
                        println("Use: setname [new name]")
                }
                "create" -> send(ActionType.CreateParty)
                "join" ->
                {
                    if (args.size > 1)
                        send(JoinPartyJson(args[1]))
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