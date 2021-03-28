package com.kairlec.ultpush.plugin

import com.kairlec.ultpush.ULTContextManager
import org.slf4j.LoggerFactory

internal object ULTPluginImplLifecycle {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val components by lazy {
        ULTContextManager.impls.values.sorted()
    }

    suspend fun destroy() {
        components.forEach {
            val status = it.destroy()
            if (!status.status) {
                logger.warn("destroy component ${it.clazz.name}(${it.name}) failed :${status}")
            }
        }
    }

    suspend fun run() {
        try {
            components.forEach {
                it.load()
            }
            components.forEach {
                val status = it.init()
                if (!status.status) {
                    logger.error("init component ${it.clazz.name}(${it.name}) failed:${status}")
                }
            }
            components.forEach {
                val status = it.run()
                if (!status.status) {
                    logger.error("run component ${it.clazz.name}(${it.name}) failed:${status}")
                }
            }
            logger.info("run lifecycle finished")
        } catch (e: Throwable) {
            logger.error("run lifecycle failed", e)
        }
    }
}
