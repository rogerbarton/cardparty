@file:JsModule("react-markdown")
@file:JsNonModule

package ch.rbarton.wordapp.web.components.external

import react.ComponentClass
import react.RProps

external interface MarkdownProps : RProps
{
    var items: String
}

@JsName("ReactMarkdown")
external val markdown: ComponentClass<MarkdownProps>