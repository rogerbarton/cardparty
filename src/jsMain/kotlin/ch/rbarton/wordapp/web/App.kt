package ch.rbarton.wordapp.web

import ch.rbarton.wordapp.common.connection.send
import ch.rbarton.wordapp.common.data.GameStateShared
import ch.rbarton.wordapp.common.request.ActionType
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.common.request.StatusResponse
import ch.rbarton.wordapp.common.request.UserInfo
import ch.rbarton.wordapp.web.components.*
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.button
import react.dom.h1
import react.dom.h2
import react.dom.span
import ch.rbarton.wordapp.common.request.Chat as ChatRequest
import ch.rbarton.wordapp.common.request.Party as PartyRequest

data class Party(
    val code: String,
    var users: MutableMap<Int, String>,
    var host: Int,
    var state: GameStateShared?
)

external interface AppState : State
{
    var globalUserCount: Int
    var globalPartyCount: Int

    var guid: Int?
    var puid: Int?
    var name: String
    var party: Party?

    var chatHistory: MutableList<String>
    var lastPartyStatus: StatusResponse?

    var serverAddress: String
    var serverPort: Int
    var httpClient: HttpClient
    var ws: WebSocketSession
}

class App : RComponent<RProps, AppState>()
{
    override fun AppState.init()
    {
        chatHistory = mutableListOf()
        name = ""

        serverAddress = "127.0.0.1"
        serverPort = 8080
        httpClient = HttpClient {
            install(WebSockets)
        }

        GlobalScope.launch {
            ws = httpClient.webSocketSession(
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

        if (state.party == null)
        {
            welcome()
            setParty()
        }
        else
        {
            inParty()
            chat()
        }

    }

    private fun RBuilder.welcome()
    {
        child(Welcome) {
            attrs.userCount = state.globalUserCount
            attrs.partyCount = state.globalPartyCount
        }
    }

    private fun RBuilder.setName()
    {
        h2 {
            +"Choose a Name"
        }
        child(NameField) {
            attrs.onSubmit = { value ->
                setState {
                    name = value
                }
                state.ws.launch {
                    println("setname: ${state.name}")
                    state.ws.send(UserInfo.SetNameRequest(state.name))
                }
            }
        }
    }

    private fun RBuilder.setParty()
    {
        child(SetParty) {
            attrs.partyCode = state.party?.code
            attrs.lastStatus = state.lastPartyStatus
            attrs.onDismissLastStatus = { setState { lastPartyStatus = null } }
            attrs.onCreateParty = {
                println("CreateParty")
                setState { lastPartyStatus = null }
                state.ws.launch {
                    state.ws.send(ActionType.PartyCreate) { response ->
                        when (response)
                        {
                            is PartyRequest.CreateResponse ->
                            {
                                setState {
                                    party = Party(
                                        response.partyCode,
                                        mutableMapOf(state.guid!! to state.name),
                                        state.guid!!,
                                        GameStateShared()
                                    )
                                }
                                println("Created party with code: ${response.partyCode}")
                            }
                            is StatusResponse ->
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
            attrs.onJoinParty = { value ->
                println("JoinParty $value")
                setState { lastPartyStatus = null }
                state.ws.launch {
                    state.ws.send(PartyRequest.JoinRequest(value)) { response ->
                        when (response)
                        {
                            is PartyRequest.JoinResponse ->
                            {
                                setState {
                                    party = Party(
                                        value,
                                        response.userToNames.toMutableMap(),
                                        response.host,
                                        response.gameState
                                    )
                                }
                                println("Joined party with ${state.party!!.users.size} users")
                            }
                            is StatusResponse ->
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
            span(classes = "badge bg-primary") { +state.party!!.code }
        }

        child(usersList) {
            attrs.thisUser = state.guid!!
            attrs.users = state.party!!.users
            attrs.onSetName = { newName ->
                state.ws.launch {
                    state.ws.send(UserInfo.SetNameRequest(newName)) { response ->
                        if (response is StatusResponse && response.status == StatusCode.Success)
                        {
                            setState {
                                name = newName
                                party!!.users[guid!!] = newName
                            }
                        }
                        else
                            handleUnidentifiedResponse(response)
                    }
                }
            }
        }

//        child(components.GameSettings::class) {
//            attrs.editable = true
//            attrs.settings = state.party!!.state.settings
//            attrs.onSubmit = { newSettings ->
//                state.ws.launch {
//                    state.ws.send(WordGame.SetGameSettingsJson(newSettings))
//                }
//            }
//        }

        button(classes = "btn btn-secondary mb-2") {
            +"Leave ch.rbarton.wordapp.web.Party"
            attrs.onClickFunction = {
                println("LeaveParty")
                state.ws.launch {
                    state.ws.send(ActionType.PartyLeave) { response ->
                        if (response is StatusResponse && response.status == StatusCode.Success)
                        {
                            setState {
                                party = null
                                lastPartyStatus = null
                                chatHistory.clear()
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
        child(Chat) {
            attrs.chatHistory = state.chatHistory
            attrs.onSubmit = { message ->
                val log = "[${state.guid}:${state.name}] $message"
                println(log)
                setState {
                    chatHistory.add(log)
                }
                state.ws.launch {
                    state.ws.send(ChatRequest.MessageRequest(message))
                }
            }
        }
    }
}

