package com.kairlec.ultpush.plugin

import com.google.inject.Key
import com.google.inject.TypeLiteral
import com.kairlec.ultpush.ULTAbstractDepend
import com.kairlec.ultpush.ULTContext
import com.kairlec.ultpush.ULTContextManager
import com.kairlec.ultpush.bind.ULTImpl
import com.kairlec.ultpush.bind.ULTInterface
import com.kairlec.ultpush.bind.ULTInternalInjector
import com.kairlec.ultpush.component.*
import com.kairlec.ultpush.component.lifecycle.*
import com.kairlec.ultpush.util.Event
import com.kairlec.ultpush.util.EventDelegate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

class ULTPluginInterface(
    val annotation: ULTInterface,
    val clazz: Class<*>,
    val plugin: ULTPlugin,
    val impls: List<ULTPluginImpl> = LinkedList()
) {
    override fun hashCode(): Int {
        return clazz.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return clazz == other
    }

    override fun toString(): String {
        return "ULTPluginInterface(annotation=$annotation, clazz='${clazz.name}', plugin='${plugin.name}', impls=${impls.map { it.clazz.name }})"
    }
}

class ULTPluginImpl(
    val annotation: ULTImpl,
    val clazz: Class<out Any>,
    val plugin: ULTPlugin,
    val tpl: TypeLiteral<out Any>?,
    val name: String,
    val order: Int,
    val interfaces: List<ULTPluginInterface> = LinkedList()
) : Comparable<ULTPluginImpl> {
    companion object {
        private val logger = LoggerFactory.getLogger(ULTPluginImpl::class.java)
    }

    private val lifeCycleLock = Mutex()

    private inline fun <T> withLifeCycleLock(crossinline event: () -> T): T {
        return runBlocking {
            lifeCycleLock.withLock {
                event()
            }
        }
    }

    var status by object {
        private var status: ULTPluginImplStatus = ULTPluginImplStatus.READY
        private val statusLock = Mutex()
        operator fun getValue(thisRef: Any?, property: KProperty<*>): ULTPluginImplStatus {
            return runBlocking {
                statusLock.withLock {
                    status
                }
            }
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: ULTPluginImplStatus) {
            runBlocking {
                statusLock.withLock {
                    status = value
                }
            }
        }
    }

    internal var currentFunctionName: String
    private var currentDependNeedStatus: ULTPluginImplStatus

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
        val instance = if (tpl != null) {
            ULTInternalInjector.getGenericInstance(Key.get(tpl), false)
        } else {
            ULTInternalInjector.getInstance(clazz)
        }
        @Suppress("DEPRECATION_ERROR")
        if (instance is ULTAbstractDepend) {
            instance.currentImpl = this
            instance.context = ULTContext(plugin)
        }
        instance
    }

    fun ifStatus(status: ULTPluginImplStatus, ok: () -> Unit, listen: EventDelegate<ULTPluginImplStatus>) {
        if (this.status == status) {
            ok()
        } else {
            statusChangeEvent += listen
        }
    }

    val statusChangeEvent = Event<ULTPluginImplStatus>()

    init {
        currentFunctionName = "constructor"
        currentDependNeedStatus = ULTPluginImplStatus.READY
        clazz.kotlin.functions.forEach {
            it.findAnnotation<ULTLoad>()?.run {
                if (it.typeParameters.isNotEmpty()) {
                    logger.error("[${clazz.name}](${name}) ULTLoad annotate function cannot have any parameter")
                } else {
                    logger.info("[${clazz.name}](${name}) ULTLoad annotate function has registered")
                    loadCycle = LoadCycle(this, it)
                }
            }
            it.findAnnotation<ULTInit>()?.run {
                if (it.typeParameters.isNotEmpty()) {
                    logger.error("[${clazz.name}](${name}) ULTInit annotate function cannot have any parameter")
                } else {
                    logger.info("[${clazz.name}](${name}) ULTInit annotate function has registered")
                    initCycle = InitCycle(this, it)
                }
            }
            it.findAnnotation<ULTRun>()?.run {
                if (it.typeParameters.run {
                        when {
                            size > 1 -> {
                                logger.error("[${clazz.name}](${name}) ULTRun annotate function cannot have more than 1 parameter")
                                false
                            }
                            size == 1 -> {
                                val parameter = this[0] as KClass<*>
                                if (parameter != RunDelegate::class) {
                                    logger.error("[${clazz.name}](${name}) ULTRun annotate function just can use RunDelegate as parameter")
                                    false
                                } else {
                                    true
                                }
                            }
                            else -> true
                        }
                    }) {
                    logger.info("[${clazz.name}](${name}) ULTRun annotate function has registered")
                    runCycle = RunCycle(this, it)
                }
            }
            it.findAnnotation<ULTDestroy>()?.run {
                if (it.typeParameters.isNotEmpty()) {
                    logger.error("[${clazz.name}](${name}) ULTDestroy annotate function cannot have any parameter")
                } else {
                    logger.info("[${clazz.name}](${name}) ULTDestroy annotate function has registered")
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
                clazz.kotlin,
                name,
                "Multi $name function:[${value.function.name}] and [${field.function.name}]"
            )
        }
    }

    private fun checkStatus() {
        if (status == ULTPluginImplStatus.FAILED) {
            throw LifecycleException(
                clazz.kotlin,
                name,
                null,
                "Cannot $currentFunctionName because status is FAILED"
            )
        }
        if (status == ULTPluginImplStatus.DEPING) {
            throw LifecycleException(
                clazz.kotlin,
                name,
                null,
                "Cannot $currentFunctionName because status is DEPING, depend has be deadlock!"
            )
        }
    }

    internal fun getDependImpl(dependName: String): ULTPluginImpl {
        return ULTContextManager.getImpl(dependName) ?: throw LifecycleException(
            clazz.kotlin,
            name,
            null,
            "Cannot found depend name:${dependName}"
        )
    }

    internal fun getDependImpl(dependNamespace: String = plugin.namespace, dependName: String): ULTPluginImpl {
        return ULTContextManager.getImpl(dependNamespace, dependName) ?: throw LifecycleException(
            clazz.kotlin,
            name,
            null,
            "Cannot found depend name:${dependName}"
        )
    }

    internal fun getDependImpl(dependClazz: KClass<out Any>): ULTPluginImpl {
        return ULTContextManager.getImpl(dependClazz) ?: throw LifecycleException(
            clazz.kotlin,
            name,
            null,
            "Cannot found depend class:${dependClazz.qualifiedName}"
        )
    }

    internal fun solveDepend(
        pluginImpl: ULTPluginImpl
    ): ULTPluginImplStatusChain {
        return pluginImpl.let {
            val depStatus = when (currentFunctionName) {
                "init" -> {
                    it.init()
                }
                "run" -> {
                    it.run()
                }
                "destroy" -> {
                    it.destroy()
                }
                else -> {
                    success()
                }
            }
            if (depStatus.status) {
                checkDependStatus(it)
            } else {
                depStatus
            }
        }
    }

    private fun solveDepend(
        dependClasses: Array<KClass<*>>,
        dependNames: Array<String>
    ): ULTPluginImplStatusChain? {
        var failed: ULTPluginImplStatusChain? = null
        dependClasses.all { depClass ->
            val status = solveDepend(getDependImpl(depClass))
            if (!status.status) {
                failed = status
            }
            !status.status
        }
        dependNames.all { depName ->
            val status = solveDepend(getDependImpl(depName))
            if (!status.status) {
                failed = status
            }
            !status.status
        }
        return failed
    }

    internal fun checkDependStatus(
        pluginImpl: ULTPluginImpl
    ): ULTPluginImplStatusChain {
        return if (!pluginImpl.status.needStatus(currentDependNeedStatus)) {
            pluginImpl.failed(
                LifecycleException(
                    clazz.kotlin,
                    name,
                    null,
                    "Cannot $currentFunctionName depend class:${pluginImpl.clazz.name} because status need check failed: need[${status.name}] got[${pluginImpl.status.name}]"
                )
            )
        } else {
            pluginImpl.success()
        }
    }

    internal fun load() {
        withLifeCycleLock {
            currentFunctionName = "load"
            currentDependNeedStatus = ULTPluginImplStatus.LOADED
            try {
                if (status.needStatus(ULTPluginImplStatus.READY)) {
                    logger.info("load component ${clazz.name}(${name})")
                    loadCycle?.run {
                        checkStatus()
                        if (function.isSuspend) {
                            runBlocking {
                                function.callSuspend(instance)
                            }
                        } else {
                            function.call(instance)
                        }
                    }
                    status = ULTPluginImplStatus.LOADED
                }
            } catch (e: Throwable) {
                logger.warn(
                    "load component ${clazz.name}(${name}) failed because:${e.message}",
                    e
                )
                status = ULTPluginImplStatus.FAILED
            }
        }
    }

    internal fun init(): ULTPluginImplStatusChain {
        return withLifeCycleLock {
            currentFunctionName = "init"
            currentDependNeedStatus = ULTPluginImplStatus.INITED
            try {
                if (status.needStatus(ULTPluginImplStatus.LOADED)) {
                    if (status.level >= ULTPluginImplStatus.INITED.level) {
                        //由于被依赖关系,已经执行过了,无需再次执行
                        return@withLifeCycleLock success()
                    }
                    logger.info("init component ${clazz.name}(${name})")
                    initCycle?.run {
                        checkStatus()
                        val depStatus = solveDepend(annotation.dependClasses, annotation.dependNames)
                        if (depStatus != null && !depStatus.status) {
                            status = ULTPluginImplStatus.FAILED
                            return@withLifeCycleLock transfer(depStatus)
                        }
                        if (function.isSuspend) {
                            runBlocking { function.callSuspend(instance) }
                        } else {
                            function.call(instance)
                        }
                        status = ULTPluginImplStatus.INITED
                        return@withLifeCycleLock success()
                    } ?: run {
                        status = ULTPluginImplStatus.INITED
                        return@withLifeCycleLock success()
                    }
                } else {
                    return@withLifeCycleLock failed("status check failed")
                }
            } catch (e: Throwable) {
                logger.warn(
                    "init component ${clazz.name}(${name}) failed because:${e.message}",
                    e
                )
                status = ULTPluginImplStatus.FAILED
                return@withLifeCycleLock failed(e)
            }
        }
    }

    internal fun run(): ULTPluginImplStatusChain {
        return withLifeCycleLock {
            currentFunctionName = "run"
            currentDependNeedStatus = ULTPluginImplStatus.RUNNING
            try {
                if (status.needStatus(ULTPluginImplStatus.INITED)) {
                    if (status.level >= ULTPluginImplStatus.RUNNING.level) {
                        //由于被依赖关系,已经执行过了,无需再次执行
                        return@withLifeCycleLock success()
                    }
                    logger.info("run component ${clazz.name}(${name})")
                    runCycle?.run {
                        checkStatus()
                        val depStatus = solveDepend(annotation.dependClasses, annotation.dependNames)
                        if (depStatus != null && !depStatus.status) {
                            status = ULTPluginImplStatus.FAILED
                            return@withLifeCycleLock transfer(depStatus)
                        }
                        val runDelegate = object : RunDelegate {
                            private val channel = Channel<ULTPluginImplStatusChain>(1)
                            suspend fun finish(): ULTPluginImplStatusChain {
                                return channel.receive()
                            }

                            override fun runFinish() {
                                status = ULTPluginImplStatus.RUNNING
                                runBlocking { channel.send(success()) }
                            }

                            override fun runError(e: Exception?, message: String?) {
                                status = ULTPluginImplStatus.FAILED
                                runBlocking { channel.send(failed(e, message)) }
                            }
                        }

                        suspend fun callFunctionSuspend(): ULTPluginImplStatusChain {
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
                                status = ULTPluginImplStatus.RUNNING
                                success()
                            }
                        }

                        fun callFunction(): ULTPluginImplStatusChain {
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
                                status = ULTPluginImplStatus.RUNNING
                                success()
                            }
                        }
                        return@withLifeCycleLock if (function.isSuspend) {
                            runBlocking { callFunctionSuspend() }
                        } else {
                            callFunction()
                        }

                    } ?: run {
                        status = ULTPluginImplStatus.RUNNING
                        return@withLifeCycleLock success()
                    }
                } else {
                    return@withLifeCycleLock failed("status check failed")
                }
            } catch (e: Throwable) {
                logger.warn(
                    "run component ${clazz.name}(${name}) failed because:${e.message}",
                    e
                )
                status = ULTPluginImplStatus.FAILED
                return@withLifeCycleLock failed(e)
            }
        }
    }

    internal fun destroy(): ULTPluginImplStatusChain {
        return withLifeCycleLock {
            currentFunctionName = "destroy"
            currentDependNeedStatus = ULTPluginImplStatus.DESTROYED
            logger.info("destroy component ${clazz.name}(${name})")
            try {
                if (status.level == ULTPluginImplStatus.DESTROYED.level) {
                    //由于被依赖关系,已经执行过了,无需再次执行
                    return@withLifeCycleLock success()
                }
                destroyCycle?.run {
                    checkStatus()
                    val depStatus = solveDepend(annotation.dependClasses, annotation.dependNames)
                    if (depStatus != null && !depStatus.status) {
                        status = ULTPluginImplStatus.FAILED
                        return@withLifeCycleLock transfer(depStatus)
                    }
                    runBlocking {
                        if (function.isSuspend) {
                            function.callSuspend(instance)
                        } else {
                            function.call(instance)
                        }
                    }
                    status = ULTPluginImplStatus.DESTROYED
                    return@withLifeCycleLock success()
                } ?: run {
                    status = ULTPluginImplStatus.DESTROYED
                    return@withLifeCycleLock success()
                }
            } catch (e: Throwable) {
                status = ULTPluginImplStatus.FAILED
                return@withLifeCycleLock failed(e)
            }
        }
    }


    override fun compareTo(other: ULTPluginImpl): Int {
        return this.order.compareTo(other.order)
    }

    override fun hashCode(): Int {
        return clazz.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is ULTPluginImpl && clazz == other.clazz
    }

    override fun toString(): String {
        return "ULTPluginImpl(annotation=$annotation, clazz='${clazz.name}', plugin='${plugin.name}', interfaces=${interfaces.map { it.clazz.name }})"
    }


    enum class ULTPluginImplStatus(val level: Int) {
        DEPING(1),
        FAILED(2),
        DESTROYED(3),
        READY(4),
        LOADED(5),
        INITED(6),
        RUNNING(7),

        ;

        fun needStatus(target: ULTPluginImplStatus): Boolean {
            return this.level >= target.level
        }
    }
}

class ULTPlugin(
    val namespace: String,
    val name: String,
    val version: String,
    val versionCode: Int,
    val packages: List<String>,
    val registeredInterfaces: List<ULTPluginInterface> = LinkedList(),
    val registeredImpls: List<ULTPluginImpl> = LinkedList(),
    val classLoaders: List<ClassLoader> = LinkedList()
) {
    override fun hashCode(): Int {
        return namespace.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return namespace == other
    }

    override fun toString(): String {
        return "ULTPlugin(namespace='$namespace', name='$name', version='$version', versionCode=$versionCode, packages=$packages, registeredInterfaces=$registeredInterfaces, registeredImpls=$registeredImpls, classLoaders=${classLoaders.map { "[${it.name}]${it}" }})"
    }
}

