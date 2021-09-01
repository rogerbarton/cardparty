package ch.rbarton.wordapp.common.request

import ch.rbarton.wordapp.common.data.GameState
import ch.rbarton.wordapp.common.data.PartyOptions
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

/**
 * See https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md
 *
 * NOTE: all serializable data classes deriving from BaseJson **must be in the same file**, due to compiler limitations.
 */

const val ApiVersion = 1

@Serializable
sealed class BaseJson(var requestId: Int? = null, @Required val version: Int? = ApiVersion)

expect fun genRequestId(): Int

@Serializable
data class StatusJson(val status: StatusCode, val message: String? = null) : BaseJson()

@Serializable
data class ActionJson(val action: ActionType) : BaseJson()


//region Party
@Serializable
data class CreatePartyResponseJson(val partyCode: String, val options: PartyOptions) : BaseJson()

@Serializable
data class JoinPartyJson(val partyCode: String) : BaseJson()

@Serializable
data class JoinPartyResponseJson(val userToNames: Map<Int, String>, val host: Int, val state: GameState) : BaseJson()

@Serializable
data class JoinPartyBroadcastJson(val userId: Int, val name: String) : BaseJson()

@Serializable
data class LeavePartyBroadcastJson(val userId: Int, val newHost: Int?) : BaseJson()
//endregion


//region UserInfo
@Serializable
data class InitJson(val guid: Int, val userCount: Int, val parties: Map<String, Int>) : BaseJson()

@Serializable
data class SetNameJson(val name: String) : BaseJson()

@Serializable
data class SetNameBroadcastJson(val userId: Int, val name: String) : BaseJson()
//endregion


//region Chat
@Serializable
data class ChatJson(val message: String) : BaseJson()

@Serializable
data class ChatBroadcastJson(val userId: Int, val message: String) : BaseJson()
//endregion


//region WordGame
@Serializable
data class SetGameSettingsJson(val settings: GameSettings) : BaseJson()

@Serializable
data class GameSettings(
    var cardsPerPlayer: Int = 4,
    var intervieweeCount: Int = 1,
    var interviewerCount: Int = 1,
    var interviewCategories: MutableSet<String> = mutableSetOf(),
    var intervieweeCategories: MutableSet<String> = mutableSetOf(),
)

@Serializable
data class SetGameStageJson(val stage: GameState.Stage) : BaseJson()

@Serializable
data class AddWordJson(val value: String, val category: String) : BaseJson()

@Serializable
data class AddWordBroadcastJson(val value: String, val category: String) : BaseJson()

@Serializable
data class AddCategoryJson(val value: String) : BaseJson()

@Serializable
data class AddCategoryBroadcastJson(val value: String) : BaseJson()
//endregion
