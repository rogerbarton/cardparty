package ch.rbarton.wordapp.web.components

import ch.rbarton.wordapp.web.components.external.icon
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import react.RProps
import react.dom.*
import react.fc
import react.useState

external interface UsersListProps : RProps
{
    var thisUser: Int
    var users: MutableMap<Int, String>
    var host: Int
    var onSetName: (String) -> Unit
}

val usersList = fc<UsersListProps> { props ->
    val (isEditingName, setIsEditingName) = useState(false)
    val (nameInput, setNameInput) = useState(props.users[props.thisUser]!!)

    val isNameValid = { name: String -> name.trim() != props.users[props.thisUser] && name.isNotBlank() }

    ul {
        for (user in props.users)
        {
            li {
                if (user.key != props.thisUser)
                {
                    +"${user.key}. ${user.value}"
                    if (user.key == props.host)
                        icon("star_border")
                }
                else
                {
                    if (isEditingName)
                    {
                        form(classes = "input-group") {
                            attrs.onSubmitFunction = {
                                it.preventDefault()
                                if (isNameValid(nameInput))
                                    props.onSetName(nameInput.trim())
                                setIsEditingName(false)
                            }
                            input(type = InputType.text, classes = "form-control") {
                                attrs.onChangeFunction = {
                                    setNameInput((it.target as HTMLInputElement).value)
                                }
                                attrs.autoFocus = true
                            }
                            button(type = ButtonType.submit, classes = "btn btn-primary btn-sm") {
                                if (isNameValid(nameInput))
                                {
                                    icon("check", size = "18px"); +"Apply"
                                }
                                else
                                {
                                    icon("close", size = "18px"); +"Cancel"
                                }
                            }
                        }
                    }
                    else
                    {
                        +"${user.key}. ${user.value}"
                        if (user.key == props.host)
                            icon("star_border")
                        button(classes = "btn btn-outline-secondary btn-sm ms-3") {
                            icon("mode_edit", size = "18px")
                            +"Edit"
                            attrs.onClickFunction = {
                                setIsEditingName(true)
                            }
                        }
                    }
                }
            }
        }
    }
}
