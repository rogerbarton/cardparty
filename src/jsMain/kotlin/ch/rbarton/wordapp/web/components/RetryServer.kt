package ch.rbarton.wordapp.web.components

import ch.rbarton.wordapp.web.components.external.icon
import kotlinx.html.InputType
import kotlinx.html.id
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

/**
 * Component to set server ip address and port, or to retry the connection.
 */

external interface SetServerProps : RProps
{
    var address: String
    var port: String
    var onRetry: (Event) -> Unit
    var onNewAddress: (String, Int) -> Unit
}

val RetryServer = fc<SetServerProps> { props ->
    val (addressText, setAddressText) = useState(props.address)
    val (portText, setPortText) = useState(props.port)

    button(classes = "btn btn-primary my-3 mx-auto d-block shadow") {
        icon("refresh"); +"Try Again"
        attrs.onClickFunction = props.onRetry
    }

    a(classes = "mb-2 mx-auto") {
        icon("construction"); +"Advanced"
        attrs.href = "#configureServer"
        attrs.role = "button"
        attrs["data-bs-toggle"] = "collapse"
    }

    div(classes = "collapse") {
        attrs.id = "configureServer"
        h2 { +"Server Address" }
        form(classes = "input-group mb-3") {
            attrs.onSubmitFunction = {
                it.preventDefault()
                val portInt = portText.toIntOrNull()
                if (portInt != null)
                    props.onNewAddress(addressText, portInt)
            }
            input(InputType.text, classes = "form-control") {
                attrs.onChangeFunction = {
                    setAddressText((it.target as HTMLInputElement).value)
                }
                attrs.autoFocus = true
                attrs.placeholder = "IP Address"
                attrs.defaultValue = props.address
            }
            span(classes = "input-group-text") { +":" }
            input(InputType.text, classes = "form-control") {
                attrs.onChangeFunction = {
                    setPortText((it.target as HTMLInputElement).value)
                }
                attrs.autoFocus = true
                attrs.placeholder = "Port"
                attrs.defaultValue = props.port
            }
            button(classes = "btn btn-primary") {
                icon("play_arrow"); +"Connect"
                attrs.disabled = addressText.isEmpty() || portText.isEmpty()
            }
        }
    }
}