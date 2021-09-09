package ch.rbarton.wordapp.web

import ch.rbarton.wordapp.common.client.data.ConnectionData
import ch.rbarton.wordapp.common.client.data.Party
import ch.rbarton.wordapp.common.connection.send
import ch.rbarton.wordapp.common.request.ActionType
import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.common.request.StatusResponse
import ch.rbarton.wordapp.common.request.UserInfo
import ch.rbarton.wordapp.web.components.Chat
import ch.rbarton.wordapp.web.components.NameField
import ch.rbarton.wordapp.web.components.SetParty
import ch.rbarton.wordapp.web.components.usersList
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
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

external interface AppState : State
{
    var connection: ConnectionData
    var party: Party?

    var globalUserCount: Int
    var globalPartyCount: Int

    var chatHistory: MutableList<String>
    var lastPartyStatus: StatusResponse?

    var httpClient: HttpClient
    var ws: WebSocketSession
}

class App : RComponent<RProps, AppState>()
{
    @DelicateCoroutinesApi
    override fun AppState.init()
    {
        connection = ConnectionData()

        chatHistory = mutableListOf()

        httpClient = HttpClient {
            install(WebSockets)
        }

        GlobalScope.launch {
            ws = httpClient.webSocketSession(
                method = HttpMethod.Get,
                host = connection.serverAddress,
                port = connection.serverPort,
                path = "/"
            ) {
                println("WebSocket running...")
            }
            receiveWebsocketFrames()
        }
    }

    override fun RBuilder.render()
    {
        if (state.connection.name.isEmpty())
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
        h1 {
            +"Welcome to the word game!"
        }

        +"Hi, there are ${state.globalUserCount} people and ${state.globalPartyCount} parties here."
        // TODO: integrate md
//        +"Hi, there are "; b { +"${props.userCount}" }; +"people and "; b { +"${props.partyCount}" }; +" parties here."
//        reactMarkdown {
//            attrs.children = "Hi, there are *${props.userCount}* people and **${props.partyCount}** parties here."
//        }
    }

    private fun RBuilder.setName()
    {
        h2 {
            +"Choose a Name"
        }
        child(NameField) {
            attrs.onSubmit = { value ->
                setState {
                    connection.name = value
                }
                state.ws.launch {
                    println("setname: ${state.connection.name}")
                    state.ws.send(UserInfo.SetNameRequest(state.connection.name))
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
                                        mutableMapOf(0 to state.connection.name),
                                        response.partyCode,
                                        state.connection.guid!!
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
                                        response.partyBase,
                                        response.userToNames.toMutableMap()
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
            attrs.thisUser = state.connection.guid!!
            attrs.users = state.party!!.users
            attrs.host = state.party!!.hostGuid
            attrs.onSetName = { newName ->
                state.ws.launch {
                    state.ws.send(UserInfo.SetNameRequest(newName)) { response ->
                        if (response is StatusResponse && response.status == StatusCode.Success)
                        {
                            setState {
                                connection.name = newName
                                party!!.users[connection.guid!!] = newName
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
            +"Leave Party"
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
                val log = "[${state.connection.guid}:${state.connection.name}] $message"
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

