package ch.rbarton.wordapp.web.components

import ch.rbarton.wordapp.web.components.external.icon
import kotlinx.css.*
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import react.RProps
import react.dom.*
import react.fc
import react.useState
import styled.css
import styled.styledDiv
import styled.styledLi

data class ChatItem(val message: String, val type: MessageType = MessageType.Status)

fun MutableList<ChatItem>.add(message: String, type: MessageType = MessageType.Status) = add(ChatItem(message, type))
fun MutableList<ChatItem>.add(message: String) = add(ChatItem(message))

enum class MessageType
{
    Chat
    {
        override fun toColor() = "azure"
    },
    Status
    {
        override fun toColor() = "lightgray"
    },
    Debug
    {
        override fun toColor() = "cornsilk"
    };

    abstract fun toColor(): String
}

external interface ChatProps : RProps
{
    var onSubmit: (String) -> Unit

    var chatHistory: MutableList<ChatItem>
}

val Chat = fc<ChatProps> { props ->
    val (inputText, setInputText) = useState("")

    h2(classes = "mt-2") {
        +"Chat"
    }

    styledDiv {
        css {
            maxHeight = LinearDimension("300px")
            overflowY = Overflow.scroll
            display = Display.flex
            flexDirection = FlexDirection.columnReverse
        }

        ul(classes = "list-group") {
            for (item in props.chatHistory)
            {
                styledLi {
                    attrs.classes = setOf("list-group-item")
                    css { backgroundColor = Color(item.type.toColor()) }
                    +item.message
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
            icon("play_arrow"); +"Send"
            attrs.disabled = inputText.isBlank()
        }
    }
}