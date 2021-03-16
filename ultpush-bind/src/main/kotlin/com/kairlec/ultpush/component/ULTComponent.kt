package com.kairlec.ultpush.component

import com.google.inject.TypeLiteral
import com.kairlec.ultpush.bind.ULTInternalInjector
import com.kairlec.ultpush.component.lifecycle.*
import com.kairlec.ultpush.util.Event
import com.kairlec.ultpush.util.EventDelegate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

/**
 * 一个ULT的组件,由ULTImpl注解过且对应了上层有ULTInterface注解的类
 */
data class ULTComponent(
    val implClazz: Class<out Any>,
    val tpl: TypeLiteral<out Any>?,
    val name: String,
    val order: Int,
) : Comparable<ULTComponent> {
    private val statusLock = ReentrantLock()
    var status: ComponentStatus = ComponentStatus.READY
        private set(value) {
            statusLock.withLock {
                field = value
                statusChangeEvent(value)
            }
        }

    private var loadCycle: LoadCycle? = null
        set(value) {
            checkMultiCycle(field, value)
            field = value
        }
    private var initCycle: InitCycle? = null
        set(value) {
            checkMultiCycle(field, value)
            field = value
        }
    private var runCycle: RunCycle? = null
        set(value) {
            checkMultiCycle(field, value)
            field = value
        }
    private var destroyCycle: DestroyCycle? = null
        set(value) {
            checkMultiCycle(field, value)
            field = value
        }

    val instance: Any by lazy {
        ULTInternalInjector.getInstance(implClazz)
    }

    fun ifStatus(status: ComponentStatus, ok: () -> Unit, listen: EventDelegate<ComponentStatus>) {
        statusLock.withLock {
            if (this.status == status) {
                ok()
            } else {
                statusChangeEvent += listen
            }
        }
    }

    val statusChangeEvent = Event<ComponentStatus>()

    init {
        implClazz.kotlin.functions.forEach {
            it.findAnnotation<ULTLoad>()?.run {
                if (it.typeParameters.isNotEmpty()) {
                    logger.error("[${implClazz.name}](${name}) ULTLoad annotate function cannot have any parameter")
                } else {
                    logger.info("[${implClazz.name}](${name}) ULTLoad annotate function has registered")
                    loadCycle = LoadCycle(this, it)
                }
            }
            it.findAnnotation<ULTInit>()?.run {
                if (it.typeParameters.isNotEmpty()) {
                    logger.error("[${implClazz.name}](${name}) ULTInit annotate function cannot have any parameter")
                } else {
                    logger.info("[${implClazz.name}](${name}) ULTInit annotate function has registered")
                    initCycle = InitCycle(this, it)
                }
            }
            it.findAnnotation<ULTRun>()?.run {
                if (it.typeParameters.run {
                        when {
                            size > 1 -> {
                                logger.error("[${implClazz.name}](${name}) ULTRun annotate function cannot have more than 1 parameter")
                                false
                            }
                            size == 1 -> {
                                val parameter = this[0] as KClass<*>
                                if (parameter != RunDelegate::class) {
                                    logger.error("[${implClazz.name}](${name}) ULTRun annotate function just can use RunDelegate as parameter")
                                    false
                                } else {
                                    true
                                }
                            }
                            else -> true
                        }
                    }) {
                    logger.info("[${implClazz.name}](${name}) ULTRun annotate function has registered")
                    runCycle = RunCycle(this, it)
                }
            }
            it.findAnnotation<ULTDestroy>()?.run {
                if (it.typeParameters.isNotEmpty()) {
                    logger.error("[${implClazz.name}](${name}) ULTDestroy annotate function cannot have any parameter")
                } else {
                    logger.info("[${implClazz.name}](${name}) ULTDestroy annotate function has registered")
                    destroyCycle = DestroyCycle(this, it)
                }
            }
        }
    }

    private fun checkMultiCycle(field: Cycle<*>?, value: Cycle<*>?) {
        if (field != null && value != null) {
            val name = value.annotation::class.simpleName!!
            throw MultiLifecycleFunctionException(
                name,
                implClazz.kotlin,
                name,
                "Multi $name function:[${value.function.name}] and [${field.function.name}]"
            )
        }
    }

    private fun checkStatus(functionName: String) {
        if (status == ComponentStatus.FAILED) {
            throw LifecycleException(
                implClazz.kotlin,
                name,
                null,
                "Cannot $functionName because status is FAILED"
            )
        }
        if (status == ComponentStatus.DEPING) {
            throw LifecycleException(
                implClazz.kotlin,
                name,
                null,
                "Cannot $functionName because status is DEPING, depend has be deadlock!"
            )
        }
    }

    private fun solveDepend(
        dependClasses: Array<KClass<*>>,
        dependNames: Array<String>,
        event: (ULTComponent) -> ULTComponentStatus
    ): ULTComponentStatus? {
        var failed: ULTComponentStatus? = null
        dependClasses.all {
            val status = ULTComponentLifecycle[it]?.run(event) ?: throw LifecycleException(
                implClazz.kotlin,
                name,
                null,
                "Cannot found depend class:${it.qualifiedName}"
            )
            if (!status.status) {
                failed = status
            }
            !status.status
        }
        dependNames.all {
            val status = ULTComponentLifecycle[it]?.run(event) ?: throw LifecycleException(
                implClazz.kotlin,
                name,
                null,
                "Cannot found depend name:${it}"
            )
            if (!status.status) {
                failed = status
            }
            !status.status
        }
        return failed
    }

    private fun checkDependStatus(
        component: ULTComponent,
        status: ComponentStatus,
        functionName: String
    ): ULTComponentStatus {
        return if (!component.status.needStatus(status)) {
            component.failed(
                LifecycleException(
                    implClazz.kotlin,
                    name,
                    null,
                    "Cannot $functionName depend class:${component.implClazz.name} because status need check failed: need[${status.name}] got[${component.status.name}]"
                )
            )
        } else {
            component.success()
        }
    }

    internal fun load() {
        try {
            if (status.needStatus(ComponentStatus.READY)) {
                logger.info("load component ${implClazz.name}(${name})")
                loadCycle?.run {
                    checkStatus("load")
                    if (function.isSuspend) {
                        runBlocking {
                            function.callSuspend(instance)
                        }
                    } else {
                        function.call(instance)
                    }
                }
                status = ComponentStatus.LOADED
            }
        } catch (e: Throwable) {
            logger.warn(
                "load component ${implClazz.name}(${name}) failed because:${e.message}",
                e
            )
            status = ComponentStatus.FAILED
        }
    }

    internal fun init(): ULTComponentStatus {
        try {
            if (status.needStatus(ComponentStatus.LOADED)) {
                if (status.level >= ComponentStatus.INITED.level) {
                    //由于被依赖关系,已经执行过了,无需再次执行
                    return success()
                }
                logger.info("init component ${implClazz.name}(${name})")
                initCycle?.run {
                    checkStatus("init")
                    val depStatus = solveDepend(annotation.dependClasses, annotation.dependNames) {
                        val depStatus = it.init()
                        if (depStatus.status) {
                            checkDependStatus(it, ComponentStatus.INITED, "init")
                        } else {
                            depStatus
                        }
                    }
                    if (depStatus != null && !depStatus.status) {
                        status = ComponentStatus.FAILED
                        return transfer(depStatus)
                    }
                    if (function.isSuspend) {
                        runBlocking {
                            function.callSuspend(instance)
                        }
                    } else {
                        function.call(instance)
                    }
                    status = ComponentStatus.INITED
                    return success()
                } ?: run {
                    status = ComponentStatus.INITED
                    return success()
                }
            } else {
                return failed("status check failed")
            }
        } catch (e: Throwable) {
            logger.warn(
                "init component ${implClazz.name}(${name}) failed because:${e.message}",
                e
            )
            status = ComponentStatus.FAILED
            return failed(e)
        }
    }

    internal fun run(): ULTComponentStatus {
        try {
            if (status.needStatus(ComponentStatus.INITED)) {
                if (status.level >= ComponentStatus.RUNNING.level) {
                    //由于被依赖关系,已经执行过了,无需再次执行
                    return success()
                }
                logger.info("run component ${implClazz.name}(${name})")
                runCycle?.run {
                    checkStatus("run")
                    val depStatus = solveDepend(annotation.dependClasses, annotation.dependNames) {
                        it.run()
                    }
                    if (depStatus != null && !depStatus.status) {
                        status = ComponentStatus.FAILED
                        return transfer(depStatus)
                    }
                    val runDelegate = object : RunDelegate {
                        private val channel = Channel<ULTComponentStatus>(1)
                        suspend fun finish(): ULTComponentStatus {
                            return channel.receive()
                        }

                        override fun runFinish() {
                            status = ComponentStatus.RUNNING
                            runBlocking { channel.send(success()) }
                        }

                        override fun runError(e: Exception?, message: String?) {
                            status = ComponentStatus.FAILED
                            runBlocking { channel.send(failed(e, message)) }
                        }
                    }

                    suspend fun callFunctionSuspend(): ULTComponentStatus {
                        return if (function.typeParameters.size > 1) {
                            if (annotation.async) {
                                GlobalScope.launch {
                                    function.callSuspend(instance, runDelegate)
                                }
                            } else {
                                function.callSuspend(instance, runDelegate)
                            }
                            runDelegate.finish()
                        } else {
                            if (annotation.async) {
                                GlobalScope.launch {
                                    function.callSuspend(instance)
                                }
                            } else {
                                function.callSuspend(instance)
                            }
                            status = ComponentStatus.RUNNING
                            success()
                        }
                    }

                    fun callFunction(): ULTComponentStatus {
                        return if (function.typeParameters.size > 1) {
                            if (annotation.async) {
                                GlobalScope.launch {
                                    function.call(instance, runDelegate)
                                }
                            } else {
                                function.call(instance, runDelegate)
                            }
                            runBlocking { runDelegate.finish() }
                        } else {
                            if (annotation.async) {
                                GlobalScope.launch {
                                    function.call(instance)
                                }
                            } else {
                                function.call(instance)
                            }
                            status = ComponentStatus.RUNNING
                            success()
                        }
                    }
                    return if (function.isSuspend) {
                        runBlocking { callFunctionSuspend() }
                    } else {
                        callFunction()
                    }

                } ?: run {
                    status = ComponentStatus.RUNNING
                    return success()
                }
            } else {
                return failed("status check failed")
            }
        } catch (e: Throwable) {
            logger.warn(
                "run component ${implClazz.name}(${name}) failed because:${e.message}",
                e
            )
            status = ComponentStatus.FAILED
            return failed(e)
        }
    }

    internal fun destroy(): ULTComponentStatus {
        logger.info("destroy component ${implClazz.name}(${name})")
        try {
            if (status.level == ComponentStatus.DESTROYED.level) {
                //由于被依赖关系,已经执行过了,无需再次执行
                return success()
            }
            destroyCycle?.run {
                checkStatus("destroy")
                val depStatus = solveDepend(annotation.dependClasses, annotation.dependNames) {
                    val depStatus = it.destroy()
                    if (depStatus.status) {
                        it.checkDependStatus(it, ComponentStatus.DESTROYED, "destroy")
                    } else {
                        depStatus
                    }
                }
                if (depStatus != null && !depStatus.status) {
                    status = ComponentStatus.FAILED
                    return transfer(depStatus)
                }
                runBlocking {
                    if (function.isSuspend) {
                        function.callSuspend(instance)
                    } else {
                        function.call(instance)
                    }
                }
                status = ComponentStatus.DESTROYED
                return success()
            } ?: run {
                status = ComponentStatus.DESTROYED
                return success()
            }
        } catch (e: Throwable) {
            status = ComponentStatus.FAILED
            return failed(e)
        }
    }

    override fun equals(other: Any?): Boolean = other is ULTComponent && other.implClazz == implClazz
    override fun hashCode(): Int {
        return implClazz.hashCode()
    }

    override fun compareTo(other: ULTComponent): Int {
        return this.order.compareTo(other.order)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ULTComponent::class.java)
    }

    enum class ComponentStatus(val level: Int) {
        DEPING(1),
        FAILED(2),
        DESTROYED(3),
        READY(4),
        LOADED(5),
        INITED(6),
        RUNNING(7),

        ;

        fun needStatus(target: ComponentStatus): Boolean {
            return this.level >= target.level
        }
    }
}

