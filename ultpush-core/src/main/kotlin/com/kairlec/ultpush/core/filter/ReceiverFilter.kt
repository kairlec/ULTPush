package com.kairlec.ultpush.core.filter

import com.kairlec.ultpush.bind.ULTInterface
import com.kairlec.ultpush.core.receiver.ReceiverMsg

@ULTInterface
abstract class ReceiverFilter<T : ReceiverMsg> : Filter<T> {
    override val scope: FilterScope
        get() = FilterScope.RECEIVER
}