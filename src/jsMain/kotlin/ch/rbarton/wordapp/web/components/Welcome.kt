package ch.rbarton.wordapp.web.components

import react.RProps
import react.dom.h1
import react.fc


external interface WelcomeProps : RProps
{
    var userCount: Int
    var partyCount: Int
}

val Welcome = fc<WelcomeProps> { props ->
    h1 {
        +"Welcome to the word game!"
    }

    +"Hi, there are ${props.userCount} people and ${props.partyCount} parties here."
//    +"Hi, there are "; b { +"${props.userCount}" }; +"people and "; b { +"${props.partyCount}" }; +" parties here."
//    reactMarkdown {
//        attrs.children = "Hi, there are *${props.userCount}* people and **${props.partyCount}** parties here."
//    }
}