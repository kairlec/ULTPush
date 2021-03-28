package com.kairlec.ultpush.wework.pusher.pojo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 上传临时素材后得到的mediaId
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90389
 */
@Serializable
data class MediaID(
        /**
         * 媒体文件类型，分别有图片（image）、语音（voice）、视频（video），普通文件(file)
         */
        @SerialName("type")
        val type: String,
        /**
         * 媒体文件上传后获取的唯一标识，3天内有效
         */
        @SerialName("media_id")
        val mediaID: String,
        /**
         * 媒体文件上传时间戳
         */
        @SerialName("created_at")
        val createdAt: Long
)