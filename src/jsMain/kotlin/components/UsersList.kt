package components

import kotlinx.browser.document
import kotlinx.html.BUTTON
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.*

external interface UsersListProps : RProps
{
    var thisUser: Int
    var users: MutableMap<Int, String>
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
                if (props.thisUser != user.key)
                    +"${user.key}. ${user.value}"
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
