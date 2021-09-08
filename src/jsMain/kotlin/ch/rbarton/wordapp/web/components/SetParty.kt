package ch.rbarton.wordapp.web.components

import ch.rbarton.wordapp.common.request.StatusCode
import ch.rbarton.wordapp.common.request.StatusResponse
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import kotlinx.html.role
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.RProps
import react.dom.*
import react.fc
import react.useState

external interface SetPartyProps : RProps
{
    var partyCode: String?
    var onCreateParty: (Event) -> Unit
    var onJoinParty: (String) -> Unit
    var lastStatus: StatusResponse?
    var onDismissLastStatus: (Event) -> Unit
}

val SetParty = fc<SetPartyProps> { props ->
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