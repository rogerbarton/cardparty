package components

import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import react.*
import react.dom.button
import react.dom.form
import react.dom.input
import react.dom.label

external interface GameSettingsProps : RProps
{
    val editable: Boolean
    val onSubmit: (Event) -> Unit
}

val gameSettings = functionalComponent<GameSettingsProps> { props ->
    val (cardsPerPerson, setCardsPerPerson) = useState(4)

    form(classes = "form  mb-3") {
        attrs.onSubmitFunction = props.onSubmit
        label(classes = "form-label") { +"Cards per Person: $cardsPerPerson" }
        input(type = InputType.range, classes = "form-range") {
            attrs.min = "1"
            attrs.max = "10"
            attrs.step = "1"
            attrs.onChangeFunction = { setCardsPerPerson((it.target as HTMLInputElement).value.toInt()) }
            attrs.disabled = props.editable
        }
        button(type = ButtonType.submit, classes = "btn btn-primary") { +"Apply" }
    }
}