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
import react.dom.button

enum class AppPhase
{
    NoParty,
    Lobby,
}

external interface AppState : RState
{
    var phase: AppPhase

    var globalUserCount: Int
    var globalPartyCount: Int

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
        phase = AppPhase.NoParty

        globalUserCount = 0
        globalPartyCount = 0

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
        child(components.Welcome) {
            attrs.userCount = state.globalUserCount
            attrs.partyCount = state.globalPartyCount
        }

        if(state.name.isEmpty())
        {
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
                attrs.clearOnSubmit = false
            }
        }
        else
        {
            if (state.partyCode == null)
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
                        state.webSocketSession.launch {
                            println("JoinParty $value")
                            state.webSocketSession.send(JoinPartyJson(value)) { response ->
                                when(response) {
                                    is JoinPartyResponseJson -> {
                                        setState {
                                            users = response.userToNames.toMutableMap()
                                            partyCode = value
                                        }
                                        println("Joined party with ${state.users!!.size} users: ${state.users!!.values.joinToString(", ")}")
                                    }
                                    else -> handleUnidentifiedResponse(response)
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                button(classes = "btn btn-secondary mb-2") {
                    +"Leave Party"
                    attrs.onClickFunction = {
                        state.webSocketSession.launch {
                            println("LeaveParty")
                            state.webSocketSession.send(ActionType.LeaveParty) { response ->
                                if(response is StatusJson && response.status == StatusCode.Success)
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
}

