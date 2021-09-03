package ch.rbarton.wordapp.cli

import ch.rbarton.wordapp.cli.data.Party
import ch.rbarton.wordapp.common.connection.send
import ch.rbarton.wordapp.common.data.PartyMode
import ch.rbarton.wordapp.common.request.*
import io.ktor.client.features.websocket.*
import kotlinx.coroutines.Job
import ch.rbarton.wordapp.common.request.Party as PartyRequest

suspend fun DefaultClientWebSocketSession.inputMessages(messageOutputRoutine: Job)
{
    send(UserInfo.SetNameRequest(connection.name))

    while (true)
    {
        val command = readLine()?.trim() ?: ""
        if (command == "exit" || messageOutputRoutine.isCompleted) return
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
                    print [key]
                    set [key] [value]
                    
                    When not in a party:
                      create
                      join [party code]
                    
                    When in a party:
                      chat [message]
                      leave
                                       
                    When in a game:
                      addword [text]
                """.trimIndent()
                )
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
                                "Joined party with ${party!!.users.size} users: ${party!!.users.values.joinToString(", ")}"
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
            "set" ->
            {
                if (args == null)
                {
                    println(
                        """
                        Use: set [key] [value]
                          1. name
                          2. partymode
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
                        "name" ->
                        {
                            connection.name = value
                            send(UserInfo.SetNameRequest(connection.name)) { response ->
                                when
                                {
                                    response !is StatusResponse -> println(response)
                                    response.status != StatusCode.Success -> println(response.status.toString())
                                    else -> println("Successfully set name")
                                }
                            }
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

                when (args)
                {
                    "code" -> println(party?.code)
                    "users" -> println(party?.users)
                    "words" -> println(party?.gameState?.interviewWords)
                    "categories" -> println(party?.gameState?.categories)
                    "partymode" -> println(party?.mode)
                    else -> println("Unknown key: $args")
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
