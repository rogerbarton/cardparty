@file:JsModule("react-markdown")
@file:JsNonModule

package components

import react.*

external interface ReactMarkdownProps : RProps {
    var items: String
}

@JsName("ReactMarkdown")
external val reactMarkdown: ComponentClass<ReactMarkdownProps>