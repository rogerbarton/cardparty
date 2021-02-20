import common.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.setState

suspend fun App.receiveWebsocketFrames()
{
    try
    {
        for (frame in state.webSocketSession.incoming)
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
        println("Error while receiving: ${e.message}")
    }
}

private fun App.onFrameReceived(rawText: String)
{
//    println(rawText)

    try
    {
        val json = Json.decodeFromString<BaseJson>(rawText)
        if (json.requestId == null)
        {
            handleUnidentifiedResponse(json)
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
        println("<~[server] SerializationException: ${e.message}\n")
    }
}

fun App.handleUnidentifiedResponse(json: BaseJson)
{
    when (json)
    {
        is InitJson ->
        {
            setState {
                guid = json.guid
                globalUserCount = json.userCount - 1
                globalPartyCount = json.parties.size
            }
            println("Hi, there are ${json.userCount - 1} people and ${json.parties.size} parties here.")
            var i = 1
            json.parties.forEach {
                println("\t${i++.toString().padStart(2)}. ${it.key} (${it.value})")
            }
        }
        is StatusJson -> println("${json.status.name}${if (json.message != null) ": ${json.message}" else ""}")
        is ActionJson -> println(json.action.name)
        is SetNameBroadcastJson ->
        {
            val log = "[${json.userId}:${state.users!![json.userId]}] Changed name to ${json.name}"
            println(log)
            setState {
                users!![json.userId] = json.name
                chatHistory.add(log)
            }
        }
        is CreatePartyResponseJson ->
        {
            setState {
                partyCode = json.partyCode
                puid = 0
                users = mutableMapOf(0 to state.name)
            }
            println("Created party with code: ${json.partyCode}")
        }
        is JoinPartyResponseJson ->
        {
            val log = "Joined party with ${state.users!!.size} users: ${state.users!!.values.joinToString(", ")}"
            setState {
                users = json.userToNames.toMutableMap()
                chatHistory.add(log)
            }
            println(log)
        }
        is JoinPartyBroadcastJson ->
        {
            val log = "[${json.userId}:${json.name}] Joined party"
            setState {
                users!![json.userId] = json.name
                chatHistory.add(log)
            }
            println(log)
        }
        is LeavePartyBroadcastJson ->
        {
            val log = "[${json.userId}:${state.users!![json.userId]}] Left party"
            println(log)
            setState {
                users!!.remove(json.userId)
                chatHistory.add(log)
            }
        }
        is ChatBroadcastJson ->
        {
            val message = "[${json.userId}:${state.users?.get(json.userId)}]: ${json.message}"
            setState{
                chatHistory.add(message)
            }
            println(message)
        }
        else -> println("-> $json")
    }
}