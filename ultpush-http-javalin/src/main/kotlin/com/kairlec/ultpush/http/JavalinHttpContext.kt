package com.kairlec.ultpush.http

import io.javalin.http.Context
import io.javalin.http.util.ContextUtil.splitKeyValueStringAndGroupByKey
import java.io.InputStream
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JavalinHttpContext(private val context: Context) : HttpContext {
    override val request: HttpServletRequest
        get() = context.req

    override val response: HttpServletResponse
        get() = context.res

    override val contentLength: Long
        get() = context.contentLength().toLong()
    override val headersMap: Map<String, Enumeration<String>> =
        headerNames.asSequence().associateWith { getHeaders(it) }
    override val headerMap: Map<String, String> = headerNames.asSequence().associateWith { getHeader(it)!! }
    override val queryStringMap: Map<String, List<String>> = splitKeyValueStringAndGroupByKey(
        queryString ?: "",
        requestCharset.name()
    )
    override val cookieMap: Map<String, String> = request.cookies?.associate { it.name to it.value } ?: emptyMap()

    override fun <T> getBody(clazz: Class<T>): T {
        return when {
            ByteArray::class.java.isAssignableFrom(clazz) -> context.bodyAsBytes() as T
            InputStream::class.java.isAssignableFrom(clazz) -> context.bodyAsInputStream() as T
            else -> context.bodyAsClass(clazz)
        }
    }

    override fun <T> getBodyOrNull(clazz: Class<T>): T? {
        return try {
            context.bodyAsClass(clazz)
        } catch (e: Throwable) {
            null
        }
    }

    override fun <T> deserialize(clazz: Class<T>): T? {
        return try {
            context.bodyAsClass(clazz)
        } catch (e: Throwable) {
            null
        }
    }

    override fun json(obj: Any) {
        context.json(obj)
    }
}

val Context.custom get() = JavalinHttpContext(this)