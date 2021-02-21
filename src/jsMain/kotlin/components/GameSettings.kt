package components

import common.GameSettings
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.button
import react.dom.form
import react.dom.input
import react.dom.label

external interface GameSettingsProps : RProps
{
    var editable: Boolean
    var settings: GameSettings
    var onSubmit: (GameSettings) -> Unit
}

external interface GameSettingsState : RState {
    var settings: GameSettings
}

class GameSettings: RComponent<GameSettingsProps, GameSettingsState>()
{
    override fun RBuilder.render()
    {
        setState {
            settings = props.settings
        }
        form(classes = "form mb-3") {
            attrs.onSubmitFunction = {
                it.preventDefault()
                props.onSubmit(state.settings)
            }
            label(classes = "form-label") { +"Cards per Person: ${state.settings.cardsPerPlayer}" }
            input(type = InputType.range, classes = "form-range") {
                attrs.min = "1"
                attrs.max = "10"
                attrs.step = "1"
                attrs.onChangeFunction = { setState{ settings.cardsPerPlayer = (it.target as HTMLInputElement).value.toInt()} }
                attrs.disabled = props.editable
            }
            button(type = ButtonType.submit, classes = "btn btn-primary") { +"Apply" }
        }
    }
}