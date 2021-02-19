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

suspend fun App.onFrameReceived(rawText: String)
{
//        println(rawText)

    try
    {
        when (val json = Json.decodeFromString<BaseJson>(rawText))
        {
            is InitJson ->
            {
                setState {
                    guid = json.guid
                    globalUserCount = json.userCount -1
                    globalPartyCount = json.parties.size
                }
                println("Hi, there are ${json.userCount -1} people and ${json.parties.size} parties here.")
                var i = 1
                json.parties.forEach {
                    println("\t${i++.toString().padStart(2)}. ${it.key} (${it.value})")
                }
            }
            is StatusJson -> println("${json.status.name}${if (json.message != null) ": ${json.message}" else ""}")
            is ActionJson -> println(json.action.name)
            is SetNameBroadcastJson ->
            {
                println("[${json.userId}:${state.users!![json.userId]}] Changed name to ${json.name}")
                setState {
                    users!![json.userId] = json.name
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
                setState {
                    users = json.userToNames.toMutableMap()
                }
                println("Joined party with ${state.users!!.size} users: ${state.users!!.values.joinToString(", ")}")
            }
            is JoinPartyBroadcastJson ->
            {
                setState {
                    users!![json.userId] = json.name
                }
                println("[${json.userId}:${json.name}] Joined party")
            }
            is LeavePartyBroadcastJson ->
            {
                println("[${json.userId}:${state.users!![json.userId]}] Left party")
                setState {
                    users!!.remove(json.userId)
                }
            }
            is ChatBroadcastJson ->
            {
                println("[${json.userId}:${state.users?.get(json.userId)}]: ${json.message}")
            }
            else -> println("-> $rawText")
        }
    }
    catch (e: SerializationException)
    {
        println("<~[server] SerializationException: ${e.message}\n")
    }
}