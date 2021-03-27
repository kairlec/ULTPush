package com.kairlec.ultpush.core.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect

class CustomMultiCondition {
    private val events = MutableSharedFlow<Unit>()

    suspend fun signal() {
        events.emit(Unit)
    }

    suspend fun await() {
        events.collect()
    }

    suspend fun await(action: suspend (value: Unit) -> Unit) {
        events.collect(action)
    }
}

class CustomCondition {
    private val events = Channel<Unit>(1)

    suspend fun signal() {
        events.send(Unit)
    }

    suspend fun await() {
        events.receive()
    }

    suspend fun await(action: suspend (value: Unit) -> Unit) {
        events.receive()
        action(Unit)
    }
}
