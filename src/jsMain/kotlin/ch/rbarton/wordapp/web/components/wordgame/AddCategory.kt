package ch.rbarton.wordapp.web.components.wordgame

import ch.rbarton.wordapp.web.components.external.icon
import kotlinx.html.ButtonType
import kotlinx.html.InputType
import kotlinx.html.classes
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onSubmitFunction
import org.w3c.dom.HTMLInputElement
import react.RProps
import react.dom.button
import react.dom.form
import react.dom.input
import react.fc
import react.useState

external interface AddCategoryProps : RProps
{
    var onAddCategory: (String) -> Unit
}

val AddCategory = fc<AddCategoryProps> { props ->
    val (showInput, setShowInput) = useState(false)
    val (categoryInput, setCategoryInput) = useState("")

    if (!showInput)
    {
        button(classes = "btn btn-outline-secondary ms-2") {
            icon("add"); +"Add Category"
            attrs.onClickFunction = {
                setShowInput(true)
            }
        }
    }
    else
    {
        form(classes = "input-group") {
            attrs.onSubmitFunction = {
                it.preventDefault()
                if (categoryInput.isNotBlank())
                    props.onAddCategory(categoryInput.trim())
                setCategoryInput("")
                setShowInput(false)
            }
            input(type = InputType.text, classes = "form-control") {
                attrs.onChangeFunction = {
                    setCategoryInput((it.target as HTMLInputElement).value)
                }
                attrs.autoFocus = true
            }
            button(type = ButtonType.submit, classes = "btn btn-sm") {
                if (categoryInput.isNotBlank())
                {
                    attrs.classes += "btn-primary"
                    icon("check", size = "18px"); +"Add Category"
                }
                else
                {
                    attrs.classes += "btn-secondary"
                    icon("close", size = "18px"); +"Cancel"
                }
            }
        }
    }
}