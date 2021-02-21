package common

import kotlinx.serialization.*

/**
 * See https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md
 */

@Serializable
sealed class BaseJson(var requestId: Int? = null, @Required val version: Int? = ApiVersion)

expect fun genRequestId(): Int

/**
 * All actions that we can send which do not have any data associated with them, i.e. an RPC.
 */
enum class ActionType
{
    CreateParty,
    LeaveParty,
}

@Serializable
data class ActionJson(val action: ActionType) : BaseJson()

@Serializable
data class StatusJson(val status: StatusCode, val message: String? = null) : BaseJson()


// --- User Info
@Serializable
data class InitJson(val guid: Int, val userCount: Int, val parties: Map<String, Int>) : BaseJson()

@Serializable
data class SetNameJson(val name: String) : BaseJson()

@Serializable
data class SetNameBroadcastJson(val userId: Int, val name: String) : BaseJson()


// --- Party membership
@Serializable
data class CreatePartyResponseJson(val partyCode: String) : BaseJson()

@Serializable
data class JoinPartyJson(val partyCode: String) : BaseJson()

@Serializable
data class JoinPartyResponseJson(val userToNames: Map<Int, String>, val host: Int, val state: GameState) : BaseJson()

@Serializable
data class JoinPartyBroadcastJson(val userId: Int, val name: String) : BaseJson()

@Serializable
data class LeavePartyBroadcastJson(val userId: Int, val newHost: Int?) : BaseJson()


// --- Chat
@Serializable
data class ChatJson(val message: String) : BaseJson()

@Serializable
data class ChatBroadcastJson(val userId: Int, val message: String) : BaseJson()


// -- FunEmployed
@Serializable
data class SetGameSettingsJson(val settings: GameSettings) : BaseJson()

@Serializable
data class GameSettings(
    var cardsPerPlayer: Int = 4,
//    var intervieweeCount: Int = 1,
//    var interviewerCount: Int = 1,
//    var interviewCategories: MutableSet<String> = mutableSetOf(),
//    var intervieweeCategories: MutableSet<String> = mutableSetOf(),
)

@Serializable
data class SetGameStageJson(val stage: GameState.Stage) : BaseJson()

@Serializable
data class AddWordJson(val value: String) : BaseJson()

@Serializable
data class AddWordBroadcastJson(val value: String) : BaseJson()