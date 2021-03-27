package com.kairlec.ultpush.core.filter

import com.kairlec.ultpush.bind.ULTInterface
import com.kairlec.ultpush.core.pusher.PusherMsg

@ULTInterface
abstract class PusherFilter<T : PusherMsg> : Filter<T> {
    override val scope: FilterScope
        get() = FilterScope.PUSHER
}