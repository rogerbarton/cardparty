package ch.rbarton.wordapp.common.request

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

/**
 * See https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md
 */

@Serializable
sealed class BaseJson(var requestId: Int? = null, @Required val version: Int? = ApiVersion)

expect fun genRequestId(): Int

@Serializable
data class StatusJson(val status: StatusCode, val message: String? = null) : BaseJson()

@Serializable
data class ActionJson(val action: ActionType) : BaseJson()

