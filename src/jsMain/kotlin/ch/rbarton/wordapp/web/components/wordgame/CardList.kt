package ch.rbarton.wordapp.web.components.wordgame

import ch.rbarton.wordapp.common.data.Card
import ch.rbarton.wordapp.common.data.colors
import ch.rbarton.wordapp.web.components.external.icon
import kotlinx.css.Color
import kotlinx.css.Overflow
import kotlinx.css.backgroundColor
import kotlinx.css.overflowX
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import react.RProps
import react.dom.*
import react.fc
import styled.css
import styled.styledDiv


external interface CardListProps : RProps
{
    var title: String
    var colorId: Int
    var cards: Map<Int, Card>
    var onAddCard: ((String) -> Unit)?
    var onRemoveCard: ((Int) -> Unit)?
    var onRemoveCategory: (() -> Unit)?
}

val CardList = fc<CardListProps> { props ->
    h3 {
        +props.title
        if (props.onRemoveCategory != null)
            button(classes = "btn btn-outline-secondary btn-sm mx-2") {
                icon("delete")
                attrs.onClickFunction = { props.onRemoveCategory!!() }
            }
    }

    if (props.onAddCard != null)
    {
        child(AddItem) {
            attrs.typeName = "Card"
            attrs.onSubmit = props.onAddCard!!
        }
        br {}
    }

    styledDiv {
        attrs.classes = setOf("row flex-row flex-nowrap mb-2")
        css { overflowX = Overflow.auto }
        for ((cardId, card) in props.cards)
        {
            div(classes = "col-sm-4") {
                styledDiv {
                    attrs.classes = setOf("card")
                    css { backgroundColor = Color(colors[props.colorId]) }

                    div(classes = "card-body") {
                        h5(classes = "card-title") { +card.text }
                        p(classes = "card-text") {
                            +card.userId.toString()
                            if (props.onRemoveCard != null)
                                button(classes = "btn btn-outline-secondary btn-sm float-end") {
                                    icon("delete")
                                    attrs.onClickFunction = { props.onRemoveCard!!(cardId) }
                                }
                        }
                    }
                }
            }
        }
    }
    hr {}
}
