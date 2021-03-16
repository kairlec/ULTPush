package com.kairlec.ultpush.core

import java.lang.Exception


/**
 * 验证状态
 */

interface AuthenticateStatus<T : Any> {
    /**
     * 此次验证是否通过
     */
    val accept: Boolean

    /**
     * 验证状态码
     */
    val statusCode: Int

    /**
     * 验证消息
     */
    val message: String?

    /**
     * 验证内容
     */
    val body: T

}

/**
 * 验证成功
 */
open class AuthenticateSuccess<T : Any>(
    override val body: T,
    override val message: String? = null
) : AuthenticateStatus<T> {
    override val accept: Boolean = true
    override val statusCode: Int = 0
}

/**
 * 验证失败
 */
open class AuthenticateFailed<T : Any>(
    override val body: T,
    override val statusCode: Int = 1,
    override val message: String? = null
) : AuthenticateStatus<T> {
    override val accept: Boolean = false

    fun throwOut(): Nothing {
        throw AuthenticateException(this)
    }

    init {
        if (statusCode == 0) {
            throw IllegalArgumentException("statusCode '0' just for auth success")
        }
    }
}

class AuthenticateException(
    override val message: String?,
    override val cause: Throwable?,
    val body: Any,
    val statusCode: Int
) : Exception(message, cause) {
    constructor(
        authenticateStatus: AuthenticateStatus<out Any>,
        cause: Throwable? = null
    ) : this(authenticateStatus.message, cause, authenticateStatus.body, authenticateStatus.statusCode)
}