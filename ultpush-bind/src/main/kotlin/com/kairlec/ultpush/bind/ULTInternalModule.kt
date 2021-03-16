@file:Suppress("UNCHECKED_CAST")

package com.kairlec.ultpush.bind

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import com.google.inject.name.Names
import com.kairlec.ultpush.component.ULTComponentLifecycle
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.management.ManagementFactory
import java.net.URLClassLoader
import java.util.jar.JarFile

import java.util.*
import java.util.jar.JarEntry
import kotlin.concurrent.thread
import kotlin.reflect.full.*

/**
 * 这个Module用来绑定所有的[ULTImpl]与[ULTInterface]的信息
 */
class ULTInternalModule : AbstractModule() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper = jacksonObjectMapper()

    private val interfaceMap = HashMap<String, String>()

    private val String?.currentLocation
        get() = if (this == null) {
            "[Main]"
        } else {
            "[Plugin:${this}]"
        }

    private fun tryBind(
        impl: Class<out Any>,
        intf: Class<Any>,
        pluginName: String?,
        implAnn: ULTImpl,
    ) {
        intf.getAnnotation(ULTInterface::class.java)?.let {
            val order = it.order.coerceAtMost(implAnn.order)
            val name = if (implAnn.name.isEmpty()) {
                impl.name
            } else {
                implAnn.name
            }
            impl.kotlin.objectInstance?.let {
                requestStaticInjection(it::class.java)
            }
            var tpl: TypeLiteral<Any>? = null

            impl.kotlin.run {
                companionObjectInstance?.let { ccui ->
                    if (ccui is TypeLiteralAble) {
                        tpl = ccui.typeLiteral as TypeLiteral<Any>?
                    }
                }
            }
            val msg = buildString {
                if (tpl != null) {
                    bind(tpl).annotatedWith(Names.named(name)).to(impl).`in`(Scopes.SINGLETON)
                    append("${pluginName.currentLocation}Bind '${tpl!!.type.typeName}'(tpl) to '${impl.name}'[with name '${name}']")
                } else {
                    bind(intf).annotatedWith(Names.named(name)).to(impl).`in`(Scopes.SINGLETON)
                    append("${pluginName.currentLocation}Bind '${intf.name}' to '${impl.name}'[with name '${name}']")
                }
                if (implAnn.default) {
                    if (interfaceMap.containsKey(intf.name)) {
                        append("[default failed because has bound with '${interfaceMap[intf.name]}']")
                        logger.warn("${pluginName.currentLocation}Bind '${intf.name}' to '${impl.name}' default failed because has bound with '${interfaceMap[intf.name]}'")
                    } else {
                        interfaceMap[intf.name] = impl.name
                        if (tpl != null) {
                            bind(tpl).to(impl).`in`(Scopes.SINGLETON)
                        } else {
                            bind(intf).to(impl).`in`(Scopes.SINGLETON)
                        }
                        append("[default]")
                    }
                }
                bind(impl).`in`(Scopes.SINGLETON)
            }
            ULTComponentLifecycle.register(impl, tpl, name, order)
            logger.info(msg)
        }
    }

    private fun checkBindClass(
        impl: Class<out Any>,
        checkCurrent: Class<out Any>,
        pluginName: String?,
        implAnn: ULTImpl
    ) {
        checkCurrent.interfaces.forEach {
            checkBindClass(impl, it, pluginName, implAnn)
            tryBind(
                impl,
                it as Class<Any>,
                pluginName,
                implAnn
            )
        }
        checkCurrent.superclass?.let {
            checkBindClass(impl, it, pluginName, implAnn)
            tryBind(
                impl,
                it as Class<Any>,
                pluginName,
                implAnn
            )
        }
    }

    override fun configure() {
        val builder = ConfigurationBuilder()
        builder.addUrls(
            ClasspathHelper.forPackage(
                "com.kairlec.ultpush",
                ClasspathHelper.contextClassLoader(),
                ClasspathHelper.staticClassLoader()
            )
        )
        builder.addScanners(SubTypesScanner(), TypeAnnotationsScanner())
        val reflections = Reflections(builder)
        val classes = reflections.getTypesAnnotatedWith(ULTImpl::class.java)
        classes.forEach {
            val ann = it.getDeclaredAnnotation(ULTImpl::class.java)
            checkBindClass(it, it, null, ann)
        }
        // 先加载自带的,再加载插件
        logger.info("ready to load plugin")
        loadPlugin()
        logger.info("load plugin finished")
    }

    private fun loadPlugin() {
        File("plugins").apply {
            logger.info("plugin dir:${this.absolutePath}")
            parentFile?.run {
                if (!exists()) {
                    mkdirs()
                }
            } ?: run {
                if (exists()) {
                    mkdirs()
                }
            }
        }.listFiles { _, name ->
            name.endsWith(".jar")
        }?.forEach(::loadJar)
    }

    private fun loadJar(pathToJar: File) {
        try {
            logger.info("Loading plugin file:${pathToJar.nameWithoutExtension} ...")
            val jarFile = JarFile(pathToJar)
            val pluginJson = jarFile.getJarEntry("ultpush-plugin.json")
                ?: run {
                    error("Cannot access 'ultpush-plugin.json' in file:${pathToJar.nameWithoutExtension}")
                }
            val pluginText = jarFile.getInputStream(pluginJson).use {
                it.reader(Charsets.UTF_8).use { isr ->
                    isr.readText()
                }
            }
            val fileUrl = pathToJar.toURI().toURL()
            val child = URLClassLoader(arrayOf(fileUrl), Thread.currentThread().contextClassLoader)

            val pluginInfo = objectMapper.readTree(pluginText)

            val pluginName = pluginInfo["name"].asText()
                ?: run {
                    logger.warn("[file:${pathToJar.nameWithoutExtension}]pluginInfo don't exist name,use the file name as plugin name")
                    pathToJar.nameWithoutExtension
                }

            val packageName = pluginInfo["package"].asText() ?: run {
                error("${pluginName.currentLocation}Cannot access 'package' in plugin info")
            }
            val packageDirName = packageName.replace(".", "/")
            val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
            println(runtimeMXBean.name)
            logger.info(child.name)
            logger.info(child.toString())
            thread(start = true, isDaemon = true, child, "Plugin Loader Thread") {
                logger.info(Thread.currentThread().contextClassLoader.name)
                logger.info(Thread.currentThread().contextClassLoader.toString())
                val e: Enumeration<JarEntry> = jarFile.entries()
                while (e.hasMoreElements()) {
                    val je: JarEntry = e.nextElement()
                    val name = je.name.removePrefix("/")
                    if (je.isDirectory || !name.endsWith(".class") || !name.startsWith(packageDirName)) {
                        continue
                    }
                    // -6 because of .class and className is 'dot' for split
                    val className: String = name.substring(0, name.length - 6).replace('/', '.')
                    //must use child ClassLoader otherwise ClassNotFound
                    logger.info("${pluginName.currentLocation}Load class '${className}'")
                    val clazz = Class.forName(className, true, child)
                    if (clazz.isAnnotationPresent(ULTInterface::class.java)) {
                        logger.info("${pluginName.currentLocation}Load ULTInterface '${clazz.name}'")
                        //TODO 保留ann额外内容
                        val ann = clazz.getDeclaredAnnotation(ULTInterface::class.java)
                    } else if (clazz.isAnnotationPresent(ULTImpl::class.java)) {
                        logger.info("${pluginName.currentLocation}Load ULTImpl '${clazz.name}'")
                        val ann = clazz.getDeclaredAnnotation(ULTImpl::class.java)
                        checkBindClass(clazz, clazz, pluginName, ann)
                    }
                }
                logger.info("Load plugin file ${pathToJar.nameWithoutExtension} success")
            }.join()

            logger.info("Load plugin file ${pathToJar.nameWithoutExtension} success")
        } catch (e: Throwable) {
            logger.error("Load plugin file ${pathToJar.nameWithoutExtension} failed:${e.message}", e)
        }

    }

}