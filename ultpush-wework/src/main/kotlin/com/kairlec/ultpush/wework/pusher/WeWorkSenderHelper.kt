package com.kairlec.ultpush.wework.pusher

import com.kairlec.ultpush.wework.Sender
import com.kairlec.ultpush.wework.WeWorkAccessTokenHelper
import com.kairlec.ultpush.wework.WeWorkHelper
import com.kairlec.ultpush.wework.message.*
import com.kairlec.ultpush.wework.pusher.pojo.MediaID
import com.kairlec.ultpush.wework.pusher.pojo.MediaTypeEnum
import com.kairlec.ultpush.wework.utils.UrlBuilder
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties


@Suppress("unused", "SpellCheckingInspection", "SameParameterValue")
open class WeWorkSenderHelper(
    private val validateCertificateChains: Boolean,
    private val accessTokenHelper: WeWorkAccessTokenHelper,
    private val agentid: Int
) :
    WeWorkHelper {
    /**
     * Sender的附带设置,这个设置有默认,同时也可以另外传入
     * @param toUser [ToAble.touser]
     * @param toTag [ToAble.toTag]
     * @param toParty [ToAble.toParty]
     * @param safe [ISaveAble.safe]
     * @param enableIdTrans [IEnableIdTransAble.enableIdTrans]
     * @param enableDuplicateCheck [IDuplicateCheckAble.enableDuplicateCheck]
     * @param duplicateCheckInterval [IDuplicateCheckAble.duplicateCheckInterval]
     */
    data class SenderSettings(
        val agentid: Int,
        var toUser: String = "@all",
        var toTag: String = "",
        var toParty: String = "",
        var safe: Int = 0,
        var enableIdTrans: Int = 0,
        var enableDuplicateCheck: Int = 0,
        var duplicateCheckInterval: Int = 1800
    ) {
        fun toUser(toUser: String) = apply { this.toUser = toUser }
        fun toTag(toTag: String) = apply { this.toTag = toTag }
        fun toParty(toParty: String) = apply { this.toParty = toParty }
        fun safe(safe: Int) = apply { this.safe = safe }
        fun enableIdTrans(enableIdTrans: Int) = apply { this.enableIdTrans = enableIdTrans }
        fun enableDuplicateCheck(enableDuplicateCheck: Int) = apply { this.enableDuplicateCheck = enableDuplicateCheck }
        fun duplicateCheckInterval(duplicateCheckInterval: Int) =
            apply { this.duplicateCheckInterval = duplicateCheckInterval }

        companion object {
            /**
             * 属性的名称和属性的map
             */
            private val propertyNames = SenderSettings::class.declaredMemberProperties
                .associateBy { it.name }
        }

        /**
         * 覆盖,将当前设置项,覆盖至指定的类上
         * @param obj 要覆盖的类实例
         * @return 覆盖后的类实例
         */
        fun cover(obj: Any): Any {
            val objClass = obj::class
            objClass.memberProperties.forEach {
                propertyNames[it.name]?.let { property ->
                    if (it is KMutableProperty<*>) {
                        it.setter.call(obj, property.get(this))
                    }
                }
            }
            return obj
        }
    }

    /**
     * 默认设置
     */
    val defaultSettings = SenderSettings(agentid)

    /**
     * 复制默认设置
     */
    /**
     * 消息发送URL接口
     */
    private val url: String
        get() = UrlBuilder("https://qyapi.weixin.qq.com/cgi-bin/message/send")
            .addQueryParameter("access_token", accessToken)
            .build()

    /**
     * 鉴权token
     */
    private val accessToken: String
        get() = accessTokenHelper.accessToken

    /**
     * 重试机制,若发送报出Pusher异常,进行重试
     *
     * 若token过期,也会先进行更新token,再重试发送
     * @param count 重试次数
     * @param action 要执行的动作,动作出错则重试
     * @return action动作的结果
     */
    private inline fun <reified T> retry(count: Int = 2, crossinline action: () -> T): T {
        if (count > 0) {
            for (i in 0 until count) {
                try {
                    return action()
                } catch (e: PusherExceptions.SendMessageException) {
                    if (e.code == 42001) {
                        accessTokenHelper.update()
                    } else {
                        throw e
                    }
                }
            }
            throw PusherExceptions.RetryException(-1)
        } else {
            throw IllegalArgumentException("The number of retries cannot be less than 1")
        }
    }

    private fun preSend(msg: WeWorkMessage, settings: SenderSettings): SenderSettings {
        MediaTypeEnum.parseMedia(msg.msgtype)?.let {
            msg::class.memberProperties.find { it.returnType == Media::class.createType() }?.let {
                (it as KProperty1<WeWorkMessage, Media>).get(msg)
            }?.run {
                if (isRawData) {
                    val raw = rawMedia!!
                    val media = uploadMedia(raw.mediaData, raw.mediaName, it)
                    uploaded(media.mediaID)
                }
            }
        }
        if (msg.toUser.isNotBlank()) {
            settings.toUser = msg.toUser
        }
        return settings
    }

    /**
     * 发送文本消息
     * @param content 文本内容
     * @param settings 发送设置,默认为Helper的设置
     */
    fun sendText(text: Text) {
        retry {
            Sender.postJsonResultMap<Unit, PusherExceptions.SendMessageException>(
                url, preSend(text, defaultSettings).cover(text), validateCertificateChains
            )
        }
    }

    /**
     * 发送图片消息
     * @param imageID 图像id,可事先根据上传临时素材[uploadMedia]得到
     * @param settings 发送设置,默认为Helper的设置
     * @see [MediaID]
     */
    fun sendImage(image: Image) {
        retry {
            Sender.postJsonResultMap<Unit, PusherExceptions.SendMessageException>(
                url, preSend(image, defaultSettings).cover(image), validateCertificateChains
            )
        }
    }

    /**
     * 发送语音消息
     * @param voiceID 语音id,可事先根据上传临时素材[uploadMedia]得到
     * @param settings 发送设置,默认为Helper的设置
     * @see [MediaID]
     */
    fun sendVoice(voice: Voice) {
        retry {
            Sender.postJsonResultMap<Unit, PusherExceptions.SendMessageException>(
                url, preSend(voice, defaultSettings).cover(voice), validateCertificateChains
            )
        }
    }

    /**
     * 发送视频消息
     * @param videoID 视频id,可事先根据上传临时素材[uploadMedia]得到
     * @param title 视频标题
     * @param description 视频描述
     * @param settings 发送设置,默认为Helper的设置
     * @see [VideoMedia]
     */
    fun sendVideo(video: Video) {
        retry {
            Sender.postJsonResultMap<Unit, PusherExceptions.SendMessageException>(
                url, preSend(video, defaultSettings).cover(video), validateCertificateChains
            )
        }
    }

    /**
     * 发送文件消息
     * @param fileID 文件id,可事先根据上传临时素材[uploadMedia]得到
     * @param settings 发送设置,默认为Helper的设置
     * @see [MediaID]
     */
    fun sendFile(file: File) {
        retry {
            Sender.postJsonResultMap<Unit, PusherExceptions.SendMessageException>(
                url, preSend(file, defaultSettings).cover(file), validateCertificateChains
            )
        }
    }

    /**
     * 发送文本卡片消息
     * @param settings 发送设置,默认为Helper的设置
     * @see [InnerTextCard]
     */
    fun sendTextCard(textCard: TextCard) {
        retry {
            Sender.postJsonResultMap<Unit, PusherExceptions.SendMessageException>(
                url, preSend(textCard, defaultSettings).cover(textCard), validateCertificateChains
            )
        }
    }

    /**
     * 发送新闻消息
     * @param settings 发送设置,默认为Helper的设置
     * @see [InnerNews]
     */
    fun sendNews(news: News) {
        retry {
            Sender.postJsonResultMap<Unit, PusherExceptions.SendMessageException>(
                url, preSend(news, defaultSettings).cover(news), validateCertificateChains
            )
        }
    }

    /**
     * 发送Mp新闻消息
     * @param settings 发送设置,默认为Helper的设置
     * @see [InnerMpNews]
     */
    fun sendMpNews(mpNews: MpNews) {
        retry {
            Sender.postJsonResultMap<Unit, PusherExceptions.SendMessageException>(
                url, preSend(mpNews, defaultSettings).cover(mpNews), validateCertificateChains
            )
        }
    }

    /**
     * 发送MarkDown消息
     * @param content markdown内容
     * @param settings 发送设置,默认为Helper的设置
     * @see [Markdown]
     */
    fun sendMarkdown(markdown: Markdown) {
        retry {
            Sender.postJsonResultMap<Unit, PusherExceptions.SendMessageException>(
                url, preSend(markdown, defaultSettings).cover(markdown), validateCertificateChains
            )
        }
    }

    /**
     * 发送任务卡片消息
     * @param settings 发送设置,默认为Helper的设置
     * @see [InnerTaskCard]
     */
    fun sendTaskCard(taskCard: TaskCard) {
        retry {
            Sender.postJsonResultMap<Unit, PusherExceptions.SendMessageException>(
                url,
                preSend(taskCard, defaultSettings).cover(taskCard),
                validateCertificateChains
            )
        }
    }

    /**
     * 上传媒体文件
     * @param file 文件内容
     * @param filename 文件名称
     * @param type 文件类型
     * @return 媒体ID
     */
    private fun uploadMedia(file: ByteArray, filename: String, type: MediaTypeEnum): MediaID {
        return retry {
            if (file.size > type.maxSize) {
                throw PusherExceptions.UploadMediaException(-2, null, "File is to large")
            }
            val url = UrlBuilder("https://qyapi.weixin.qq.com/cgi-bin/media/upload")
                .addQueryParameter("access_token", accessToken)
                .addQueryParameter("type", type.typeString)
                .build()
            return@retry Sender.uploadResultMap<MediaID, PusherExceptions.UploadMediaException>(
                url,
                file,
                filename,
                validateCertificateChains
            )
        }
    }

}