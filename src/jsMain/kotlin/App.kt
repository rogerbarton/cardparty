import common.*

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.*

external interface AppState : RState
{
    var globalUserCount: Int
    var globalPartyCount: Int

    var guid: Int?
    var puid: Int?
    var partyCode: String?
    var name: String
    var users: MutableMap<Int, String>?

    var chatHistory: MutableList<String>
    var lastPartyStatus: StatusJson?

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
        globalUserCount = 0
        globalPartyCount = 0

        guid = null
        puid = null
        partyCode = null
        name = ""
        users = null

        chatHistory = mutableListOf()

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
        if (state.name.isEmpty())
        {
            welcome()
            setName()
            return
        }

        if (state.partyCode == null)
        {
            welcome()
            setParty()
        }
        else
            inParty()

        chat()
    }

    private fun RBuilder.welcome()
    {
        child(components.Welcome) {
            attrs.userCount = state.globalUserCount
            attrs.partyCount = state.globalPartyCount
        }
    }

    private fun RBuilder.setName()
    {
        h2 {
            +"Choose a Name"
        }
        child(components.NameField) {
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
    }

    private fun RBuilder.setParty()
    {
        child(components.SetParty) {
            attrs.partyCode = state.partyCode
            attrs.lastStatus = state.lastPartyStatus
            attrs.onCreateParty = {
                state.webSocketSession.launch {
                    println("CreateParty")
                    state.webSocketSession.send(ActionType.CreateParty)
                }
            }
            attrs.onJoinParty = { value ->
                state.webSocketSession.launch {
                    println("JoinParty $value")
                    state.webSocketSession.send(JoinPartyJson(value)) { response ->
                        when (response)
                        {
                            is JoinPartyResponseJson ->
                            {
                                setState {
                                    users = response.userToNames.toMutableMap()
                                    partyCode = value
                                }
                                println(
                                    "Joined party with ${state.users!!.size} users: ${
                                        state.users!!.values.joinToString(
                                            ", "
                                        )
                                    }"
                                )
                            }
                            is StatusJson ->
                            {
                                setState {
                                    lastPartyStatus = response
                                }
                            }
                            else -> handleUnidentifiedResponse(response)
                        }
                    }
                }
            }
        }
    }

    private fun RBuilder.inParty()
    {
        h1 {
            +"Word Game Lobby"
        }
        h2 {
            +"Join with code: "
            span(classes = "badge bg-primary") { +state.partyCode.toString() }
        }

        child(components.usersList) {
            attrs.thisUser = state.guid!!
            attrs.users = state.users!!
            attrs.onSetName = { newName ->
                state.webSocketSession.launch {
                    state.webSocketSession.send(SetNameJson(newName)) { response ->
                        if (response is StatusJson && response.status == StatusCode.Success)
                        {
                            setState {
                                name = newName
                                users!![guid!!] = newName
                            }
                        }
                        else
                            handleUnidentifiedResponse(response)
                    }
                }
            }
        }

//        child(components.gameSettings) {
//            attrs.editable = isHost
//            attrs.onSubmit = {
//
//            }
//        }

        button(classes = "btn btn-secondary mb-2") {
            +"Leave Party"
            attrs.onClickFunction = {
                state.webSocketSession.launch {
                    println("LeaveParty")
                    state.webSocketSession.send(ActionType.LeaveParty) { response ->
                        if (response is StatusJson && response.status == StatusCode.Success)
                        {
                            setState {
                                partyCode = null
                            }
                        }
                        else
                            handleUnidentifiedResponse(response)
                    }
                }
            }
        }
    }

    private fun RBuilder.chat()
    {
        child(components.Chat) {
            attrs.chatHistory = state.chatHistory
            attrs.onSubmit = { message ->
                val log = "[${state.guid}:${state.name}] $message"
                println(log)
                setState {
                    chatHistory.add(log)
                }
                state.webSocketSession.launch {
                    state.webSocketSession.send(ChatJson(message))
                }
            }
        }
    }
}

