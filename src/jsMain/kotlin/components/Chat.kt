package components

import kotlinx.css.*
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.*
import styled.css
import styled.styledDiv

external interface ChatProps : RProps
{
    var onSubmit: (String) -> Unit

    var chatHistory: MutableList<String>
}

val Chat = fc<ChatProps> { props ->
    val (inputText, setInputText) = useState("")

    h2 {
        +"Chat"
    }

    styledDiv {
        css {
            maxHeight = LinearDimension("300px")
            overflow = Overflow.scroll
            display = Display.flex
            flexDirection = FlexDirection.columnReverse
        }

        ul (classes = "list-group"){
            for (item in props.chatHistory)
            {
                li(classes = "list-group-item") {
                    +item
                }
            }
        }
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