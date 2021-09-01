package ch.rbarton.wordapp.cli.data

data class Connection(
    var serverAddress: String = "127.0.0.1",
    var serverPort: Int = 8080,
    var guid: Int? = null,
    var name: String = ""
)