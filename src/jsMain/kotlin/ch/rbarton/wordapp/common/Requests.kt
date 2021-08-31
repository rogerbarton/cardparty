package ch.rbarton.wordapp.common

var nextRequestId = 0

actual fun genRequestId(): Int
{
    return nextRequestId++
}