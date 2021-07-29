package com.kairlec.ultpush.http

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
