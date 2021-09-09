package ch.rbarton.wordapp.common.data

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(var name: String = "", var colorId: Int = 0)