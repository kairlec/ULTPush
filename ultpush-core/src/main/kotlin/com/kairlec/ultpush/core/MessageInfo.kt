package com.kairlec.ultpush.core

interface MessageInfo {
    /**
     * 发送人(唯一标识)
     */
    val fromUser: String

    /**
     * 接收人(唯一标识)
     */
    val toUser: String

    /**
     * 消息创建时间
     */
    val createTime: Int

}