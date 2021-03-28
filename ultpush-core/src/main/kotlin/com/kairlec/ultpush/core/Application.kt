package com.kairlec.ultpush.core

import com.kairlec.ultpush.bind.runLifecycle
import com.kairlec.ultpush.core.util.CustomCondition
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

object Application {
    private var started = false

    private val mainJob = GlobalScope.launch(Dispatchers.IO, start = CoroutineStart.LAZY) {
        withContext(NonCancellable) {
            runLifecycle()
            logger.info("Start ULTPush Application Success!")
            if (!args.contains("--nobanner")) {
                logger.info(
                    """

       __  ____  __________             __  
      / / / / / /_  __/ __ \__  _______/ /_ 
     / / / / /   / / / /_/ / / / / ___/ __ \
    / /_/ / /___/ / / ____/ /_/ (__  ) / / /
    \____/_____/_/ /_/    \__,_/____/_/ /_/
    
       https://github.com/kairlec/ULTPush

"""
                )
            }
            LOCK.withLock {
                STOP.await()
            }
        }
    }

    fun pid(): Long {
        return ProcessHandle.current().pid()
    }

    suspend fun join() {
        applicationStartMutex.withLock {
            if (mainJob.isCompleted) {
                return
            }
            if (!started) {
                start()
            }
            if (mainJob.isActive) {
                mainJob.join()
            }
        }
    }

    private val logger = LoggerFactory.getLogger(Application::class.java)
    var args: Array<String> = emptyArray()
        private set

    private val applicationStartMutex = Mutex()

    private val LOCK = Mutex()
    private val STOP = CustomCondition()

    suspend fun start() {
        start(emptyArray())
    }

    suspend fun start(args: Array<String>) {
        applicationStartMutex.withLock {
            if (!started) {
                this.args = args
                mainJob.start()
                Runtime.getRuntime().addShutdownHook(Thread({
                    runBlocking {
                        try {
                            com.kairlec.ultpush.bind.stop()
                        } catch (e: Throwable) {
                            logger.error("StartMain stop exception ", e)
                        }
                        logger.info("jvm exit, all service stopped.")
                        LOCK.withLock {
                            STOP.signal()
                        }
                    }
                }, "ULTPush-Main-shutdown-hook"))
                started = true
            }
        }
    }
}