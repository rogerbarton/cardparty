package ch.rbarton.wordapp.common.request

var nextRequestId = 0

actual fun genRequestId(): Int = nextRequestId++