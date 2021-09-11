package ch.rbarton.wordapp.web.components.external

import kotlinx.css.LinearDimension
import kotlinx.css.fontSize
import kotlinx.html.classes
import react.RBuilder
import styled.css
import styled.styledSpan

/**
 * Add a material design icon.
 * See https://google.github.io/material-design-icons/
 *
 * @param classes
 * - Size: md-18, md-24 (*), md-36, md-48
 * - Tint: md-dark, md-light, md-inactive
 */
fun RBuilder.icon(name: String, classes: String = "align-text-bottom mx-1", size: String = "18px")
{
    styledSpan {
        attrs.classes = setOf("material-icons", classes)
        if (size.isNotEmpty())
        {
            css {
                fontSize = LinearDimension(size)
            }
        }
        +name
    }
}