package ch.rbarton.wordapp.common.request

import ch.rbarton.wordapp.common.data.*
import ch.rbarton.wordapp.common.data.UserInfo
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
data class InitResponse(val userId: Int, val userCount: Int, val parties: Map<String, Int>) : BaseRequest()


class Party
{
    @Serializable
    data class CreateResponse(val partyCode: String) : BaseRequest()

    @Serializable
    data class JoinRequest(val partyCode: String) : BaseRequest()

    @Serializable
    data class JoinResponse(val partyBase: PartyBase, val userToNames: Map<Int, UserInfo>, val newColorId: Int?) :
        BaseRequest()

    @Serializable
    data class JoinBroadcast(val userId: Int, val userInfo: UserInfo) : BaseRequest()

    @Serializable
    data class LeaveBroadcast(val userId: Int, val newHost: Int?) : BaseRequest()
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
    data class SetNameBroadcast(val userId: Int, val name: String) : BaseRequest()

    @Serializable
    data class SetColorRequest(val colorId: Int) : BaseRequest()

    @Serializable
    data class SetColorBroadcast(val userId: Int, val colorId: Int) : BaseRequest()
}


class Chat
{
    @Serializable
    data class MessageRequest(val message: String) : BaseRequest()

    @Serializable
    data class MessageBroadcast(val userId: Int, val message: String) : BaseRequest()
}


class WordGame
{
    @Serializable
    data class SetGameSettingsRequest(val settings: GameSettings) : BaseRequest()

    @Serializable
    data class SetGameStageRequest(val stage: GameStage) : BaseRequest()

    @Serializable
    data class AddCategoryRequest(val text: String, val colorId: Int?) : BaseRequest()

    @Serializable
    data class AddCategoryBroadcast(val category: CardCategory, val categoryId: Int) : BaseRequest()

    @Serializable
    data class RemoveCategoryRequest(val categoryId: Int) : BaseRequest()

    @Serializable
    data class AddCardRequest(val text: String, val categoryId: Int) : BaseRequest()

    @Serializable
    data class AddCardBroadcast(val card: Card, val cardId: Int) : BaseRequest()

    @Serializable
    data class RemoveCardRequest(val cardId: Int) : BaseRequest()

    @Serializable
    data class AssignWordsScatter(val cards: List<Card>) : BaseRequest()

    class Playing
    {
        @Serializable
        data class SetWordVisibilityRequest(val card: Card, val player: Int, val visibility: Boolean) : BaseRequest()
    }
}
