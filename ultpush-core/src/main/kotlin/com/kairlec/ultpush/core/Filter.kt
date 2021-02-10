package com.kairlec.ultpush.core

/**
 * 过滤器
 * 此接口允许对内容进行过滤,以判断是否需要接受相关内容
 */
interface Filter<T : Any> {
    /**
     * 是否允许内容,为true则接收内容,为false则不接受内容
     */
    fun allow(content: T): Boolean
}