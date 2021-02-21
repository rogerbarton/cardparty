package components

import common.StatusCode
import common.StatusJson
import kotlinx.html.InputType
import kotlinx.html.js.*
import kotlinx.html.role
import react.*
import react.dom.*
import org.w3c.dom.events.Event
import org.w3c.dom.HTMLInputElement

external interface SetPartyProps : RProps
{
    var partyCode: String?
    var onCreateParty: (Event) -> Unit
    var onJoinParty: (String) -> Unit
    var lastStatus: StatusJson?
    var onDismissLastStatus: (Event) -> Unit
}

val SetParty = functionalComponent<SetPartyProps> { props ->
    val (inputText, setInputText) = useState("")

    button(classes = "btn btn-secondary mb-2 mx-auto d-block shadow") {
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
            attrs.autoFocus = true
        }
        button(classes = "btn btn-primary") {
            +"Join Party"
        }
    }
    if (props.lastStatus != null && props.lastStatus!!.status != StatusCode.Success)
    {
        div(classes = "alert alert-warning fade show") {
            attrs.role = "alert"
            strong(classes = "me-auto") { +props.lastStatus!!.status.name }
            if (props.lastStatus!!.message != null)
                +props.lastStatus!!.message!!
            button(classes = "btn-close") {
                attrs.onClickFunction = props.onDismissLastStatus
            }
        }
    }
}