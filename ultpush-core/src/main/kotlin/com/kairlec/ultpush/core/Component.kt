package com.kairlec.ultpush.core

/**
 * 组件相关接口
 */
interface Component {
    /**
     * 加载组件
     */
    fun load()

    /**
     * 初始化组件
     */
    fun init()

    /**
     * 运行组件
     * 此运行可为阻塞函数,系统会自动异步运行
     */
    fun run()

    /**
     * 摧毁组件
     */
    fun destroy()
}