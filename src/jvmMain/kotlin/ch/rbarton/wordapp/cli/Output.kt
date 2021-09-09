package ch.rbarton.wordapp.cli

import ch.rbarton.wordapp.common.connection.responseHandlerQueue
import ch.rbarton.wordapp.common.data.PartyMode
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
                else ->
                {
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
            connection.userId = response.userId
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
            if (party == null || !party!!.users.contains(response.userId)) return

            println("[${response.userId}:${party!!.users[response.userId]?.name}] Changed name to ${response.name}")
            party!!.users[response.userId]!!.name = response.name
        }
        is UserInfo.SetColorBroadcast ->
        {
            if (party == null || !party!!.users.contains(response.userId)) return

            println("[${response.userId}:${party!!.users[response.userId]?.name}] Changed color to ${response.colorId}")
            party!!.users[response.userId]!!.colorId = response.colorId
        }
        is PartyRequest.JoinBroadcast ->
        {
            if (party == null) return

            party!!.users[response.userId] = response.userInfo
            println("[${response.userId}:${response.userInfo.name}] Joined party")
        }
        is PartyRequest.LeaveBroadcast ->
        {
            if (party == null) return

            println(
                "[${response.userId}:${party!!.users[response.userId]?.name}] Left party" +
                        if (response.newHost != null) ", ${party!!.users[response.newHost]} is now host" else ""
            )

            party!!.users.remove(response.userId)
            if (response.newHost != null)
                party!!.hostId = response.newHost
        }
        is PartyOptions.SetPartyModeBroadcast ->
        {
            if (party == null) return

            party!!.mode = response.mode
            party!!.stateShared = response.state

            when (party!!.mode)
            {
                PartyMode.Idle -> println("Party is idle.")
                PartyMode.WordGame -> println("Word game started.")
            }
        }
        is Chat.MessageBroadcast ->
        {
            if (party != null)
                println("[${response.userId}:${party!!.users[response.userId]?.name}]: ${response.message}")
        }
        is WordGame.SetGameStageRequest ->
        {
            if (party == null || party!!.stateShared == null) return

            party!!.stateShared!!.stage = response.stage
            println("Game stage: ${party!!.stateShared!!.stage}")
        }
        is WordGame.AddCardBroadcast ->
        {
            if (party == null || party!!.stateShared == null) return

            party!!.stateShared!!.cards[response.cardId] = response.card

            println("${response.card.text} added to ${response.card.categoryId}")
        }
        is WordGame.RemoveCardRequest ->
        {
            if (party == null || party!!.stateShared == null || !party!!.stateShared!!.cards.contains(response.cardId)) return

            println("Removing card: ${party!!.stateShared!!.cards[response.cardId]!!.text}")
            party!!.stateShared!!.cards.remove(response.cardId)
        }
        is WordGame.AddCategoryBroadcast ->
        {
            if (party == null || party!!.stateShared == null) return

            party!!.stateShared!!.categories[response.categoryId] = response.category

            println("Category ${response.category} added")
        }
        is WordGame.RemoveCategoryRequest ->
        {
            if (party == null || party!!.stateShared == null || !party!!.stateShared!!.categories.contains(response.categoryId)) return

            println("Removing category: ${party!!.stateShared!!.categories[response.categoryId]!!.text}")

            party!!.stateShared!!.categories.remove(response.categoryId)
            party!!.stateShared!!.cards =
                party!!.stateShared!!.cards.filter { it.value.categoryId != response.categoryId }.toMutableMap()
        }
        is WordGame.AssignWordsScatter ->
        {
            if (party == null || party!!.stateShared == null) return

            party!!.stateClient!!.myCards = response.cards

            println("Assigned words:\n${party!!.stateClient!!.myCards!!.joinToString { " - $it\n" }}")
        }
        else -> println("-> Unhandled Message: $rawText")
    }
}