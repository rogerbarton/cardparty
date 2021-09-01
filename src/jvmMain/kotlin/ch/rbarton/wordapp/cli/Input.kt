package ch.rbarton.wordapp.cli

import ch.rbarton.wordapp.cli.data.Party
import ch.rbarton.wordapp.common.connection.send
import ch.rbarton.wordapp.common.data.PartyMode
import ch.rbarton.wordapp.common.request.*
import io.ktor.client.features.websocket.*
import ch.rbarton.wordapp.common.request.Party as PartyRequest

suspend fun DefaultClientWebSocketSession.inputMessages()
{
    send(UserInfo.SetNameRequest(connection.name))

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

                connection.name = args
                send(UserInfo.SetNameRequest(connection.name)) { response ->
                    when
                    {
                        response !is StatusResponse -> println("SetName: $response")
                        response.status != StatusCode.Success -> println("SetName: ${response.status}")
                        else -> println("Successfully set name")
                    }
                }
            }
            "create" -> send(ActionType.PartyCreate) { response ->
                when (response)
                {
                    is PartyRequest.CreateResponse ->
                    {
                        party = Party(0, mutableMapOf(0 to connection.name), response.partyCode, response.partyOptions)
                        println("Created party with code: ${response.partyCode}")
                    }
                    else -> println("Error in create party: $response")
                }
            }
            "join" ->
            {
                if (args == null)
                {
                    println("Use: join [party code]")
                    return
                }

                send(PartyRequest.JoinRequest(args)) { response ->
                    when (response)
                    {
                        is PartyRequest.JoinResponse ->
                        {
                            party = Party(response.puid, response.userToNames.toMutableMap(), args, response.options)
                            println(
                                "Joined party with ${party!!.users.size} users: ${
                                    party!!.users.values.joinToString(
                                        ", "
                                    )
                                }"
                            )
                        }
                        is StatusResponse -> println("Join Party: ${response.status}")
                        else -> println("Error in JoinParty: $response")
                    }
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
                        party = null
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
                        "users", "names" -> println("users: ${party?.users?.values?.joinToString(", ")}")
                        "party", "code" -> println("partyCode: ${party?.code}")
                        else -> println("Unknown argument: $args")
                    }
            }
            "set" ->
            {
                if (args == null)
                {
                    println(
                        """
                        Use: set [key] [value]
                          1. partymode
                        """.trimIndent()
                    )
                    return
                }

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
            "addword" ->
            {
                if (args == null)
                {
                    println("Use: addword [text]")
                    return
                }

                send(WordGame.AddWordRequest(args, "General"))
            }
            "print" ->
            {
                if (args == null)
                {
                    println(
                        """
                        Use: print [key]
                          1. code
                          2. users
                          3. words
                          4. categories
                        Prints local state
                        """.trimIndent()
                    )
                    return

                }

                val (key, value) = splitFirstSpace(args)
                if (value == null)
                {
                    println("Must give a value.")
                    return
                }

                when (key)
                {
                    "code" -> println(party?.code)
                    "users" -> println(party?.users)
                    "words" -> println(party?.gameState?.interviewWords)
                    "categories" -> println(party?.gameState?.categories)
                    else -> println("Unknown key: $key")
                }
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
