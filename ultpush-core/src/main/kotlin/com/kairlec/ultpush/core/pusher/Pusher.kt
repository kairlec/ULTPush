package com.kairlec.ultpush.core.pusher

import com.kairlec.ultpush.core.Authenticate
import com.kairlec.ultpush.core.Component

abstract class Pusher : Component, Authenticate<PusherMsg> {
    /**
     * 推送器的名称
     */
    open val name: String = "[Pusher]unnamed@${hashCode()}"

    override fun init() {}
    override fun load() {}
    override fun destroy() {}

    override fun toString(): String {
        return name
    }
}
