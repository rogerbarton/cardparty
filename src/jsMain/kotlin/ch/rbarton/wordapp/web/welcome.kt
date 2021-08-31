package ch.rbarton.wordapp.web

import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.*
import styled.*

external interface WelcomeProps : RProps
{
    var name: String
}

data class WelcomeState(val name: String) : State

@ExperimentalJsExport
class Welcome(props: WelcomeProps) : RComponent<WelcomeProps, WelcomeState>(props)
{

    init
    {
        state = WelcomeState(props.name)
    }

    override fun RBuilder.render()
    {
        styledDiv {
            css {
                +WelcomeStyles.textContainer
            }
            +"Hello, ${state.name}"
        }
        styledInput {
            css {
                +WelcomeStyles.textInput
            }
            attrs {
                type = InputType.text
                value = state.name
                onChangeFunction = { event ->
                    setState(
                        WelcomeState(name = (event.target as HTMLInputElement).value)
                    )
                }
            }
        }
    }
}
