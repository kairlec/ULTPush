package com.kairlec.ultpush.core.pusher

import com.kairlec.ultpush.core.Filterable
import com.kairlec.ultpush.core.MessageInfo

/**
 * 推送的消息
 */

interface PusherMsg : MessageInfo, Filterable {
    /**
     * 允许超类推送
     * 比如若有[Pusher]所监听的类型为[PusherMsg]或其他当前类的父类,则也允许推送
     * 否则只有当前的[kotlin.reflect.KClass]完全相等的时候才允许推送
     */
    val allowSuperClassPush: Boolean

}
