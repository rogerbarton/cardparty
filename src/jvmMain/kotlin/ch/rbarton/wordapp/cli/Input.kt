package ch.rbarton.wordapp.cli

import ch.rbarton.wordapp.common.connection.send
import ch.rbarton.wordapp.common.data.PartyMode
import ch.rbarton.wordapp.common.request.*
import io.ktor.client.features.websocket.*

suspend fun DefaultClientWebSocketSession.inputMessages()
{
    send(UserInfo.SetNameRequest(name))

    while (true)
    {
        val command = readLine()?.trim() ?: ""
        if (command == "exit") return
        parseCliCommand(command)
    }
}

suspend fun DefaultClientWebSocketSession.parseCliCommand(input: String)
{
    val (command, args) = splitFirstSpace(input)

    try
    {
        when (command)
        {
            "help" ->
            {
                println(
                    """
                    setname [new name]
                    
                    When not in a party:
                      create
                      join [party code]
                    
                    When in a party:
                      chat [message]
                      get [attribute]
                      leave
                                       
                    When in a game:
                      addword [text]
                """.trimIndent()
                )
            }
            "setname" ->
            {
                if (args == null)
                {
                    println("Use: setname [new name]")
                    return
                }

                name = args
                send(UserInfo.SetNameRequest(name)) { response ->
                    when
                    {
                        response !is StatusResponse -> println("SetName: $response")
                        response.status != StatusCode.Success -> println("SetName: ${response.status}")
                        else -> println("Successfully set name")
                    }
                }
            }
            "create" -> send(ActionType.PartyCreate)
            "join" ->
            {
                if (args == null)
                {
                    println("Use: join [party code]")
                    return
                }

                send(Party.JoinRequest(args)) { response ->
                    if (response is Party.JoinResponse)
                    {
                        partyCode = args
                        users = response.userToNames.toMutableMap()
                        println("Joined party with ${users!!.size} users: ${users!!.values.joinToString(", ")}")
                    }
                    else if (response is StatusResponse)
                        println("Join Party: ${response.status}")
                    else
                        println("Error in JoinParty: $response")
                }
            }
            "chat" ->
            {
                if (args == null)
                {
                    println("Use: chat [message]")
                    return
                }

                send(Chat.MessageRequest(args))
            }
            "leave" ->
            {
                send(ActionType.PartyLeave) { response ->
                    if (response is StatusResponse && response.status == StatusCode.Success)
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
                if (args == null)
                    println(
                        """Use: get [attribute]
                          1. users | names
                          2. party | code
                        """.trimIndent()
                    )
                else
                    when (args)
                    {
                        "users", "names" -> println("users: ${users?.values?.joinToString(", ")}")
                        "party", "code" -> println("partyCode: $partyCode")
                        else -> println("Unknown argument: $args")
                    }
            }
            "set" ->
            {
                if (args == null)
                    println(
                        """
                        Use: set [key] [value]
                          1. partymode
                        """.trimIndent()
                    )
                else
                {
                    val (key, value) = splitFirstSpace(args)
                    if (value == null)
                        println("Must give a value.")
                    else
                        when (key)
                        {
                            "partymode" ->
                            {
                                val mode: PartyMode? = PartyMode.values().firstOrNull { it.ordinal == value.toInt() }
                                if (mode == null)
                                    println("Invalid value.")
                                else
                                    send(PartyOptions.SetPartyModeRequest(mode))
                            }
                            else -> println("Unknown key: $key")
                        }
                }
            }
            "addcard" ->
            {
                if (args == null)
                {
                    println("Use: addcard [text]")
                    return
                }

                send(WordGame.AddWordRequest(args, "General"))
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

private fun splitFirstSpace(input: String): Pair<String, String?>
{
    val splitInput = input.split(" ", limit = 2)
    val command = splitInput[0]
    val args = splitInput.getOrNull(1)
    return Pair(command, args)
}
