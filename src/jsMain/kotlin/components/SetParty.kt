package components

import common.ActionType
import common.JoinPartyJson
import common.send
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import react.dom.*
import styled.*

external interface SetPartyProps : RProps
{
    var partyCode: String?
    var onCreateParty: (Event) -> Unit
    var onJoinParty: (String) -> Unit
}

val SetParty = functionalComponent<SetPartyProps> { props ->
    val (inputText, setInputText) = useState("")

    styledButton {
        +"Create Party"
        css.classes = mutableListOf("btn", "btn-primary")
        attrs.onClickFunction = props.onCreateParty
    }

    form {
        +"Join Party with Code"
        attrs.onSubmitFunction = {
            it.preventDefault()
            props.onJoinParty(inputText)
        }
        input(InputType.text) {
            attrs.onChangeFunction = {
                setInputText((it.target as HTMLInputElement).value)
            }
        }
    }
}