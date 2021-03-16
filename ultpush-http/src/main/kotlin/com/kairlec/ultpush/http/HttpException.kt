package com.kairlec.ultpush.http

import java.lang.Exception

sealed class HttpException : Exception {
    constructor() : super()

    constructor(
        message: String, cause: Throwable,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)

    constructor(cause: Throwable) : super(cause)

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(message: String) : super(message)
}

class MethodNotSupportException(val method: String, override val message: String = "Method $method not support") :
    HttpException(message)

class HttpResponseFinishedException(val statusCode: Int, override val message: String? = null) : Exception(message)