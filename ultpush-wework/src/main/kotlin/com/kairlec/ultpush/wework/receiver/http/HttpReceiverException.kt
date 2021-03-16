package com.kairlec.ultpush.wework.receiver.http

import java.lang.Exception
import java.lang.reflect.Type

sealed class HttpReceiverException(
    override val message: String?,
    override val cause: Throwable?
) :
    Exception(message, cause)

class MissingRequestParamException(
    val paramName: String,
    override val message: String? = null,
    override val cause: Throwable? = null
) : HttpReceiverException(message, cause)