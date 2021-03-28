package com.kairlec.ultpush.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

typealias EventDelegate<Base> = suspend (base: Base) -> Unit

class Event<Base> {
    interface CallableWrapper<Base> {
        suspend operator fun invoke(base: Base)
    }

    private inner class EventDelegateWrapper(private val delegate: EventDelegate<Base>) : CallableWrapper<Base> {
        override suspend operator fun invoke(base: Base) {
            delegate(base)
        }

        override fun hashCode() = delegate.hashCode()
        override fun equals(other: Any?) =
            other is Event<*>.EventDelegateWrapper && delegate.hashCode() == other.hashCode()
    }

    private val lock = Mutex()

    private val functions = LinkedList<CallableWrapper<Base>>()

    suspend operator fun invoke(base: Base) {
        lock.withLock {
            functions.forEach {
                it(base)
            }
        }
    }

    operator fun contains(callable: CallableWrapper<Base>): Boolean {
        return callable in functions
    }

    operator fun contains(delegate: EventDelegate<Base>): Boolean {
        return EventDelegateWrapper(delegate) in functions
    }

    suspend operator fun plusAssign(callable: CallableWrapper<Base>) {
        lock.withLock {
            if (callable !in functions) {
                functions.add(callable)
            }
        }
    }

    suspend operator fun plusAssign(delegate: EventDelegate<Base>) {
        plusAssign(EventDelegateWrapper(delegate))
    }

    suspend operator fun minusAssign(callable: CallableWrapper<Base>) {
        lock.withLock {
            functions.remove(callable)
        }
    }

    suspend operator fun minusAssign(delegate: EventDelegate<Base>) {
        minusAssign(EventDelegateWrapper(delegate))
    }
}