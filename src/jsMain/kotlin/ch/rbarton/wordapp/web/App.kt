package ch.rbarton.wordapp.web

import ch.rbarton.wordapp.web.components.*
import ch.rbarton.wordapp.common.*

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.*

data class Party(
    val code: String,
    var users: MutableMap<Int, String>,
    var host: Int,
    var state: GameState
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
    var lastPartyStatus: StatusJson?

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
                    state.ws.send(SetNameJson(state.name))
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
                    state.ws.send(ActionType.CreateParty) { response ->
                        when (response)
                        {
                            is CreatePartyResponseJson ->
                            {
                                setState {
                                    party = Party(
                                        response.partyCode,
                                        mutableMapOf(state.guid!! to state.name),
                                        state.guid!!,
                                        GameState()
                                    )
                                }
                                println("Created party with code: ${response.partyCode}")
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
            attrs.onJoinParty = { value ->
                println("JoinParty $value")
                setState { lastPartyStatus = null }
                state.ws.launch {
                    state.ws.send(JoinPartyJson(value)) { response ->
                        when (response)
                        {
                            is JoinPartyResponseJson ->
                            {
                                setState {
                                    party = Party(
                                        value,
                                        response.userToNames.toMutableMap(),
                                        response.host,
                                        response.state
                                    )
                                }
                                println("Joined party with ${state.party!!.users.size} users")
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
            span(classes = "badge bg-primary") { +state.party!!.code }
        }

        child(usersList) {
            attrs.thisUser = state.guid!!
            attrs.users = state.party!!.users
            attrs.onSetName = { newName ->
                state.ws.launch {
                    state.ws.send(SetNameJson(newName)) { response ->
                        if (response is StatusJson && response.status == StatusCode.Success)
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
//                    state.ws.send(SetGameSettingsJson(newSettings))
//                }
//            }
//        }

        button(classes = "btn btn-secondary mb-2") {
            +"Leave ch.rbarton.wordapp.web.Party"
            attrs.onClickFunction = {
                println("LeaveParty")
                state.ws.launch {
                    state.ws.send(ActionType.LeaveParty) { response ->
                        if (response is StatusJson && response.status == StatusCode.Success)
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
                    state.ws.send(ChatJson(message))
                }
            }
        }
    }
}

