package com.kairlec.ultpush.core.receiver

import com.kairlec.ultpush.core.handler.MessageHandler

/**
 * 接收来的消息
 */
interface ReceiverMsg{
    /**
     * 允许超类处理
     * 比如若有[MessageHandler]所监听的类型为[ReceiverMsg]或其他当前类的父类,则也允许处理
     * 否则只有当前的[kotlin.reflect.KClass]完全相等的时候才允许推送
     */
    val allowSuperClassHandle: Boolean

}