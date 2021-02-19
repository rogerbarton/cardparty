package components

import common.ChatJson
import common.send
import kotlinx.coroutines.launch
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.button
import react.dom.form
import react.dom.h2
import react.dom.input

external interface ChatProps : RProps
{
    var onSubmit: (String) -> Unit
}

val Chat = functionalComponent<ChatProps> { props ->
    val (inputText, setInputText) = useState("")


    h2 {
        +"Chat"
    }
    form(classes = "input-group mb-3 shadow") {
        attrs.onSubmitFunction = {
            it.preventDefault()
            if (inputText.isNotEmpty())
            {
                props.onSubmit(inputText)
                setInputText("")
            }
        }
        input(InputType.text, classes = "form-control") {
            attrs.onChangeFunction = {
                setInputText((it.target as HTMLInputElement).value)
            }
            attrs.value = inputText
        }
        button(classes = "btn btn-secondary") {
            +"Send"
        }
    }
}