package ch.rbarton.wordapp.cli

import ch.rbarton.wordapp.common.connection.send
import ch.rbarton.wordapp.common.request.*
import io.ktor.client.features.websocket.*

suspend fun DefaultClientWebSocketSession.inputMessages()
{
    send(SetNameJson(name))

    while (true)
    {
        val command = readLine()?.trim() ?: ""
        if (command == "exit") return
        parseCliCommand(command)
    }
}

suspend fun DefaultClientWebSocketSession.parseCliCommand(command: String)
{
    val args = command.split(" ", limit = 2)

    try
    {
        when (args[0])
        {
            "help" ->
            {
                println(
                    """
                    setname [new name]
                    
                    -- When not in a party:
                    create
                    join [party code]
                    
                    -- When in a party:
                    chat [message]
                    get [attribute]
                    leave                    
                """.trimIndent()
                )
            }
            "setname" ->
            {
                if (args.size <= 1)
                {
                    println("Use: setname [new name]")
                    return
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
                    return
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
                    return
                }

                send(ChatJson(args[1]))
            }
            "leave" ->
            {
                send(ActionType.LeaveParty) { response ->
                    if (response is StatusJson && response.status == StatusCode.Success)
                    {
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
            "addcard" ->
            {
                if (args.size <= 1)
                {
                    println("Use: addcard [text]")
                    return
                }

                send(AddWordJson(args[1], "General"))
            }
            else -> println("Invalid command")
        }
    }
    catch (e: Exception)
    {
        println("Error while sending: ${e.localizedMessage}")
        return
    }
}
