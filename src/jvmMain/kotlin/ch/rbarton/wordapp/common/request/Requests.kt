package ch.rbarton.wordapp.common.request

import java.util.concurrent.atomic.AtomicInteger

var nextRequestId = AtomicInteger(0)

actual fun genRequestId(): Int = nextRequestId.getAndIncrement()