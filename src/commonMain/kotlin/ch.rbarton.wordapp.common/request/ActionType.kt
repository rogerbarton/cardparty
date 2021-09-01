package ch.rbarton.wordapp.common.request

/**
 * All actions that we can send which do not have any data associated with them, i.e. an RPC.
 */
enum class ActionType
{
    PartyCreate,
    PartyLeave,
}