package ch.rbarton.wordapp.common.request

import ch.rbarton.wordapp.common.data.GameState
import ch.rbarton.wordapp.common.data.PartyOptions
import kotlinx.serialization.Serializable

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