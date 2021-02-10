package com.kairlec.ultpush.core

import com.kairlec.ultpush.core.handler.MessageHandler
import com.kairlec.ultpush.core.pusher.Pusher
import com.kairlec.ultpush.core.receiver.Receiver
import com.kairlec.ultpush.core.receiver.ReceiverMsg
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class Application private constructor() {
    companion object {
        val logger = LoggerFactory.getLogger(Application::class.java)
        lateinit var args: Array<String>
            private set
        private val receiverMap = ConcurrentHashMap<String, Receiver>()
        private val receiverRegisterMutex = Mutex()
        val receiverContext get() = receiverMap as Map<String, Receiver>

        private val pusherMap = ConcurrentHashMap<String, Pusher>()
        private val pusherRegisterMutex = Mutex()
        val pusherContext get() = pusherMap as Map<String, Pusher>

        private val handlerMap = ConcurrentHashMap<String, MessageHandler>()
        private val handlerRegisterMutex = Mutex()
        val handlerContext get() = handlerMap as Map<String, MessageHandler>

        private val applicationStartMutex = Mutex()
        private lateinit var application: Application

        suspend fun start(): Application {
            applicationStartMutex.withLock {
                if (!this::args.isInitialized) {
                    this.args = emptyArray()
                }
                return if (this::application.isInitialized) {
                    application
                } else {
                    application = Application()
                    application
                }
            }
        }

        suspend fun start(args: Array<String>): Application {
            applicationStartMutex.withLock {
                if (!this::args.isInitialized) {
                    this.args = args
                }
                return if (this::application.isInitialized) {
                    application
                } else {
                    application = Application()
                    application
                }
            }
        }
    }

    //region 接收器
    suspend fun registerReceiver(receiver: Receiver) {
        receiverRegisterMutex.withLock {
            if (receiverMap.containsKey(receiver.name)) {
                throw IllegalArgumentException("receiver name '${receiver.name}' has registered")
            }
            receiverMap[receiver.name] = receiver
        }
    }

    fun getReceiver(name: String): Receiver? {
        return receiverMap[name]
    }

    fun getReceiver(clazz: KClass<Receiver>): List<Receiver> {
        return receiverMap.values.filter { it::class == clazz }
    }

    inline fun <reified R : Receiver> getReceiver(): List<Receiver> {
        return receiverContext.values.filter { it::class == R::class }
    }
    //endregion

    //region 推送器
    suspend fun registerPusher(pusher: Pusher) {
        pusherRegisterMutex.withLock {
            if (pusherMap.containsKey(pusher.name)) {
                throw IllegalArgumentException("pusher name '${pusher.name}' has registered")
            }
            pusherMap[pusher.name] = pusher
        }
    }

    fun getPusher(name: String): Pusher? {
        return pusherMap[name]
    }

    fun getPusher(clazz: KClass<Pusher>): List<Pusher> {
        return pusherMap.values.filter { it::class == clazz }
    }

    inline fun <reified R : Pusher> getPusher(): List<Pusher> {
        return pusherContext.values.filter { it::class == R::class }
    }
    //endregion

    //region 消息处理器
    suspend fun registerMessageHandler(handler: MessageHandler) {
        handlerRegisterMutex.withLock {
            if (handlerMap.containsKey(handler.name)) {
                throw IllegalArgumentException("handler name '${handler.name}' has registered")
            }
            handlerMap[handler.name] = handler
        }
    }

    fun getMessageHandler(name: String): MessageHandler? {
        return handlerMap[name]
    }

    fun getMessageHandler(clazz: KClass<MessageHandler>): List<MessageHandler> {
        return handlerMap.values.filter { it::class == clazz }
    }

    inline fun <reified R : MessageHandler> getMessageHandler(): List<MessageHandler> {
        return handlerContext.values.filter { it::class == R::class }
    }
    //endregion


    init {
        GlobalScope.launch {
        }
    }
}
