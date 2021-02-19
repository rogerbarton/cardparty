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
    SetName,
    SetParty,
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
        phase = AppPhase.SetName

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

            if (state.partyCode != null)
            {
                button(classes = "btn btn-secondary mb-2") {
                    +"Leave Party"
                    attrs.onClickFunction = {
                        state.webSocketSession.launch {
                            println("LeaveParty")
                            state.webSocketSession.send(ActionType.LeaveParty)
                        }
                        setState {
                            partyCode = null
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

