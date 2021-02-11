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

}
