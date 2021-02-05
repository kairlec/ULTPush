package com.kairlec.ultpush.core


/**
 * 验证状态
 */
interface AuthenticateStatus<T> {
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

    /**
     * 验证成功
     */
    class AuthenticateSuccess<T>(
        override val body: T,
        override val message: String? = null
    ) : AuthenticateStatus<T> {
        override val accept: Boolean = true
        override val statusCode: Int = 0
    }

    /**
     * 验证失败
     */
    class AuthenticateFailed<T>(
        override val body: T,
        override val statusCode: Int = 1,
        override val message: String? = null
    ) : AuthenticateStatus<T> {
        override val accept: Boolean = false

        init {
            if (statusCode == 0) {
                throw IllegalArgumentException("statusCode '0' just for auth success")
            }
        }
    }
}
