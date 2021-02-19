package common

var nextRequestId = 0

actual fun genRequestId(): Int
{
    return nextRequestId++
}