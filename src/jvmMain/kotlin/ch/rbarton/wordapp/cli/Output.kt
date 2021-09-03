package ch.rbarton.wordapp.cli

import ch.rbarton.wordapp.common.connection.responseHandlerQueue
import ch.rbarton.wordapp.common.data.PartyMode
import ch.rbarton.wordapp.common.data.Word
import ch.rbarton.wordapp.common.request.*

import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ch.rbarton.wordapp.common.request.Party as PartyRequest

suspend fun DefaultClientWebSocketSession.outputMessages()
{
    try
    {
        for (frame in incoming)
        {
            when (frame)
            {
                is Frame.Text -> onFrameReceived(frame.readText())
                is Frame.Close ->
                {
                    println("Disconnected: ${frame.readReason()}")
                    return
                }
            }

        }
    }
    catch (e: CancellationException)
    {
    }
    catch (e: Exception)
    {
        println("Error while receiving: ${e.localizedMessage}")
    }
}

private fun onFrameReceived(rawText: String)
{
//    println(rawText)

    try
    {
        // Handle incoming messages differently depending on if they have a matching requestId
        val json = Json.decodeFromString<BaseRequest>(rawText)
        if (json.requestId == null)
            handleUnidentifiedResponse(json, rawText)
        else
            if (responseHandlerQueue.contains(json.requestId))
                responseHandlerQueue[json.requestId!!]!!.invoke(json)
            else
                println(
                    "<~[server] InvalidRequestId: BaseRequest.requestId = ${json.requestId} " +
                            "has no corresponding responseHandler in responseHandlerQueue.\n"
                )
    }
    catch (e: SerializationException)
    {
        println("<~[server] SerializationException: ${e.localizedMessage}\n")
    }
}

private fun handleUnidentifiedResponse(response: BaseRequest, rawText: String)
{
    when (response)
    {
        is InitResponse ->
        {
            connection.guid = response.guid
            println("Hi, there are ${response.userCount} people and ${response.parties.size} parties here.")
            var i = 1
            response.parties.forEach {
                println("\t${i++.toString().padStart(2)}. ${it.key} (${it.value})")
            }
        }
        is StatusResponse -> println("${response.status.name}${if (response.message != null) ": ${response.message}" else ""}")
        is ActionRequest -> println(response.action.name)
        is UserInfo.SetNameBroadcast ->
        {
            if (party != null)
            {
                println("[${response.userId}:${party!!.users[response.userId]}] Changed name to ${response.name}")
                party!!.users[response.userId] = response.name
            }
        }
        is PartyRequest.JoinBroadcast ->
        {
            if (party == null) return

            party!!.users[response.userId] = response.name
            println("[${response.userId}:${response.name}] Joined party")
        }
        is PartyRequest.LeaveBroadcast ->
        {
            if (party == null) return

            println("[${response.userId}:${party!!.users[response.userId]}] Left party")
            party!!.users.remove(response.userId)
        }
        is PartyOptions.SetPartyModeBroadcast ->
        {
            if (party == null) return

            party!!.mode = response.mode
            party!!.gameState = response.gameState

            when (party!!.mode)
            {
                PartyMode.Idle -> println("Party is idle.")
                PartyMode.WordGame -> println("Word game started.")
            }
        }
        is Chat.MessageBroadcast ->
        {
            if (party != null)
                println("[${response.userId}:${party!!.users.get(response.userId)}]: ${response.message}")
        }
        is WordGame.AddWordBroadcast ->
        {
            if (party == null || party!!.gameState == null) return

            if (!party!!.gameState!!.words.keys.contains(response.category))
                party!!.gameState!!.words += Pair(response.category, mutableSetOf())

            party!!.gameState!!.words[response.category]!! += Word(response.value)

            println("${response.value} added to ${response.category}")
        }
        is WordGame.AddCategoryBroadcast ->
        {
            if (party == null || party!!.gameState == null) return

            if (!party!!.gameState!!.words.keys.contains(response.value))
                party!!.gameState!!.words += Pair(response.value, mutableSetOf())

            println("Category ${response.value} added")
        }
        else -> println("-> Unhandled Message: $rawText")
    }
}