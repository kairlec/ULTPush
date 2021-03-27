package com.kairlec.ultpush.core.filter

import com.kairlec.ultpush.bind.ULTInterface

@ULTInterface
interface Filter<T : Any> {
    val scope: FilterScope
    fun allow(content: T): Boolean
}

enum class FilterScope {
    PUSHER,
    RECEIVER,
    HANDLER,

    ;

}