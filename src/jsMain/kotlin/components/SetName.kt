package components

import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import react.RProps
import react.dom.button
import react.dom.form
import react.dom.h2
import react.dom.input
import react.functionalComponent
import react.useState

external interface SetNameProps : RProps
{
    var onSubmit: (String) -> Unit
}

/**
 * Note: this is only used right on the first page when setting the name for the first time
 */
val NameField = functionalComponent<SetNameProps> { props ->
    val (value, setValue) = useState("")

    form(classes = "input-group mb-2") {
        attrs.onSubmitFunction = {
            it.preventDefault()
            if (value.isNotBlank())
                props.onSubmit(value)
        }
        input(InputType.text, classes = "form-control") {
            attrs.onChangeFunction = {
                setValue((it.target as HTMLInputElement).value)
            }
            attrs.autoFocus = true
        }
        button(type = ButtonType.submit, classes = "btn btn-primary btn-sm") {
            icon("check"); +"Ok"
            attrs.disabled = value.isBlank()
        }
    }
}
