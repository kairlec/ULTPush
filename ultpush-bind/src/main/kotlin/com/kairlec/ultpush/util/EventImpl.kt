package com.kairlec.ultpush.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

fun interface EventDelegate<Base> {
    operator fun invoke(base: Base)
}

interface Event<Base> : ReadOnlyEvent<Base>, InvokeAbleEvent<Base>

interface ReadOnlyEvent<Base> {
    fun interface CallableWrapper<Base> {
        suspend operator fun invoke(base: Base)
    }

    operator fun contains(callable: CallableWrapper<Base>): Boolean
    operator fun contains(delegate: EventDelegate<Base>): Boolean
    suspend operator fun plusAssign(callable: CallableWrapper<Base>)
    suspend operator fun plusAssign(delegate: EventDelegate<Base>)
    suspend operator fun minusAssign(callable: CallableWrapper<Base>)
    suspend operator fun minusAssign(delegate: EventDelegate<Base>)
}

interface InvokeAbleEvent<Base> {
    suspend operator fun invoke(base: Base)
}

class EventImpl<Base> : Event<Base> {
    private inner class EventDelegateWrapper(private val delegate: EventDelegate<Base>) :
        ReadOnlyEvent.CallableWrapper<Base> {
        override suspend operator fun invoke(base: Base) {
            delegate(base)
        }

        override fun hashCode() = delegate.hashCode()
        override fun equals(other: Any?) =
            other is EventImpl<*>.EventDelegateWrapper && delegate.hashCode() == other.hashCode()
    }

    private val lock = Mutex()

    private val functions = LinkedList<ReadOnlyEvent.CallableWrapper<Base>>()

    override suspend operator fun invoke(base: Base) {
        lock.withLock {
            functions.forEach {
                it(base)
            }
        }
    }

    override operator fun contains(callable: ReadOnlyEvent.CallableWrapper<Base>): Boolean {
        return callable in functions
    }

    override operator fun contains(delegate: EventDelegate<Base>): Boolean {
        return EventDelegateWrapper(delegate) in functions
    }

    override suspend operator fun plusAssign(callable: ReadOnlyEvent.CallableWrapper<Base>) {
        lock.withLock {
            if (callable !in functions) {
                functions.add(callable)
            }
        }
    }

    override suspend operator fun plusAssign(delegate: EventDelegate<Base>) {
        plusAssign(EventDelegateWrapper(delegate))
    }

    override suspend operator fun minusAssign(callable: ReadOnlyEvent.CallableWrapper<Base>) {
        lock.withLock {
            functions.remove(callable)
        }
    }

    override suspend operator fun minusAssign(delegate: EventDelegate<Base>) {
        minusAssign(EventDelegateWrapper(delegate))
    }
}
