package ch.rbarton.wordapp.common.request

import kotlinx.serialization.Serializable

@Serializable
data class InitJson(val guid: Int, val userCount: Int, val parties: Map<String, Int>) : BaseJson()

@Serializable
data class SetNameJson(val name: String) : BaseJson()

@Serializable
data class SetNameBroadcastJson(val userId: Int, val name: String) : BaseJson()