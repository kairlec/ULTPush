package com.kairlec.ultpush.http

import com.kairlec.ultpush.http.HttpCommon.HTML_CT
import com.kairlec.ultpush.http.HttpCommon.LOCATION_HD
import com.kairlec.ultpush.http.HttpCommon.UA_HD
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*

interface HttpContext {
    var scope: HttpScope
    val request: HttpServletRequest
    val response: HttpServletResponse

    /////////////////////////////////////
    // Request properties and function //
    /////////////////////////////////////

    val requestCharset: Charset
        get() = request.characterEncoding?.let { Charset.forName(it) } ?: Charset.defaultCharset()
    val contentLength: Long
    var contentType: String?
        get() = request.contentType
        set(value) {
            response.contentType = value
        }
    val headerNames: Enumeration<String> get() = request.headerNames
    val formParamMap: Map<String, List<String>>
    val queryStringMap: Map<String, List<String>>
    val headersMap: Map<String, Enumeration<String>>
    val headerMap: Map<String, String>
    val host: String? get() = request.remoteHost
    val address: String get() = request.remoteAddr
    val method: HttpMethod get() = request.ultMethod
    val port: Int get() = request.serverPort
    val protocol: String get() = request.protocol
    val queryString: String? get() = request.queryString
    val scheme: String get() = request.scheme
    val session: HttpSession get() = request.session
    val url: String get() = request.requestURL.toString()
    val fullUrl: String get() = queryString?.let { "$url?$it" } ?: url
    val contextPath: String get() = request.contextPath
    val userAgent: String? get() = request.getHeader(UA_HD)
    val cookieMap: Map<String, String>
    fun <T> getBody(clazz: Class<T>): T
    fun <T> getBodyOrNull(clazz: Class<T>): T?
    fun <T> deserialize(clazz: Class<T>): T?
    fun getHeader(key: String): String? = request.getHeader(key)
    fun getHeaders(key: String): Enumeration<String> = request.getHeaders(key)
    fun getQueryParams(key: String): List<String> = queryStringMap[key] ?: emptyList()
    fun getQueryParam(key: String, default: String) = getQueryParam(key) ?: default
    fun getQueryParam(key: String) = getQueryParams(key).firstOrNull()
    fun getFormParam(key: String) = getFormParams(key).firstOrNull()
    fun getFormParam(key: String, default: String) = getFormParam(key) ?: default
    fun getFormParams(key: String) = formParamMap[key] ?: emptyList()
    fun getRequestParam(key: String): String? = getFormParam(key) ?: getQueryParam(key)
    fun getRequestParam(key: String, default: String): String = getRequestParam(key) ?: default
    fun getCookie(name: String): String? = cookieMap[name]
    fun getRawCookie(name: String): Cookie? = request.cookies?.find { name == it.name }
    fun getMultiPartFile(filename: String): PartFile? = getMultiPartFiles(filename).firstOrNull()
    fun getMultiPartFiles(filename: String): List<PartFile>

    fun setRequestAttribute(name: String, value: Any) = request.setAttribute(name, value)
    fun getRequestAttribute(name: String): Any? = request.getAttribute(name)
    fun setSessionAttribute(name: String, value: Any) = session.setAttribute(name, value)
    fun getSessionAttribute(name: String): Any? = session.getAttribute(name)

    //////////////////////////////////////
    // Response properties and function //
    //////////////////////////////////////
    val outputStream get() = response.outputStream
    var responseCharset: Charset
        get() = Charset.forName(request.characterEncoding) ?: Charset.defaultCharset()
        set(value) {
            response.characterEncoding = value.name()
        }

    fun redirect(location: String, statusCode: Int = SC_MOVED_TEMPORARILY) {
        response.setHeader(LOCATION_HD, location)
        response.status = statusCode
        if (scope == HttpScope.BEFORE) {
            throw HttpResponseFinishedException(statusCode)
        }
    }

    var statusCode: Int
        get() = response.status
        set(value) {
            response.status = value
        }

    fun setHeader(key: String, value: String) = response.setHeader(key, value)
    fun setCookie(cookie: Cookie) {
        cookie.path = cookie.path ?: "/"
        response.addCookie(cookie)
    }

    fun setCookie(key: String, value: String, maxAge: Int = -1) {
        setCookie(Cookie(key, value).apply { setMaxAge(maxAge) })
    }

    fun removeCookie(key: String, path: String? = null) {
        response.addCookie(Cookie(key, "").apply {
            this.path = path
            this.maxAge = 0
        })
    }

    fun html(html: String) {
        response.contentType = HTML_CT
        response.outputStream.use { it.print(html) }
        response.outputStream.flush()
    }

    fun json(obj: Any)

    fun out(inputStream: InputStream) {
        inputStream.transferTo(response.outputStream)
    }

    fun out(byteArray: ByteArray) {
        response.outputStream.write(byteArray)
    }

}

