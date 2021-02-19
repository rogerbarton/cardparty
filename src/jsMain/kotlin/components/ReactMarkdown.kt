@file:JsModule("react-markdown")
@file:JsNonModule

package components

import react.*

external interface ReactMarkdownProps : RProps {
    var children: String
}

@JsName("ReactMarkdown")
external val reactMarkdown: RClass<ReactMarkdownProps>