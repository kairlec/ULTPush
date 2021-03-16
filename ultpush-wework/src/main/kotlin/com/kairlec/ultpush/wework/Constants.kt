package com.kairlec.ultpush.wework

import com.kairlec.ultpush.wework.message.WeWorkMessage

fun WeWorkMessage.withData(data: Any) = apply {
    this.withData = data
}

fun WeWorkMessage.fromUser(fromUser: String) = apply {
    val filed = this::class.java.getField("fromUser")
    filed.isAccessible = true
    filed.set(this, fromUser)
}

fun WeWorkMessage.toUser(toUser: String) = apply {
    val filed = this::class.java.getField("toUser")
    filed.isAccessible = true
    filed.set(this, toUser)
}
