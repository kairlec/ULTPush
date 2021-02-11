@file:Suppress("UNCHECKED_CAST")

package com.kairlec.ultpush.bind

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Singleton
import com.google.inject.TypeLiteral
import com.google.inject.name.Names
import com.kairlec.ultpush.component.ULTComponentLifecycle
import com.kairlec.ultpush.component.ULTInterfaceType
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URLClassLoader
import java.util.jar.JarFile

import java.util.*
import java.util.jar.JarEntry
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

class ULTModule : AbstractModule() {
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
        singleton: Boolean
    ) {
        if (intf.isAnnotationPresent(ULTInterface::class.java)) {
            val name = if (implAnn.name.isEmpty()) {
                impl.name
            } else {
                implAnn.name
            }
            impl.kotlin.objectInstance?.let {
                requestStaticInjection(it::class.java)
            }
            var tpl: TypeLiteral<Any>? = null
            intf.kotlin.memberProperties.forEach {
                it.findAnnotation<ULTInterfaceType>()?.run {
                    val type = it.get(impl) ?: run {
                        logger.error("error to bind TypeLiteral because of null")
                        return
                    }
                    if (type is TypeLiteral<*>) {
                        tpl = type as TypeLiteral<Any>
                    }
                }
            }
            val msg = buildString {
                if (tpl != null) {
                    bind(tpl).annotatedWith(Names.named(name)).to(impl)
                    append("${pluginName.currentLocation}Bind '${tpl!!.rawType.name}' to '${impl.name}'[with name '${name}']")
                } else {
                    bind(intf).annotatedWith(Names.named(name)).to(impl)
                    append("${pluginName.currentLocation}Bind '${intf.name}' to '${impl.name}'[with name '${name}']")
                }
                if (implAnn.default) {
                    if (interfaceMap.containsKey(intf.name)) {
                        append("[default failed because has bound with '${interfaceMap[intf.name]}']")
                        logger.warn("${pluginName.currentLocation}Bind '${intf.name}' to '${impl.name}' default failed because has bound with '${interfaceMap[intf.name]}'")
                    } else {
                        interfaceMap[intf.name] = impl.name
                        if (tpl != null) {
                            bind(tpl).to(impl)
                        } else {
                            bind(intf).to(impl)
                        }
                        append("[default]")
                    }
                }
                if (singleton) {
                    append("[singleton]")
                }
            }
            ULTComponentLifecycle.register(impl.kotlin, name)
            logger.info(msg)
        }
    }

    private fun checkBindClass(impl: Class<out Any>, pluginName: String?, implAnn: ULTImpl) {
        impl.interfaces.forEach {
            tryBind(
                impl,
                it as Class<Any>,
                pluginName,
                implAnn,
                impl.isAnnotationPresent(Singleton::class.java) || impl.isAnnotationPresent(javax.inject.Singleton::class.java)
            )
        }
        impl.superclass?.let {
            checkBindClass(it, pluginName, implAnn)
            tryBind(
                impl,
                it as Class<Any>,
                pluginName,
                implAnn,
                impl.isAnnotationPresent(Singleton::class.java) || impl.isAnnotationPresent(javax.inject.Singleton::class.java)
            )
        }
    }

    override fun configure() {
        val builder = ConfigurationBuilder()
        builder.addUrls(
            ClasspathHelper.forPackage(
                "com.kairlec.ultpush",
                ClassLoader.getSystemClassLoader(),
                ClasspathHelper.contextClassLoader(),
                ClasspathHelper.staticClassLoader()
            )
        )
        builder.addScanners(SubTypesScanner(), TypeAnnotationsScanner())
        val reflections = Reflections(builder)
        val classes = reflections.getTypesAnnotatedWith(ULTImpl::class.java)
        classes.forEach {
            val ann = it.getDeclaredAnnotation(ULTImpl::class.java)
            checkBindClass(it, null, ann)
        }
        // 先加载自带的,再加载插件
        loadPlugin()
    }

    private fun loadPlugin() {
        File("plugins").apply {
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
            val child = URLClassLoader(arrayOf(fileUrl), javaClass.classLoader)
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
                val clazz = Class.forName(className, true, child)
                logger.debug("${pluginName.currentLocation}Load class '${clazz.name}'")
                if (clazz.isAnnotationPresent(ULTInterface::class.java)) {
                    val ann = clazz.getDeclaredAnnotation(ULTInterface::class.java)
                    //TODO 保留ann额外内容
                    logger.info("${pluginName.currentLocation}Load ULTInterface '${clazz.name}'")
                } else if (clazz.isAnnotationPresent(ULTImpl::class.java)) {
                    val ann = clazz.getDeclaredAnnotation(ULTImpl::class.java)
                    checkBindClass(clazz, pluginName, ann)
                    logger.info("${pluginName.currentLocation}Load ULTImpl '${clazz.name}'")
                }
            }

            logger.info("Load plugin file ${pathToJar.nameWithoutExtension} success")
        } catch (e: Throwable) {
            logger.error("Load plugin file ${pathToJar.nameWithoutExtension} failed:${e.message}", e)
        }

    }

}