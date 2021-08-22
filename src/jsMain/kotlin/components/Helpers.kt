package components

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
fun RBuilder.icon(name: String, classes: String = "", size: String = "18px")
{
    styledSpan {
        attrs.classes = mutableSetOf("material-icons $classes", "align-top me-1")
        if(size.isNotEmpty())
        {
            css {
                fontSize = LinearDimension(size)
            }
        }
        +name
    }
}