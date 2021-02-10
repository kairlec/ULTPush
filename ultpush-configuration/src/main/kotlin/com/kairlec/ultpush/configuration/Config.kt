package com.kairlec.ultpush.configuration

interface Config {
    fun getChild(name: String): Config?
    fun getChild(index: Int): Config?

    operator fun get(name: String) = getChild(name)
    operator fun get(index: Int) = getChild(index)

    fun get(name: String, event: Config.() -> Unit): Config?
    fun get(index: Int, event: Config.() -> Unit): Config?

    val type: ConfigType
    val data: Any?

    fun <T> getData(clazz: Class<T>): T

    val arrayValue: Iterable<Config>?
    val objectValue: Config?
    val stringValue: String?
    val booleanValue: Boolean?
    val integerValue: Int?
    val floatValue: Float?
    val binaryValue: ByteArray?

    fun <T> ifArray(event: Iterable<Config>.() -> T) = arrayValue?.let(event)

    fun <T> ifObject(event: Config.() -> T) = objectValue?.let(event)

    fun <T> ifString(event: String.() -> T) = stringValue?.let(event)

    fun <T> ifBoolean(event: Boolean.() -> T) = booleanValue?.let(event)

    fun <T> ifInteger(event: Int.() -> T) = integerValue?.let(event)

    fun <T> ifFloat(event: Float.() -> T) = floatValue?.let(event)

    fun <T> ifBinary(event: ByteArray.() -> T) = binaryValue?.let(event)
}