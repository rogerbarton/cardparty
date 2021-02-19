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

    button(classes = "btn btn-primary") {
        +"Create Party"
        attrs.onClickFunction = props.onCreateParty
    }

    form(classes = "input-group mb-3") {
        input(InputType.text, classes = "form-control") {
            attrs.onChangeFunction = {
                setInputText((it.target as HTMLInputElement).value)
            }
            attrs.onSubmitFunction = {
                it.preventDefault()
                props.onJoinParty(inputText)
            }
        }
        button(classes = "btn btn-outline-secondary") {
            +"Join Party"
            attrs.onClickFunction = {
                it.preventDefault()
                props.onJoinParty(inputText)
            }
        }
    }
}