package components

import common.ChatJson
import common.send
import kotlinx.coroutines.launch
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.h2
import react.dom.input
import styled.styledForm

external interface ChatProps : RProps {
    var onSubmit: (String) -> Unit
}

val Chat = functionalComponent<ChatProps> { props ->
    val (inputText, setInputText) = useState("")

    styledForm {
        h2 {
            +"Chat"
        }
        attrs.onSubmitFunction = {
            it.preventDefault()
            props.onSubmit(inputText)
            setInputText("")
        }
        input(InputType.text) {
            attrs.onChangeFunction = {
                setInputText((it.target as HTMLInputElement).value)
            }
        }
    }
}