package ch.rbarton.wordapp.web

import ch.rbarton.wordapp.common.client.data.ConnectionData
import ch.rbarton.wordapp.common.client.data.Party
import ch.rbarton.wordapp.common.connection.launchSend
import ch.rbarton.wordapp.common.data.GameStage
import ch.rbarton.wordapp.common.data.PartyMode
import ch.rbarton.wordapp.common.data.UserInfo
import ch.rbarton.wordapp.common.request.*
import ch.rbarton.wordapp.web.components.*
import ch.rbarton.wordapp.web.components.external.icon
import ch.rbarton.wordapp.web.components.wordgame.AddCategory
import ch.rbarton.wordapp.web.components.wordgame.CardList
import ch.rbarton.wordapp.web.receive.onBaseRequestReceived
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.css.LinearDimension
import kotlinx.css.fontFamily
import kotlinx.css.fontSize
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.*
import react.dom.*
import styled.css
import styled.styledSpan
import ch.rbarton.wordapp.common.request.Chat as ChatRequest
import ch.rbarton.wordapp.common.request.Party as PartyRequest
import ch.rbarton.wordapp.common.request.UserInfo as UserInfoRequest

external interface AppState : State
{
    var connection: ConnectionData
    var userInfo: UserInfo
    var party: Party?

    var globalUserCount: Int
    var globalPartyCount: Int

    var chatHistory: MutableList<ChatItem>
    var lastPartyStatus: StatusResponse?

    var httpClient: HttpClient
    var isAttemptingConnection: Boolean
    var ws: WebSocketSession?
}

class App : RComponent<RProps, AppState>()
{
    override fun AppState.init()
    {
        connection = ConnectionData()
        userInfo = UserInfo()

        chatHistory = mutableListOf()

        httpClient = HttpClient {
            install(WebSockets)
        }

        launchWebsocket()
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun launchWebsocket() = GlobalScope.launch {
        setState { isAttemptingConnection = true }
        try
        {
            val newWs = state.httpClient.webSocketSession(
                method = HttpMethod.Get,
                host = state.connection.serverAddress,
                port = state.connection.serverPort,
                path = "/"
            ) {
                println("WebSocket started")
            }
            setState {
                ws = newWs
            }
            receiveWebsocketFrames()
        }
        catch (e: WebSocketException)
        {
            println("Failed to connect websocket: ${e.message}")
        }
        setState { isAttemptingConnection = false }
    }

    override fun RBuilder.render()
    {
        if (state.ws == null)
        {
            renderConnecting()
            return
        }

        if (state.userInfo.name.isEmpty())
        {
            renderWelcome()
            renderSetName()
            return
        }

        if (state.party == null)
        {
            renderWelcome()
            renderSetParty()
        }
        else
        {
            when (state.party!!.mode)
            {
                PartyMode.Idle -> renderPartyIdle()
                PartyMode.WordGame ->
                {
                    h1(classes = "mt-2") {
                        +"Word Game"
                        renderPartyCode()
                    }
                    when (state.party!!.stateShared!!.stage)
                    {
                        GameStage.Setup -> renderWordGameSetup()
                        GameStage.Playing -> renderWordGamePlaying()
                    }
                }
            }
            renderChat()
        }
    }

    private fun RBuilder.renderConnecting()
    {
        if (state.isAttemptingConnection)
        {
            div(classes = "d-flex align-items-center justify-content-center m-3") {
                div(classes = "spinner-border text-primary mx-3") { }
                b(classes = "fs-3") { +"Connecting..." }
            }
        }
        else
        {
            child(RetryServer) {
                attrs.address = state.connection.serverAddress
                attrs.port = state.connection.serverPort.toString()
                attrs.onRetry = {
                    launchWebsocket()
                }
                attrs.onNewAddress = { address, port ->
                    setState {
                        connection.serverAddress = address
                        connection.serverPort = port
                    }
                    launchWebsocket()
                }
            }
        }
    }

    private fun RBuilder.renderWelcome()
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

    private fun RBuilder.renderSetName()
    {
        h2 {
            +"Choose a Name"
        }
        child(NameField) {
            attrs.onSubmit = { newName ->
                println("setname: $newName")
                state.ws!!.launchSend(UserInfoRequest.SetNameRequest(newName)) { response ->
                    if (response is StatusResponse && response.status == StatusCode.Success)
                        setState { userInfo.name = newName }
                    else
                        onBaseRequestReceived(response)
                }
            }
        }
    }

    private fun RBuilder.renderSetParty()
    {
        child(SetParty) {
            attrs.partyCode = state.party?.code
            attrs.lastStatus = state.lastPartyStatus
            attrs.onDismissLastStatus = { setState { lastPartyStatus = null } }
            attrs.onCreateParty = {
                println("CreateParty")
                setState { lastPartyStatus = null }
                state.ws!!.launchSend(ActionType.PartyCreate) { response ->
                    when (response)
                    {
                        is PartyRequest.CreateResponse ->
                        {
                            setState {
                                party = Party(
                                    mutableMapOf(state.connection.userId!! to state.userInfo),
                                    response.partyCode,
                                    state.connection.userId!!
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
                        else -> onBaseRequestReceived(response)
                    }
                }
            }
            attrs.onJoinParty = { value ->
                println("JoinParty $value")
                setState { lastPartyStatus = null }
                state.ws!!.launchSend(PartyRequest.JoinRequest(value)) { response ->
                    when (response)
                    {
                        is PartyRequest.JoinResponse ->
                        {
                            setState {
                                party = Party(response.partyBase, response.userToNames.toMutableMap())
                                if (response.newColorId != null)
                                    userInfo.colorId = response.newColorId
                            }
                            println("Joined party with ${state.party!!.users.size} users")
                        }
                        is StatusResponse ->
                        {
                            setState {
                                lastPartyStatus = response
                            }
                        }
                        else -> onBaseRequestReceived(response)
                    }
                }
            }
        }
    }

    private fun RBuilder.renderPartyIdle()
    {
        h1(classes = "mt-2") {
            +"Yolo"
            renderPartyCode()
        }

        strong { +"Select a game mode" }
        div(classes = "row mb-2") {
            div(classes = "col-sm-6") {
                div(classes = "card") {
                    div(classes = "card-body") {
                        h5(classes = "card-title") { +"Word Game" }
                        p(classes = "card-text") { +"Write your own cards and shuffle them between players." }
                        button(classes = "btn btn-primary") {
                            +"Play"
                            attrs.disabled = state.party!!.hostId != state.connection.userId
                            attrs.onClickFunction = {
                                state.ws!!.launchSend(PartyOptions.SetPartyModeRequest(PartyMode.WordGame))
                            }
                        }
                    }
                }
            }
        }

        renderUsersList()
    }

    private fun RBuilder.renderWordGameSetup()
    {
        renderUsersList()

//        child(components.GameSettings::class) {
//            attrs.editable = true
//            attrs.settings = state.party!!.state.settings
//            attrs.onSubmit = { newSettings ->
//                state.ws!!.launch {
//                    state.ws!!.send(WordGame.SetGameSettingsJson(newSettings))
//                }
//            }
//        }

        for ((categoryId, category) in state.party!!.stateShared!!.categories)
        {
            child(CardList) {
                attrs.category = category
                attrs.cards = state.party!!.stateShared!!.cards.filter { it.value.categoryId == categoryId }
                attrs.onRemoveCard = { cardId ->
                    state.ws!!.launchSend(WordGame.RemoveCardRequest(cardId))
                }
            }
        }

        child(AddCategory) {
            attrs.onAddCategory = { category ->
                state.ws!!.launchSend(WordGame.AddCategoryRequest(category))
            }
        }
    }

    private fun RBuilder.renderWordGamePlaying()
    {

    }

    private fun RBuilder.renderPartyCode()
    {
        styledSpan {
            attrs.classes = setOf("badge bg-primary float-end")
            icon("vpn_key", classes = "align-top me-2", size = "24px")
            +state.party!!.code
            css {
                fontFamily = "'Roboto Mono', monospace"
                fontSize = LinearDimension("24px")
            }
        }

        button(classes = "btn btn-secondary btn-sm float-end mx-2") {
            icon("logout"); +"Leave"
            attrs.onClickFunction = {
                println("LeaveParty")
                state.ws!!.launchSend(ActionType.PartyLeave) { response ->
                    if (response is StatusResponse && response.status == StatusCode.Success)
                    {
                        setState {
                            party = null
                            lastPartyStatus = null
                            chatHistory.clear()
                        }
                    }
                    else
                        onBaseRequestReceived(response)
                }
            }
        }
    }

    private fun RBuilder.renderUsersList()
    {
        child(usersList)
        {
            attrs.thisUserId = state.connection.userId!!
            attrs.users = state.party!!.users
            attrs.host = state.party!!.hostId
            attrs.onSetName = { newName ->
                state.ws!!.launchSend(UserInfoRequest.SetNameRequest(newName)) { response ->
                    if (response is StatusResponse && response.status == StatusCode.Success)
                    {
                        setState {
                            userInfo.name = newName
                            party!!.users[connection.userId!!]!!.name = newName
                        }
                    }
                    else
                        onBaseRequestReceived(response)
                }
            }
        }
    }

    private fun RBuilder.renderChat()
    {
        child(Chat) {
            attrs.chatHistory = state.chatHistory
            attrs.onSubmit = { message ->
                val log = "[${state.connection.userId}:${state.userInfo.name}] $message"
                println(log)
                setState {
                    chatHistory.add(log, MessageType.Chat)
                }
                state.ws!!.launchSend(ChatRequest.MessageRequest(message))
            }
        }
    }
}

