import kotlinx.browser.document
import react.dom.*

fun main()
{
    document.bgColor = "yellow"
    render(document.getElementById("root")) {
        child(App::class) {}
    }
}