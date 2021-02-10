package com.kairlec.ultpush.core.pusher

import com.kairlec.ultpush.core.Authenticate
import com.kairlec.ultpush.core.Filter

abstract class Pusher : Authenticate<PusherMsg>, Filter<PusherMsg> {
    /**
     * 推送器的名称
     */
    open val name: String = "[Pusher]unnamed@${hashCode()}"

    override fun allow(content: PusherMsg) = true

    override fun toString(): String {
        return name
    }
}
