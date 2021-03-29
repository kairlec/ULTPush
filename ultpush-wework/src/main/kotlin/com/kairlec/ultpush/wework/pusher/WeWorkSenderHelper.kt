package com.kairlec.ultpush.wework.pusher

import com.kairlec.ultpush.wework.SenderKtor
import com.kairlec.ultpush.wework.WeWorkAccessTokenHelper
import com.kairlec.ultpush.wework.WeWorkHelper
import com.kairlec.ultpush.wework.message.*
import com.kairlec.ultpush.wework.pusher.pojo.MediaID
import com.kairlec.ultpush.wework.pusher.pojo.MediaTypeEnum
import com.kairlec.ultpush.wework.utils.UrlBuilder
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties


@Suppress("unused", "SpellCheckingInspection", "SameParameterValue")
open class WeWorkSenderHelper(
    private val validateCertificateChains: Boolean,
    private val accessTokenHelper: WeWorkAccessTokenHelper,
    private val agentid: Int
) : WeWorkHelper {

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
     * 消息发送URL
     */
    suspend fun getUrl() = UrlBuilder("https://qyapi.weixin.qq.com/cgi-bin/message/send")
        .addQueryParameter("access_token", accessTokenHelper.get())
        .build()

    /**
     * 重试机制,若发送报出Pusher异常,进行重试
     *
     * 若token过期,也会先进行更新token,再重试发送
     * @param count 重试次数
     * @param action 要执行的动作,动作出错则重试
     * @return action动作的结果
     */
    private suspend inline fun <reified T> retry(count: Int = 2, crossinline action: suspend () -> T): T {
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

    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> preUploadMedia(obj: T, mediaTypeEnum: MediaTypeEnum) {
        logger.info("pre:${obj::class.qualifiedName}")
        if (obj is IRawMedia) {
            logger.info("upload obj")
            obj.run {
                if (isRawData) {
                    val raw = rawMedia!!
                    val media = uploadMedia(raw.mediaData, raw.mediaName, mediaTypeEnum)
                    uploaded(media.mediaID)
                }
            }
            return
        }
        obj::class.memberProperties.forEach {
            val clazz = it.returnType.classifier as KClass<*>
            logger.info("in:${clazz.qualifiedName}")
            when {
                clazz.isSubclassOf(IRawMedia::class) -> {
                    (it as KProperty1<T, IRawMedia>).get(obj).run {
                        if (isRawData) {
                            val raw = rawMedia!!
                            val media = uploadMedia(raw.mediaData, raw.mediaName, mediaTypeEnum)
                            uploaded(media.mediaID)
                        }
                    }
                }
                clazz.isSubclassOf(Iterable::class) -> {
                    (it as KProperty1<T, Iterable<*>>).get(obj).run {
                        this.forEach { subObj ->
                            subObj?.run { preUploadMedia(this, mediaTypeEnum) }
                        }
                    }
                }
                clazz.java.isArray -> {
                    (it as KProperty1<T, Array<*>>).get(obj).run {
                        this.forEach { subObj ->
                            subObj?.run { preUploadMedia(this, mediaTypeEnum) }
                        }
                    }
                }
            }
        }
    }

    private suspend fun preSend(msg: WeWorkMessage, settings: SenderSettings): SenderSettings {
        MediaTypeEnum.parseMedia(msg.msgtype)?.let {
            preUploadMedia(msg, it)
        }
        if (msg.toUser.isNotBlank()) {
            settings.toUser = msg.toUser
        }
        return settings
    }

    @Serializable
    private data class SendResult(
        val errcode: Int,
        val errmsg: String
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)
        fun checkResult() {
            logger.info("code=${errcode} Result=${errmsg}")
            if (errcode != 0) {
                throw PusherExceptions.SendMessageException(errcode, null, errmsg)
            }
        }
    }

    /**
     * 发送文本消息
     * @param content 文本内容
     * @param settings 发送设置,默认为Helper的设置
     */
    suspend fun sendText(text: Text) {
        retry {
            SenderKtor.postJsonResultMap<SendResult, PusherExceptions.SendMessageException>(
                getUrl(), preSend(text, defaultSettings).cover(text), validateCertificateChains
            ).checkResult()
        }
    }

    /**
     * 发送图片消息
     * @param imageID 图像id,可事先根据上传临时素材[uploadMedia]得到
     * @param settings 发送设置,默认为Helper的设置
     * @see [MediaID]
     */
    suspend fun sendImage(image: Image) {
        retry {
            SenderKtor.postJsonResultMap<SendResult, PusherExceptions.SendMessageException>(
                getUrl(), preSend(image, defaultSettings).cover(image), validateCertificateChains
            ).checkResult()
        }
    }

    /**
     * 发送语音消息
     * @param voiceID 语音id,可事先根据上传临时素材[uploadMedia]得到
     * @param settings 发送设置,默认为Helper的设置
     * @see [MediaID]
     */
    suspend fun sendVoice(voice: Voice) {
        retry {
            SenderKtor.postJsonResultMap<SendResult, PusherExceptions.SendMessageException>(
                getUrl(), preSend(voice, defaultSettings).cover(voice), validateCertificateChains
            ).checkResult()
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
    suspend fun sendVideo(video: Video) {
        retry {
            SenderKtor.postJsonResultMap<SendResult, PusherExceptions.SendMessageException>(
                getUrl(), preSend(video, defaultSettings).cover(video), validateCertificateChains
            ).checkResult()
        }
    }

    /**
     * 发送文件消息
     * @param fileID 文件id,可事先根据上传临时素材[uploadMedia]得到
     * @param settings 发送设置,默认为Helper的设置
     * @see [MediaID]
     */
    suspend fun sendFile(file: File) {
        retry {
            SenderKtor.postJsonResultMap<SendResult, PusherExceptions.SendMessageException>(
                getUrl(), preSend(file, defaultSettings).cover(file), validateCertificateChains
            ).checkResult()
        }
    }

    /**
     * 发送文本卡片消息
     * @param settings 发送设置,默认为Helper的设置
     * @see [InnerTextCard]
     */
    suspend fun sendTextCard(textCard: TextCard) {
        retry {
            SenderKtor.postJsonResultMap<SendResult, PusherExceptions.SendMessageException>(
                getUrl(), preSend(textCard, defaultSettings).cover(textCard), validateCertificateChains
            ).checkResult()
        }
    }

    /**
     * 发送新闻消息
     * @param settings 发送设置,默认为Helper的设置
     * @see [InnerNews]
     */
    suspend fun sendNews(news: News) {
        retry {
            SenderKtor.postJsonResultMap<SendResult, PusherExceptions.SendMessageException>(
                getUrl(), preSend(news, defaultSettings).cover(news), validateCertificateChains
            ).checkResult()
        }
    }

    /**
     * 发送Mp新闻消息
     * @param settings 发送设置,默认为Helper的设置
     * @see [InnerMpNews]
     */
    suspend fun sendMpNews(mpNews: MpNews) {
        retry {
            SenderKtor.postJsonResultMap<SendResult, PusherExceptions.SendMessageException>(
                getUrl(), preSend(mpNews, defaultSettings).cover(mpNews), validateCertificateChains
            ).checkResult()
        }
    }

    /**
     * 发送MarkDown消息
     * @param content markdown内容
     * @param settings 发送设置,默认为Helper的设置
     * @see [Markdown]
     */
    suspend fun sendMarkdown(markdown: Markdown) {
        retry {
            SenderKtor.postJsonResultMap<SendResult, PusherExceptions.SendMessageException>(
                getUrl(), preSend(markdown, defaultSettings).cover(markdown), validateCertificateChains
            ).checkResult()
        }
    }

    /**
     * 发送任务卡片消息
     * @param settings 发送设置,默认为Helper的设置
     * @see [InnerTaskCard]
     */
    suspend fun sendTaskCard(taskCard: TaskCard) {
        retry {
            SenderKtor.postJsonResultMap<SendResult, PusherExceptions.SendMessageException>(
                getUrl(),
                preSend(taskCard, defaultSettings).cover(taskCard),
                validateCertificateChains
            ).checkResult()
        }
    }

    /**
     * 上传媒体文件
     * @param file 文件内容
     * @param filename 文件名称
     * @param type 文件类型
     * @return 媒体ID
     */
    private suspend fun uploadMedia(file: ByteArray, filename: String, type: MediaTypeEnum): MediaID {
        logger.info("ready to upload $filename by ${type.typeString}")
        return retry {
            if (file.size > type.maxSize) {
                throw PusherExceptions.UploadMediaException(-2, null, "File is to large")
            }
            val url = UrlBuilder("https://qyapi.weixin.qq.com/cgi-bin/media/upload")
                .addQueryParameter("access_token", accessTokenHelper.get())
                .addQueryParameter("type", type.typeString)
                .build()
            return@retry SenderKtor.uploadResultMap<MediaID, PusherExceptions.UploadMediaException>(
                url,
                file,
                filename,
                validateCertificateChains
            )
        }
    }

}