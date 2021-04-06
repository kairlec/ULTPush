package com.kairlec.ultpush.core.receiver

import com.kairlec.ultpush.ULTAbstractDepend
import com.kairlec.ultpush.bind.ULTInterface
import com.kairlec.ultpush.core.Authenticate
import com.kairlec.ultpush.core.Filter

/**
 * 定义push的消息接收器
 */
@ULTInterface(30)
abstract class Receiver<T : ReceiverMsg> : Authenticate<T>, Filter<T>, ULTAbstractDepend() {
    /**
     * 接收器的名称
     */
    open val name: String = "[Receiver]unnamed@${hashCode()}"

    override fun allow(content: T) = true

    override fun toString(): String {
        return name
    }

}
