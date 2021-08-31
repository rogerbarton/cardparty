@file:JsModule("react-markdown")
@file:JsNonModule

package ch.rbarton.wordapp.web.components

import react.*

external interface ReactMarkdownProps : RProps {
    var items: String
}

@JsName("ReactMarkdown")
external val reactMarkdown: ComponentClass<ReactMarkdownProps>