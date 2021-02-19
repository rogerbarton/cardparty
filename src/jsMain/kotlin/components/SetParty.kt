package components

import kotlinx.html.InputType
import kotlinx.html.js.*
import react.*
import react.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.HTMLInputElement

external interface SetPartyProps : RProps
{
    var partyCode: String?
    var onCreateParty: (Event) -> Unit
    var onJoinParty: (String) -> Unit
}

val SetParty = functionalComponent<SetPartyProps> { props ->
    val (inputText, setInputText) = useState("")

    button(classes = "btn btn-primary mb-2 mx-auto d-block shadow") {
        +"Create Party"
        attrs.onClickFunction = props.onCreateParty
    }

    form(classes = "input-group mb-3 shadow") {
        attrs.onSubmitFunction = {
            it.preventDefault()
            if (inputText.isNotEmpty())
                props.onJoinParty(inputText)
        }
        input(InputType.text, classes = "form-control") {
            attrs.onChangeFunction = {
                setInputText((it.target as HTMLInputElement).value)
            }
        }
        button(classes = "btn btn-secondary") {
            +"Join Party"
        }
    }
}