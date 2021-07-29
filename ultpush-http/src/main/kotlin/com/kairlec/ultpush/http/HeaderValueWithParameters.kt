package com.kairlec.ultpush.http

import java.nio.charset.Charset
import java.util.*

/**
 * 详看[RFC2616#2.2](https://datatracker.ietf.org/doc/html/rfc2616#section-2.2)
 */
internal val HeaderFieldValueSeparators =
    setOf('(', ')', '<', '>', '@', ',', ';', ':', '\\', '\"', '/', '[', ']', '?', '=', '{', '}', ' ', '\t', '\n', '\r')

/**
 * 单个的参数
 * @property name 参数名称
 * @property value 参数值
 */
data class HeaderValueParam(val name: String, val value: String) {
    override fun equals(other: Any?): Boolean {
        return other is HeaderValueParam &&
                other.name.equals(name, ignoreCase = true) &&
                other.value.equals(value, ignoreCase = true)
    }

    override fun hashCode(): Int {
        var result = name.lowercase(Locale.getDefault()).hashCode()
        result += 31 * result + value.lowercase(Locale.getDefault()).hashCode()
        return result
    }
}

/**
 * 表示[content] + [parameters] 的Header值,比如`Content-Type`, `Content-Disposition`
 *
 * 例子: application/json; charset=utf-8
 *
 * @property content header值内容(不带参数)
 * @property parameters 参数
 */
abstract class HeaderValueWithParameters(
    protected val content: String,
    val parameters: List<HeaderValueParam> = emptyList()
) {
    fun parameter(name: String): String? =
        parameters.firstOrNull { it.name.equals(name, ignoreCase = true) }?.value

    override fun toString(): String = when {
        parameters.isEmpty() -> content
        else -> {
            val size = content.length + parameters.sumOf { it.name.length + it.value.length + 3 }
            StringBuilder(size).apply {
                append(content)
                for (element in parameters) {
                    val (name, value) = element
                    append("; ")
                    append(name)
                    append("=")
                    value.escapeIfNeededTo(this)
                }
            }.toString()
        }
    }

    companion object {
        inline fun <R> parse(value: String, init: (String, List<HeaderValueParam>) -> R): R {
            val headerValue = parseHeaderValue(value).single()
            return init(headerValue.value, headerValue.params)
        }
    }
}

fun HeaderValueWithParameters.charset(): Charset? = parameter("charset")?.let {
    try {
        Charset.forName(it)
    } catch (exception: IllegalArgumentException) {
        null
    }
}