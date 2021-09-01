package ch.rbarton.wordapp.common.request

import kotlinx.serialization.Serializable

@Serializable
data class ChatJson(val message: String) : BaseJson()

@Serializable
data class ChatBroadcastJson(val userId: Int, val message: String) : BaseJson()