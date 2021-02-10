package com.kairlec.ultpush.http

import javax.servlet.http.HttpServletRequest

enum class HttpMethod {
    GET,
    POST,
    DELETE,
    PATCH,
    PUT,
    HEAD,
    CONNECT,
    OPTIONS,
    TRACE,
    ;

    companion object {
        fun parse(method: String): HttpMethod {
            val m = method.trim()
            values().forEach {
                if (it.name.equals(m, true)) {
                    return it
                }
            }
            throw MethodNotSupportException(method)
        }
    }
}

internal val HttpServletRequest.ultMethod: HttpMethod
    get() = HttpMethod.parse(this.method)