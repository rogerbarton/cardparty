package components

import react.*
import react.dom.h1


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