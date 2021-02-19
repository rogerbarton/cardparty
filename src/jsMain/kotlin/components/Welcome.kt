package components

import react.*
import react.dom.h1

external interface WelcomeProps : RProps {
    var userCount: Int
    var partyCount: Int
}

val Welcome = functionalComponent<WelcomeProps> { props ->
    h1 {
        +"Welcome to the word game!"
    }

    +"Hi, there are ${props.userCount} people and ${props.partyCount} parties here."
}