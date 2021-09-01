package ch.rbarton.wordapp.cli

import ch.rbarton.wordapp.common.connection.responseHandlerQueue
import ch.rbarton.wordapp.common.request.*

import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

suspend fun DefaultClientWebSocketSession.outputMessages()
{
    try
    {
        for (frame in incoming)
        {
            frame as? Frame.Text ?: continue
            onFrameReceived(frame.readText())
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

private suspend fun DefaultClientWebSocketSession.onFrameReceived(rawText: String)
{
//    println(rawText)

    try
    {
        // Handle incoming messages differently depending on if they have a matching requestId
        val json = Json.decodeFromString<BaseJson>(rawText)
        if (json.requestId == null)
        {
            handleUnidentifiedResponse(json, rawText)
        }
        else
        {
            if (responseHandlerQueue.contains(json.requestId))
                responseHandlerQueue[json.requestId!!]!!.invoke(json)
            else
                println("<~[server] InvalidRequestId: BaseJson.requestId = ${json.requestId} " +
                        "has no corresponding responseHandler in responseHandlerQueue.\n")
        }
    }
    catch (e: SerializationException)
    {
        println("<~[server] SerializationException: ${e.localizedMessage}\n")
    }
}

private fun DefaultClientWebSocketSession.handleUnidentifiedResponse(json: BaseJson, rawText: String)
{
    when (json)
    {
        is InitJson ->
        {
            guid = json.guid
            println("Hi, there are ${json.userCount} people and ${json.parties.size} parties here.")
            var i = 1
            json.parties.forEach {
                println("\t${i++.toString().padStart(2)}. ${it.key} (${it.value})")
            }
        }
        is StatusJson -> println("${json.status.name}${if (json.message != null) ": ${json.message}" else ""}")
        is ActionJson -> println(json.action.name)
        is SetNameBroadcastJson ->
        {
            println("[${json.userId}:${users!![json.userId]}] Changed name to ${json.name}")
            users!![json.userId] = json.name
        }
        is CreatePartyResponseJson ->
        {
            users = mutableMapOf(0 to name)
            partyCode = json.partyCode
            println("Created party with code: ${json.partyCode}")
        }
        is JoinPartyResponseJson ->
        {
            users = json.userToNames.toMutableMap()
            println("Joined party with ${users!!.size} users: ${users!!.values.joinToString(", ")}")
        }
        is JoinPartyBroadcastJson ->
        {
            users!![json.userId] = json.name
            println("[${json.userId}:${json.name}] Joined party")
        }
        is LeavePartyBroadcastJson ->
        {
            println("[${json.userId}:${users!![json.userId]}] Left party")
            users!!.remove(json.userId)
        }
        is ChatBroadcastJson ->
        {
            println("[${json.userId}:${users?.get(json.userId)}]: ${json.message}")
        }
        else -> println("-> $rawText")
    }
}