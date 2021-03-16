package com.kairlec.ultpush.util

import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

typealias EventDelegate<Base> = (base: Base) -> Unit

class Event<Base> {
    interface CallableWrapper<Base> {
        operator fun invoke(base: Base)
    }

    private inner class EventDelegateWrapper(private val delegate: EventDelegate<Base>) : CallableWrapper<Base> {
        override operator fun invoke(base: Base) {
            delegate(base)
        }

        override fun hashCode() = delegate.hashCode()
        override fun equals(other: Any?) = other is Event<*>.EventDelegateWrapper && delegate.hashCode() == other.hashCode()
    }

    private val lock = ReentrantLock()

    private val functions = LinkedList<CallableWrapper<Base>>()

    operator fun invoke(base: Base) {
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

    operator fun plusAssign(callable: CallableWrapper<Base>) {
        lock.withLock {
            if (callable !in functions) {
                functions.add(callable)
            }
        }
    }

    operator fun plusAssign(delegate: EventDelegate<Base>) {
        plusAssign(EventDelegateWrapper(delegate))
    }

    operator fun minusAssign(callable: CallableWrapper<Base>) {
        lock.withLock {
            functions.remove(callable)
        }
    }

    operator fun minusAssign(delegate: EventDelegate<Base>) {
        minusAssign(EventDelegateWrapper(delegate))
    }
}