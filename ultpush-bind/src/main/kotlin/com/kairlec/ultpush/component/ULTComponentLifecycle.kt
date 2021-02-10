package com.kairlec.ultpush.component

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredFunctions

internal object ULTComponentLifecycle {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val components = HashSet<Lifecycle>()

    fun register(clazz: KClass<out Any>, name: String) {
        val lifecycle = Lifecycle(clazz, name)
        clazz.declaredFunctions.forEach {
            ULTInit.register(lifecycle, it)
            ULTLoad.register(lifecycle, it)
            ULTRun.register(lifecycle, it)
            ULTDestroy.register(lifecycle, it)
        }
        components.add(lifecycle)
    }

    private val failedNames = HashSet<String>()
    private val failedClasses = HashSet<KClass<out Any>>()

    fun run() {
        try {
            try {
                Runtime.getRuntime().addShutdownHook(thread(false) {
                    run(components, failedNames, failedClasses, LifecycleEnum.DESTROY)
                })
            } catch (e: Throwable) {
                logger.error("Hook shutdown failed:${e.message}", e)
            }
            LifecycleEnum.values().sortedBy { it.order }.forEach { lc ->
                if (lc.autoRun) {
                    run(components, failedNames, failedClasses, lc)
                }
            }
        } catch (e: Throwable) {
            logger.error("run lifecycle failed", e)
        }
    }

}