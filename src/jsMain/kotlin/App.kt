import common.*

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.html.*
import kotlinx.html.js.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.*
import styled.*

enum class AppPhase
{
    SetName,
    SetParty,
    Lobby,
}

external interface AppState : RState
{
    var phase: AppPhase

    var guid: Int?
    var puid: Int?
    var partyCode: String?
    var name: String
    var users: MutableMap<Int, String>?

    var serverAddress: String
    var serverPort: Int
    var httpClient: HttpClient
    var webSocketSession: WebSocketSession
}

class App : RComponent<RProps, AppState>()
{
    @KtorExperimentalAPI
    override fun AppState.init()
    {
        phase = AppPhase.SetName
        guid = null
        puid = null
        partyCode = null
        name = ""
        users = null

        serverAddress = "127.0.0.1"
        serverPort = 8080
        httpClient = HttpClient {
            install(WebSockets)
        }

        GlobalScope.launch {
            webSocketSession = httpClient.webSocketSession(
                method = HttpMethod.Get,
                host = serverAddress,
                port = serverPort,
                path = "/"
            ) {
                println("WebSocket running...")
            }
            receiveWebsocketFrames()
        }
    }

    override fun RBuilder.render()
    {
        h1 {
            +"Welcome to the word game!"
        }

        child(components.InputField) {
            attrs.title = "Choose a Name"
            attrs.onSubmit = { value ->
                setState {
                    name = value
                }
                state.webSocketSession.launch {
                    println("setname: ${state.name}")
                    state.webSocketSession.send(SetNameJson(state.name))
                }
            }
        }

        if (state.name.isNotEmpty())
        {
            child(components.SetParty) {
                attrs.partyCode = state.partyCode
                attrs.onCreateParty = {
                    state.webSocketSession.launch {
                        println("CreateParty")
                        state.webSocketSession.send(ActionType.CreateParty)
                    }
                }
                attrs.onJoinParty = { value ->
                    setState {
                        partyCode = value
                    }
                    state.webSocketSession.launch {
                        println("JoinParty ${state.partyCode}")
                        state.webSocketSession.send(JoinPartyJson(state.partyCode!!))
                    }
                }
            }

            child(components.Chat) {
                attrs.onSubmit = { message ->
                    state.webSocketSession.launch {
                        println("Chat: $message")
                        state.webSocketSession.send(ChatJson(message))
                    }
                }
            }
        }
    }

    private suspend fun receiveWebsocketFrames()
    {
        try
        {
            println("Receiving frames from incoming")
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


    suspend fun onFrameReceived(rawText: String)
    {
//        println(rawText)

        try
        {
            when (val json = Json.decodeFromString<BaseJson>(rawText))
            {
                is InitJson ->
                {
                    state.guid = json.guid
                    println("Hi, there are ${json.userCount} people and ${json.parties.size} parties here.")
                    var i = 1
                    json.parties.forEach {
                        println("\t${i.toString().padStart(2)}. ${it.key} (${it.value})")
                    }
                }
                is StatusJson -> println("${json.status.name}${if (json.message != null) ": ${json.message}" else ""}")
                is ActionJson -> println(json.action.name)
                is SetNameBroadcastJson ->
                {
                    println("[${json.userId}:${state.users!![json.userId]}] Changed name to ${json.name}")
                    state.users!![json.userId] = json.name
                }
                is CreatePartyResponseJson ->
                {
                    println("Created party with code: ${json.partyCode}")
                    state.users = mutableMapOf(0 to state.name)
                }
                is JoinPartyResponseJson ->
                {
                    state.users = json.userToNames.toMutableMap()
                    println("Joined party with ${state.users!!.size} users: ${state.users!!.values.joinToString(", ")}")
                }
                is JoinPartyBroadcastJson ->
                {
                    state.users!![json.userId] = json.name
                    println("[${json.userId}:${json.name}] Joined party")
                }
                is LeavePartyBroadcastJson ->
                {
                    println("[${json.userId}:${state.users!![json.userId]}] Left party")
                    state.users!!.remove(json.userId)
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
}
