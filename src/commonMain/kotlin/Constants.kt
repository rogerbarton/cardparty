package common

const val ApiVersion = 1

enum class StatusCode(val status: Int)
{
    Success(0),
    ServerError(1)
    {
        override fun toString() = "Server Error"
    },
    InvalidRequestType(2),
    InvalidRequest(3),
    AlreadyInAParty(12)
    {
        override fun toString() = "Already in a party"
    },
    NotInAParty(13)
    {
        override fun toString() = "Not in a party"
    },
    InvalidPartyCode(14)
    {
        override fun toString() = "Invalid party code"
    },
    NotHost(15)
    {
        override fun toString() = "You are not the host"
    },
}