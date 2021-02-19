package components

import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import react.RProps
import react.dom.form
import react.dom.h2
import react.dom.input
import react.functionalComponent
import react.useState

external interface SetNameProps : RProps {
    var title: String
    var onSubmit: (String) -> Unit
    var clearOnSubmit: Boolean
}

val InputField = functionalComponent<SetNameProps> { props ->
    val (value, setValue) = useState("")

    form(classes = "mb-2") {
        attrs.onSubmitFunction = {
            it.preventDefault()
            props.onSubmit(value)
            if(props.clearOnSubmit)
                setValue("")
        }
        h2 {
            +props.title
        }
        input(InputType.text, classes = "form-control") {
            attrs.onChangeFunction = {
                setValue((it.target as HTMLInputElement).value)
            }
        }
    }
}
