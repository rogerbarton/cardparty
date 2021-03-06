package ch.rbarton.wordapp.web.components

import ch.rbarton.wordapp.common.data.UserInfo
import ch.rbarton.wordapp.common.data.colors
import ch.rbarton.wordapp.web.components.external.icon
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import react.RBuilder
import react.RProps
import react.dom.*
import react.fc
import react.useState
import styled.css
import styled.styledSpan

external interface UsersListProps : RProps
{
    var thisUserId: Int
    var users: MutableMap<Int, UserInfo>
    var host: Int
    var onSetName: (String) -> Unit
}

val usersList = fc<UsersListProps> { props ->
    val (isEditingName, setIsEditingName) = useState(false)
    val (nameInput, setNameInput) = useState(props.users[props.thisUserId]!!.name)

    val isNameValid = { name: String -> name.trim() != props.users[props.thisUserId]?.name && name.isNotBlank() }

    ul(classes = "list-group mb-3") {
        for ((userId, userInfo) in props.users)
        {
            li(classes = "list-group-item") {
                drawUser(userId, userInfo, props)
                if (userId == props.thisUserId)
                    if (!isEditingName)
                    {
                        button(classes = "btn btn-outline-secondary btn-sm float-end ms-2") {
                            icon("mode_edit"); +"Edit"
                            attrs.onClickFunction = { setIsEditingName(true) }
                        }
                    }
                    else
                    {
                        form(classes = "input-group") {
                            attrs.onSubmitFunction = {
                                it.preventDefault()
                                if (isNameValid(nameInput))
                                    props.onSetName(nameInput.trim())
                                setIsEditingName(false)
                            }
                            input(type = InputType.text, classes = "form-control") {
                                attrs.onChangeFunction = { setNameInput((it.target as HTMLInputElement).value) }
                                attrs.autoFocus = true
                                attrs.placeholder = "Name"
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
            }
        }
    }
}

private fun RBuilder.drawUser(
    userId: Int,
    userInfo: UserInfo,
    props: UsersListProps
)
{
    styledSpan {
        attrs.classes = setOf("badge me-2")
        css { backgroundColor = Color(colors.getOrNull(userInfo.colorId) ?: "#000000") }
        +userId.toString()
    }
    +userInfo.name
    if (userId == props.host)
        icon("star_border")
}
