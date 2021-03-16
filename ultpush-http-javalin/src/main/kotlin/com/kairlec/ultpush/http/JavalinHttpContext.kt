package com.kairlec.ultpush.http

import io.javalin.http.Context
import io.javalin.http.util.ContextUtil
import io.javalin.http.util.ContextUtil.splitKeyValueStringAndGroupByKey
import io.javalin.http.util.MultipartUtil
import java.io.InputStream
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JavalinHttpContext(private val context: Context, override var scope: HttpScope) : HttpContext {
    override fun getMultiPartFiles(filename: String): List<PartFile> {
        return context.uploadedFiles(filename).map { JavalinPartFile(it) }
    }

    override val request: HttpServletRequest
        get() = context.req

    override val response: HttpServletResponse
        get() = context.res

    override val contentLength: Long
        get() = context.contentLength().toLong()
    override val headersMap: Map<String, Enumeration<String>> =
        headerNames.asSequence().associateWith { getHeaders(it) }
    override val headerMap: Map<String, String> = context.headerMap()
    override val queryStringMap: Map<String, List<String>> get() = context.queryParamMap()
    override val cookieMap: Map<String, String> get() = context.cookieMap()
    override val formParamMap: Map<String, List<String>> get() = context.formParamMap()

    override fun getFormParam(key: String): String? {
        return context.formParam(key)
    }

    override fun getFormParam(key: String, default: String): String {
        return context.formParam(key, default)!!
    }

    override fun getFormParams(key: String): List<String> {
        return context.formParams(key)
    }

    override fun getQueryParam(key: String): String? {
        return context.queryParam(key)
    }

    override fun getQueryParam(key: String, default: String): String {
        return context.queryParam(key, default)!!
    }

    override fun getQueryParams(key: String): List<String> {
        return context.queryParams(key)
    }

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

