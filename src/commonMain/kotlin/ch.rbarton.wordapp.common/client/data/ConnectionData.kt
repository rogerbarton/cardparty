package ch.rbarton.wordapp.common.client.data

data class ConnectionData(
    var serverAddress: String = "127.0.0.1",
    var serverPort: Int = 8080,
    var guid: Int? = null,
    var name: String = ""
)