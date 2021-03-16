package com.kairlec.ultpush.core

import com.kairlec.ultpush.bind.runLifecycle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.concurrent.locks.Condition
import kotlin.concurrent.thread
import java.util.concurrent.locks.ReentrantLock


class Application private constructor() {
    private val mainThread: Thread = thread(start = true, isDaemon = false, name = "ULTPush Application") {
        runLifecycle()
        try {
            LOCK.lock()
            STOP.await()
        } catch (e: InterruptedException) {
            logger.warn(" service   stopped, interrupted by other thread!", e)
        } finally {
            LOCK.unlock()
        }
    }

    init {
        Runtime.getRuntime().addShutdownHook(Thread({
            try {
                com.kairlec.ultpush.bind.stop()
            } catch (e: Exception) {
                logger.error("StartMain stop exception ", e)
            }
            logger.info("jvm exit, all service stopped.")
            try {
                LOCK.lock()
                STOP.signal()
            } finally {
                LOCK.unlock()
            }
        }, "ULTPush-Main-shutdown-hook"))
    }

    fun pid(): Long {
        return ProcessHandle.current().pid()
    }

    fun join() {
        mainThread.join()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Application::class.java)
        lateinit var args: Array<String>
            private set

        private val applicationStartMutex = Mutex()
        private lateinit var application: Application
        private val LOCK = ReentrantLock()
        private val STOP: Condition = LOCK.newCondition()


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
