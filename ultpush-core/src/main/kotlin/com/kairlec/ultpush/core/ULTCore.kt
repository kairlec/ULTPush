package com.kairlec.ultpush.core

import com.google.inject.Key
import com.google.inject.TypeLiteral
import com.kairlec.ultpush.bind.ULTInjector
import com.kairlec.ultpush.core.handler.MessageHandler
import com.kairlec.ultpush.core.pusher.Pusher
import com.kairlec.ultpush.core.pusher.PusherMsg
import com.kairlec.ultpush.core.receiver.Receiver
import com.kairlec.ultpush.core.receiver.ReceiverMsg
import java.util.*
import kotlin.collections.HashMap

typealias Event = (Array<out Any?>) -> Unit

private val eventMap = HashMap<String, LinkedList<Event>>()

fun registerEvent(key: String, keyword: String? = null, event: Event) {
    eventMap[key]?.addLast(event) ?: run {
        eventMap[key] = LinkedList<Event>().apply { add(event) }
    }
}

fun emit(key: String, keyword: String? = null, vararg args: Array<out Any?>) {
    eventMap[key]?.forEach {
        it(args)
    }
}

object ULTCore {
    inline fun <reified T : ReceiverMsg> Receiver<T>.receiverMessage(
        receiverMsg: T,
        assignable: Boolean = true
    ): ReceiverResult {
        return receiveMessage(receiverMsg, this, assignable)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : ReceiverMsg> receiveMessage(
        receiverMsg: T,
        receiver: Receiver<T>,
        assignable: Boolean = true
    ): ReceiverResult {
        val auth = receiver.authenticate(receiverMsg)
        if (!auth.accept) {
            val exception = AuthenticateException(auth)
            return ReceiverResult(
                exception.message,
                false,
                exception
            )
        }
        try {
            if (receiver.allow(receiverMsg)) {
                val handler = ULTInjector.getGenericInstance(
                    Key.get(object : TypeLiteral<MessageHandler<T, out PusherMsg>>() {}),
                    assignable
                )
                if (handler.allow(receiverMsg)) {
                    val pusherMsg = handler.handle(receiverMsg)
                    val pusher =
                        ULTInjector.getGenericInstances(Key.get(handler.getPusherTypeLiteral()), assignable)
                    pusher.forEach {
                        it as Pusher<PusherMsg>
                        if (it.allow(pusherMsg)) {
                            it.push(pusherMsg)
                            return ReceiverResult(
                                "OK",
                                true,
                                null
                            )
                        }
                    }
                }
            }
            return ReceiverResult(
                "Filtered",
                true,
                null
            )
        } catch (e: Throwable) {
            return ReceiverResult(
                e.message,
                false,
                e
            )
        }
    }
}

data class ReceiverResult(
    val message: String?,
    val ok: Boolean,
    val data: Throwable?
)