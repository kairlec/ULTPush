package com.kairlec.ultpush.core.util

import java.io.InputStream
import java.net.URL

object ClassPathResources {
    private val contextClassLoader get() = Thread.currentThread().contextClassLoader

    private val currentJavaClass get() = ClassPathResources::class.java

    fun loads(path: String) = loads(path, contextClassLoader)

    fun loadsAsStream(path: String) = loadsAsStream(path, contextClassLoader)

    fun loadsAsStream(path: String, classLoader: ClassLoader) = classLoader.getResourceAsStream(path.trimStart('/'))

    fun loads(path: String, classLoader: ClassLoader) = classLoader.getResources(path.trimStart('/'))

    fun load(path: String, clazz: Class<*>): URL? {
        return if (path.startsWith("/")) {
            clazz.getResource(path)
        } else {
            clazz.getResource("/$path")
        }
    }

    fun load(path: String) = load(path, currentJavaClass)

    fun loadAsStream(path: String, clazz: Class<*>): InputStream? {
        return if (path.startsWith("/")) {
            clazz.getResourceAsStream(path)
        } else {
            clazz.getResourceAsStream("/$path")
        }
    }

    fun loadAsStream(path: String) = loadAsStream(path, currentJavaClass)
}