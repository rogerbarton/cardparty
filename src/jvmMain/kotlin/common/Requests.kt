package common

import java.util.concurrent.atomic.AtomicInteger

var nextRequestId = AtomicInteger(0)

actual fun genRequestId(): Int
{
    return nextRequestId.getAndIncrement()
}