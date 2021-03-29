@file:Suppress("UNCHECKED_CAST")

package com.kairlec.ultpush.bind

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import com.google.inject.name.Names
import com.kairlec.ultpush.ULTContextManager
import com.kairlec.ultpush.plugin.ULTPlugin
import com.kairlec.ultpush.plugin.ULTPluginImpl
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
import kotlin.concurrent.thread
import kotlin.reflect.full.*

/**
 * 这个Module用来绑定所有的[ULTImpl]与[ULTInterface]的信息
 */
class ULTInternalModule : AbstractModule() {
    private val logger = LoggerFactory.getLogger(javaClass)

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
        plugin: ULTPlugin
    ): ULTPluginImpl? {
        return intf.getAnnotation(ULTInterface::class.java)?.let {
            val pluginInterface = ULTContextManager.submit(it, intf, plugin)
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
            //val component = ULTComponentLifecycle.register(impl, tpl, name, order)
            logger.info(msg)
            ULTContextManager.submit(implAnn, impl, plugin, tpl, name, order, pluginInterface)
            //ULTPluginImpl(implAnn, impl, plugin, component)
        }
    }

    private fun checkBindClass(
        impl: Class<out Any>,
        checkCurrent: Class<out Any>,
        pluginName: String?,
        implAnn: ULTImpl,
        plugin: ULTPlugin,
        //list: MutableList<ULTPluginImpl>
    ) {
        checkCurrent.interfaces.forEach {
            checkBindClass(impl, it, pluginName, implAnn, plugin/*, list*/)
            tryBind(
                impl,
                it as Class<Any>,
                pluginName,
                implAnn,
                plugin
            )/*?.run {
                list.add(this)
            }*/
        }
        checkCurrent.superclass?.let {
            checkBindClass(impl, it, pluginName, implAnn, plugin/*, list*/)
            tryBind(
                impl,
                it as Class<Any>,
                pluginName,
                implAnn,
                plugin
            )/*?.run {
                list.add(this)
            }*/
        }
    }

    override fun configure() {
        // 当使用-cp来指定加载插件的时候,所有的资源将都被加载进ClassPath
        ClasspathHelper.contextClassLoader().getResources("ultpush-plugin.properties").asIterator().forEach { url ->
            try {
                val content = url.readText()
                val pluginProperties = Properties()
                url.openStream().use {
                    pluginProperties.load(it)
                }
                //val pluginInfo = objectMapper.readTree(content)
                val pluginNamespace = pluginProperties.getProperty("namespace")
                    ?: throw UnsupportedOperationException("Cannot access plugin namespace in content:${content}")
                val pluginName = pluginProperties.getProperty("name")
                    ?: throw UnsupportedOperationException("Cannot access plugin name in content:${content}")
                val pluginScanPackages = pluginProperties.getProperty("package")?.split(',')
                    ?: throw UnsupportedOperationException("Cannot access plugin package in content:${content}")
                val builder = ConfigurationBuilder()
                pluginScanPackages.map { packageName ->
                    builder.addUrls(
                        ClasspathHelper.forPackage(
                            packageName,
                            ClasspathHelper.contextClassLoader(),
                            ClasspathHelper.staticClassLoader()
                        )
                    )
                }
                builder.addScanners(SubTypesScanner(), TypeAnnotationsScanner())
                val reflections = Reflections(builder)
                val classes = reflections.getTypesAnnotatedWith(ULTImpl::class.java)
//                val plugin = ULTPlugin.build {
//                    name = pluginName
//                    packages.addAll(pluginScanPackages)
//                    pluginInfo["version"]?.textValue()?.let {
//                        version = it
//                    }
//                    pluginInfo["versionCode"]?.intValue()?.let {
//                        versionCode = it
//                    }
//                    classLoaders.add(ClasspathHelper.contextClassLoader())
//                    classLoaders.add(ClasspathHelper.staticClassLoader())
//                }
                val plugin = ULTContextManager.submit(
                    pluginNamespace,
                    pluginName,
                    pluginProperties.getProperty("version"),
                    pluginProperties["versionCode"]?.let {
                        when (it) {
                            is Int ->
                                it
                            is String ->
                                it.toIntOrNull()
                            else ->
                                null
                        }
                    },
                    pluginScanPackages.toMutableList(),
                    mutableListOf(ClasspathHelper.contextClassLoader(), ClasspathHelper.staticClassLoader())
                )
                classes.forEach {
                    val ann = it.getDeclaredAnnotation(ULTImpl::class.java)
                    //val implList = LinkedList<ULTPluginImpl>()
                    checkBindClass(it, it, pluginName, ann, plugin/*, implList*/)
                    //(plugin.registeredImpls as MutableList).addAll(implList)
                }
                logger.info(plugin.toString())
            } catch (e: Throwable) {
                logger.error("load error:${e.message}", e)
            }
        }
        // 先加载ClassPath内的,再加载Plugins插件文件夹内的
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
        logger.info("------------")
        logger.info(ULTContextManager.plugins.values.toString())
        logger.info(ULTContextManager.impls.values.toString())
        logger.info(ULTContextManager.interfaces.values.toString())
    }

    private fun loadJar(pathToJar: File) {
        try {
            logger.info("Loading plugin file:${pathToJar.nameWithoutExtension} ...")
            val jarFile = JarFile(pathToJar)
            val pluginJson = jarFile.getJarEntry("ultpush-plugin.properties")
                ?: run {
                    error("Cannot access 'ultpush-plugin.properties' in file:${pathToJar.nameWithoutExtension}")
                }
            val pluginProperties = Properties()
            jarFile.getInputStream(pluginJson).use {
                it.reader(Charsets.UTF_8).use { isr ->
                    pluginProperties.load(isr)
                }
            }
            val fileUrl = pathToJar.toURI().toURL()
            val child = URLClassLoader(arrayOf(fileUrl), Thread.currentThread().contextClassLoader)

            //val pluginInfo = objectMapper.readTree(pluginText)

            val pluginNamespace = pluginProperties.getProperty("namespace")
                ?: error("Cannot access plugin namespace in plugin info:${pathToJar.nameWithoutExtension}")
            val pluginName = pluginProperties.getProperty("name")
                ?: run {
                    logger.warn("[file:${pathToJar.nameWithoutExtension}]pluginInfo don't exist name,use the file name as plugin name")
                    pathToJar.nameWithoutExtension
                }
            val pluginScanPackages = pluginProperties.getProperty("package")?.split(',')
                ?: throw UnsupportedOperationException("${pluginName.currentLocation}Cannot access plugin package in plugin info")
            val plugin = ULTContextManager.submit(
                pluginNamespace,
                pluginName,
                pluginProperties.getProperty("version"),
                pluginProperties["versionCode"]?.let {
                    when (it) {
                        is Int -> it
                        is String -> it.toIntOrNull()
                        else -> null
                    }
                },
                pluginScanPackages.toMutableList(),
                mutableListOf(child)
            )
            val packageDirName = pluginScanPackages.map { it.replace(".", "/") }
//            val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
//            println(runtimeMXBean.name)
//            logger.info(child.name)
//            logger.info(child.toString())
            thread(start = true, isDaemon = true, child, "Plugin Loader Thread") {
                val e: Enumeration<JarEntry> = jarFile.entries()
                while (e.hasMoreElements()) {
                    val je: JarEntry = e.nextElement()
                    val name = je.name.removePrefix("/")
                    if (je.isDirectory || !name.endsWith(".class") || packageDirName.all { !name.startsWith(it) }) {
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
                        ULTContextManager.submit(ann, clazz, plugin)
                    } else if (clazz.isAnnotationPresent(ULTImpl::class.java)) {
                        logger.info("${pluginName.currentLocation}Load ULTImpl '${clazz.name}'")
                        val ann = clazz.getDeclaredAnnotation(ULTImpl::class.java)
                        //val list = LinkedList<ULTPluginImpl>()
                        checkBindClass(clazz, clazz, pluginName, ann, plugin/*, list*/)
                        //(plugin.registeredImpls as MutableList).addAll(list)
                    }
                }
                logger.info("Load plugin file ${pathToJar.nameWithoutExtension} success")
            }.join()
            logger.info(plugin.toString())
            logger.info("Load plugin file ${pathToJar.nameWithoutExtension} success")
        } catch (e: Throwable) {
            logger.error("Load plugin file ${pathToJar.nameWithoutExtension} failed:${e.message}", e)
        }
    }

}