package com.kairlec.ultpush.http

enum class HttpScope {
    /**
     * 在处理前拦截
     */
    BEFORE,

    /**
     * 正常处理
     */
    NORMAL,

    /**
     * 处理后拦截
     */
    AFTER,

    /**
     * 发生未捕获的异常
     */
    EXCEPTION,

    /**
     * 发生错误
     */
    ERROR
}
