package com.kairlec.ultpush.http

import java.lang.Exception

sealed class HttpException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
}

sealed class ResponseException private constructor(
    val statusCode: HttpStatusCode,
    message: String? = null,
    cause: Throwable? = null,
) : HttpException(message, cause)

class MethodNotSupportException(val method: String, override val message: String = "Method $method not support") :
    HttpException(message)

class HttpResponseFinishedException(statusCode: HttpStatusCode, override val message: String? = null) :
    ResponseException(statusCode, message, null)

/**
 * 错误的ContentType格式
 * @param value ContentType值
 */
class BadContentTypeFormatException(value: String) : HttpException("Bad Content-Type format: $value")