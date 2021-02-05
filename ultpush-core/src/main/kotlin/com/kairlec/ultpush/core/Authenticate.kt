package com.kairlec.ultpush.core

/**
 * 验证接口,允许通过此接口对内容进行验证
 */
interface Authenticate<T> {
    /**
     * 对请求来的内容进行验证
     */
    fun authenticate(body: T): AuthenticateStatus<T>
}

