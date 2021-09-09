package ch.rbarton.wordapp.common.request

import ch.rbarton.wordapp.common.data.*
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

/**
 * See https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md
 *
 * NOTE: all serializable data classes deriving from BaseRequest **must be in the same file**, due to compiler limitations.
 */

const val ApiVersion = 1

@Serializable
sealed class BaseRequest(var requestId: Int? = null, @Required val version: Int? = ApiVersion)

expect fun genRequestId(): Int

@Serializable
data class StatusResponse(val status: StatusCode, val message: String? = null) : BaseRequest()

@Serializable
data class ActionRequest(val action: ActionType) : BaseRequest()

@Serializable
data class InitResponse(val guid: Int, val userCount: Int, val parties: Map<String, Int>) : BaseRequest()


class Party
{
    @Serializable
    data class CreateResponse(val partyCode: String) : BaseRequest()

    @Serializable
    data class JoinRequest(val partyCode: String) : BaseRequest()

    @Serializable
    data class JoinResponse(val partyBase: PartyBase, val userToNames: Map<Int, String>) : BaseRequest()

    @Serializable
    data class JoinBroadcast(val userGuid: Int, val name: String) : BaseRequest()

    @Serializable
    data class LeaveBroadcast(val userGuid: Int, val newHost: Int?) : BaseRequest()
}


class PartyOptions
{
    @Serializable
    data class SetPartyModeRequest(val mode: PartyMode) : BaseRequest()

    @Serializable
    data class SetPartyModeBroadcast(val mode: PartyMode, val state: GameStateShared?) : BaseRequest()
}


class UserInfo
{
    @Serializable
    data class SetNameRequest(val name: String) : BaseRequest()

    @Serializable
    data class SetNameBroadcast(val userGuid: Int, val name: String) : BaseRequest()
}


class Chat
{
    @Serializable
    data class MessageRequest(val message: String) : BaseRequest()

    @Serializable
    data class MessageBroadcast(val userGuid: Int, val message: String) : BaseRequest()
}


class WordGame
{
    @Serializable
    data class SetGameSettingsRequest(val settings: GameSettings) : BaseRequest()

    @Serializable
    data class SetGameStageRequest(val stage: GameStage) : BaseRequest()

    @Serializable
    data class AddWordRequest(val value: String, val category: String) : BaseRequest()

    @Serializable
    data class AddWordBroadcast(val value: String, val category: String) : BaseRequest()

    @Serializable
    data class AddCategoryRequest(val value: String) : BaseRequest()

    @Serializable
    data class AddCategoryBroadcast(val value: String) : BaseRequest()

    @Serializable
    data class AssignWordsScatter(val words: List<Word>) : BaseRequest()

    class Playing
    {
        @Serializable
        data class SetWordVisibilityRequest(val word: Word, val player: Int, val visibility: Boolean) : BaseRequest()
    }
}
