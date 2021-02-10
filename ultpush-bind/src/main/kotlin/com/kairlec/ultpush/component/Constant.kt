package com.kairlec.ultpush.component

import com.kairlec.ultpush.bind.ULTInject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend

class LifecycleRunner

private val logger = LoggerFactory.getLogger(LifecycleRunner::class.java)

fun run(
    lifecycles: Iterable<Lifecycle>,
    failedNames: MutableSet<String>,
    failedClasses: MutableSet<KClass<out Any>>,
    lifecycleEnum: LifecycleEnum
) {
    val finishedNames = HashSet<String>()
    val finishedClasses = HashSet<KClass<out Any>>()
    val waitNames = HashSet<String>()
    val waitClasses = HashSet<KClass<out Any>>()
    lifecycles.forEach {
        logger.debug("${lifecycleEnum.stage} class:${it.clazz.qualifiedName} [name:${it.name}]")
        try {
            when (lifecycleEnum) {
                LifecycleEnum.LOAD -> {
                    runSignal(
                        lifecycles,
                        it,
                        it.loadFunction,
                        null,
                        null,
                        finishedNames,
                        finishedClasses,
                        waitNames,
                        waitClasses,
                        failedNames,
                        failedClasses,
                        false
                    )
                }
                LifecycleEnum.INIT -> {
                    runSignal(
                        lifecycles,
                        it,
                        it.initFunction,
                        it.initAnn?.dependClasses,
                        it.initAnn?.dependNames,
                        finishedNames,
                        finishedClasses,
                        waitNames,
                        waitClasses,
                        failedNames,
                        failedClasses,
                        false
                    )
                }
                LifecycleEnum.RUN -> {
                    runSignal(
                        lifecycles,
                        it,
                        it.runFunction,
                        it.runAnn?.dependClasses,
                        it.runAnn?.dependNames,
                        finishedNames,
                        finishedClasses,
                        waitNames,
                        waitClasses,
                        failedNames,
                        failedClasses,
                        it.runAnn?.async ?: true
                    )
                }
                LifecycleEnum.DESTROY -> {
                    runSignal(
                        lifecycles,
                        it,
                        it.destroyFunction,
                        it.destroyAnn?.dependClasses,
                        it.destroyAnn?.dependNames,
                        finishedNames,
                        finishedClasses,
                        waitNames,
                        waitClasses,
                        failedNames,
                        failedClasses,
                        false
                    )
                }
            }
            logger.info("${it.clazz.qualifiedName}[${it.name}] {${lifecycleEnum.stage}} finished")
        } catch (e: Throwable) {
            failedNames.add(it.name)
            failedClasses.add(it.clazz)
            logger.error("${lifecycleEnum.stage} failed:${e.message}", e)
        }
    }
}

fun runSignal(
    lifecycles: Iterable<Lifecycle>,
    lifecycle: Lifecycle,
    currentFunction: KFunction<*>?,
    dependClasses: Array<KClass<out Any>>?,
    dependNames: Array<String>?,
    finishedNames: MutableSet<String>,
    finishedClasses: MutableSet<KClass<out Any>>,
    waitNames: MutableSet<String>,
    waitClass: MutableSet<KClass<out Any>>,
    failedNames: MutableSet<String>,
    failedClass: MutableSet<KClass<out Any>>,
    async: Boolean,
) {
    if (lifecycle.instance == null) {
        val instance = ULTInject.getInstance(lifecycle.clazz.java)
        lifecycle.instance = instance
    }
    // 已完成的不用再次执行(多个依赖一个)
    if (finishedNames.contains(lifecycle.name) || finishedClasses.contains(lifecycle.clazz)) {
        return
    }
    // 前面已经有等待了,无法解决依赖循环的问题
    if (waitNames.contains(lifecycle.name) || waitClass.contains(lifecycle.clazz)) {
        throw RuntimeException("Dependency contains closed loop on '${lifecycle.clazz.qualifiedName}' [name:${lifecycle.name}]")
    }
    currentFunction?.run {
        waitNames.add(lifecycle.name)
        waitClass.add(lifecycle.clazz)
        fun solveDependency(dependLifecycles: List<Lifecycle>) {
            dependLifecycles.forEach { dependLifecycle ->
                if (failedNames.contains(dependLifecycle.name) || failedClass.contains(dependLifecycle.clazz)) {
                    throw RuntimeException("Dependency class '${dependLifecycle.clazz.qualifiedName}' [name:${dependLifecycle.name}] failed")
                }
                runSignal(
                    lifecycles,
                    dependLifecycle,
                    currentFunction,
                    dependClasses,
                    dependNames,
                    finishedNames,
                    finishedClasses,
                    waitNames,
                    waitClass,
                    failedNames,
                    failedClass,
                    async
                )
            }
        }
        dependClasses?.forEach { kclass ->
            if (!finishedClasses.contains(kclass)) {
                lifecycles.filter { it.clazz == kclass.java }.apply {
                    if (isEmpty()) {
                        throw ClassNotFoundException("No dep class '${kclass.java.name}' registered")
                    }
                }.run(::solveDependency)
            }
        }
        dependNames?.forEach { name ->
            if (!finishedNames.contains(name)) {
                lifecycles.filter { it.name == name }.apply {
                    if (isEmpty()) {
                        throw ClassNotFoundException("No dep class named[$name] registered")
                    }
                }.run(::solveDependency)
            }
        }
        waitNames.remove(lifecycle.name)
        waitClass.remove(lifecycle.clazz)
        if (async) {
            if (isSuspend) {
                GlobalScope.launch {
                    callSuspend(lifecycle.instance)
                }
            } else {
                GlobalScope.launch {
                    call(lifecycle.instance)
                }
            }
        } else {
            if (isSuspend) {
                runBlocking {
                    callSuspend(lifecycle.instance)
                }
            } else {
                call(lifecycle.instance)
            }
        }
    }
    finishedClasses.add(lifecycle.clazz)
    finishedNames.add(lifecycle.name)
}