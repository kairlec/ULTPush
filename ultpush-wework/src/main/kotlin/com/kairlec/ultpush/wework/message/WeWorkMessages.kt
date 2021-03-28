@file:Suppress("SpellCheckingInspection", "PropertyName", "unused", "ArrayInDataClass")

package com.kairlec.ultpush.wework.message

import com.kairlec.ultpush.core.FilterLevel
import com.kairlec.ultpush.core.pusher.PusherMsg
import com.kairlec.ultpush.core.receiver.ReceiverMsg
import kotlinx.serialization.SerialName
import kotlin.properties.Delegates
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class WeWorkMessage : PusherMsg, ReceiverMsg {
    @Transient
    override var allowSuperClassPush: Boolean = true

    @Transient
    override val keyword: Set<String> = emptySet()

    @Transient
    override var level: FilterLevel = FilterLevel.INFO

    override val textContent: String
        get() = content.toString()

    override val createTime: Long
        get() = System.currentTimeMillis()

    override val allowSuperClassHandle: Boolean
        get() = false

    abstract val msgtype: String

    @Transient
    var withData: Any? = null
}

/**
 * 文本消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E6%96%87%E6%9C%AC%E6%B6%88%E6%81%AF
 * @param text content字段可以支持换行、以及A标签，即可打开自定义的网页（可参考以上示例代码）(注意：换行符请用转义过的\n)
 */
@Serializable
@SerialName("Text")
data class Text(
    val text: Content,
) : ToAble, IDuplicateCheckAble, IEnableIdTransAble, ISaveAble, WeWorkMessage() {
    constructor(text: String) : this(Content(text))

    override lateinit var toParty: String
    override lateinit var toTag: String
    override lateinit var toUser: String

    @Transient
    override lateinit var fromUser: String
    var agentid: Int = 0
    override var safe: Int? = null
    override var enableIdTrans: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    override val content: Any
        get() = text

    override val textContent: String
        get() = text.content

    /**
     * 消息类型，此时固定为：text
     */
    override val msgtype: String = "text"

    @Transient
    override val keyword: MutableSet<String> = HashSet(listOf("text"))
}

/**
 * 图片消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E5%9B%BE%E7%89%87%E6%B6%88%E6%81%AF
 * @param image 图像最小5B，最大2MB，支持JPG,PNG格式
 */
@Serializable
data class Image(
    val image: Media,
) : ToAble, ISaveAble, IDuplicateCheckAble, WeWorkMessage() {
    constructor(imageID: String) : this(Media(imageID))


    override lateinit var toUser: String
    override lateinit var toParty: String
    override lateinit var toTag: String

    @Transient
    override lateinit var fromUser: String
    var agentid: Int = 0
    override var safe: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    override val content: Any
        get() = image

    override val textContent
        get() = "[Image]${image.media_id}"

    /**
     * 消息类型，此时固定为：image
     */
    override val msgtype: String = "image"

    @Transient
    override val keyword: MutableSet<String> = HashSet(listOf("image"))
}

/**
 * 语音消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E8%AF%AD%E9%9F%B3%E6%B6%88%E6%81%AF
 * @param voice 音频,最小5B，最大2MB，播放长度不超过60s，仅支持AMR格式
 */
@Serializable
data class Voice(
    val voice: Media,
) : ToAble, ISaveAble, IDuplicateCheckAble, WeWorkMessage() {
    constructor(voiceID: String) : this(Media(voiceID))

    override lateinit var toUser: String
    override lateinit var toParty: String
    override lateinit var toTag: String

    @Transient
    override lateinit var fromUser: String
    var agentid: Int = 0
    override var safe: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    override val content: Any
        get() = voice
    override val textContent
        get() = "[Voice]${voice.media_id}"

    /**
     * 消息类型，此时固定为：voice
     */
    override val msgtype: String = "voice"

    @Transient
    override val keyword: MutableSet<String> = HashSet(listOf("voice"))
}

/**
 * 视频消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E8%A7%86%E9%A2%91%E6%B6%88%E6%81%AF
 * @param video 视频，最小5B，最大10MB，支持MP4格式
 */
@Serializable
data class Video(
    val video: VideoMedia,
) : ToAble, ISaveAble, IDuplicateCheckAble, WeWorkMessage() {
    constructor(
        videoID: String,
        title: String? = null,
        description: String? = null
    ) : this(VideoMedia(videoID).apply {
        this.title = title
        this.description = description
    })

    override val content: Any
        get() = video
    override val textContent: String
        get() = "[Video]${video.media_id}"

    override lateinit var toUser: String
    override lateinit var toParty: String
    override lateinit var toTag: String

    @Transient
    override lateinit var fromUser: String
    var agentid: Int = 0
    override var safe: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    /**
     * 消息类型，此时固定为：video
     */
    override val msgtype: String = "video"

    @Transient
    override val keyword: MutableSet<String> = HashSet(listOf("video"))
}

/**
 * 文件消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E6%96%87%E4%BB%B6%E6%B6%88%E6%81%AF
 * @param file 文件，最小5B，最大20MB
 */
@Serializable
data class File(
    val file: Media,
) : ToAble, ISaveAble, IDuplicateCheckAble, WeWorkMessage() {
    constructor(fileID: String) : this(Media(fileID))

    override lateinit var toUser: String
    override lateinit var toParty: String
    override lateinit var toTag: String

    @Transient
    override lateinit var fromUser: String
    var agentid: Int = 0
    override var safe: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    override val content: Any
        get() = file

    override val textContent
        get() = "[File]${file.media_id}"

    /**
     * 消息类型，此时固定为：file
     */
    override val msgtype: String = "file"

    @Transient
    override val keyword: MutableSet<String> = HashSet(listOf("file"))
}

/**
 * 文本卡片消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E6%96%87%E6%9C%AC%E5%8D%A1%E7%89%87%E6%B6%88%E6%81%AF
 * @param textcard 文本卡片消息体
 */
@Serializable
data class TextCard(
    val textcard: InnerTextCard,
) : ToAble, IEnableIdTransAble, IDuplicateCheckAble, WeWorkMessage() {
    constructor(title: String, description: String, url: String, btntxt: String? = "详情") : this(
        InnerTextCard(
            title,
            description,
            url
        ).apply { this.btntxt = btntxt })

    override val content: Any
        get() = textcard

    override val textContent: String
        get() = textcard.title

    override lateinit var toUser: String
    override lateinit var toParty: String
    override lateinit var toTag: String

    @Transient
    override lateinit var fromUser: String
    var agentid: Int = 0
    override var enableIdTrans: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    /**
     * 消息类型，此时固定为：textcard
     */
    override val msgtype: String = "textcard"

    @Transient
    override val keyword: MutableSet<String> = HashSet(listOf("textcard"))
}

/**
 * 图文消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E5%9B%BE%E6%96%87%E6%B6%88%E6%81%AF
 * @param news 图文消息体
 */
@Serializable
data class News(
    val news: InnerNews,
) : ToAble, IEnableIdTransAble, IDuplicateCheckAble, WeWorkMessage() {
    constructor(
        title: String,
        url: String,
        description: String? = null,
        picurl: String? = null
    ) : this(InnerNews(arrayOf(NewsArticles(title, url).apply {
        this.description = description
        this.picurl = picurl
    })))

    override val content: Any
        get() = news

    override val textContent: String
        get() = news.articles.singleOrNull()?.title ?: "[News]{Empty}"

    override lateinit var toUser: String
    override lateinit var toParty: String
    override lateinit var toTag: String

    @Transient
    override lateinit var fromUser: String
    var agentid: Int = 0
    override var enableIdTrans: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    /**
     * 消息类型，此时固定为：news
     */
    override val msgtype: String = "news"

    @Transient
    override val keyword: MutableSet<String> = HashSet(listOf("news"))
}


/**
 * 图文消息（mpnews）
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E5%9B%BE%E6%96%87%E6%B6%88%E6%81%AF%EF%BC%88mpnews%EF%BC%89
 * @param mpnews 图文消息体
 */
@Serializable
data class MpNews(
    val mpnews: InnerMpNews,
) : ToAble, ISaveAble, IEnableIdTransAble, IDuplicateCheckAble, WeWorkMessage() {
    constructor(
        title: String,
        thumbMediaID: String,
        content: String,
        author: String? = null,
        contentSourceUrl: String? = null,
        digest: String? = null,
    ) : this(InnerMpNews(arrayOf(MpNewsArticles(title, thumbMediaID, content).apply {
        this.author = author
        this.content_source_url = contentSourceUrl
        this.digest = digest
    })))

    override val content: Any
        get() = mpnews

    override val textContent: String
        get() = mpnews.articles.singleOrNull()?.title ?: "[News]{Empty}"

    override lateinit var toUser: String
    override lateinit var toParty: String
    override lateinit var toTag: String

    @Transient
    override lateinit var fromUser: String
    var agentid: Int = 0
    override var safe: Int? = null
    override var enableIdTrans: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    /**
     * 消息类型，此时固定为：mpnews
     */
    override val msgtype: String = "mpnews"

    @Transient
    override val keyword: MutableSet<String> = HashSet(listOf("mpnews"))
}

/**
 * Markdown消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#markdown%E6%B6%88%E6%81%AF
 *
 * 支持的语法: https://work.weixin.qq.com/api/doc/90000/90135/90236#%E6%94%AF%E6%8C%81%E7%9A%84markdown%E8%AF%AD%E6%B3%95
 * @param markdown 图文消息的内容，支持html标签，不超过666 K个字节（支持id转译）
 */
@Serializable
data class Markdown(
    val markdown: Content,
) : ToAble, IDuplicateCheckAble, WeWorkMessage() {
    constructor(content: String) : this(Content(content))

    override lateinit var toUser: String
    override lateinit var toParty: String
    override lateinit var toTag: String

    @Transient
    override lateinit var fromUser: String
    var agentid: Int = 0
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    override val content: Any
        get() = markdown

    override val textContent: String
        get() = markdown.content

    /**
     * 消息类型，此时固定为：markdown
     */
    override val msgtype: String = "markdown"

    @Transient
    override val keyword: MutableSet<String> = HashSet(listOf("markdown"))
}

/**
 * 任务卡片消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E4%BB%BB%E5%8A%A1%E5%8D%A1%E7%89%87%E6%B6%88%E6%81%AF
 * @param taskcard TaskCard
 * @since WeWork 2.8.2
 */
@Serializable
data class TaskCard(
    val taskcard: InnerTaskCard
) : ToAble, IDuplicateCheckAble, IEnableIdTransAble, WeWorkMessage() {
    constructor(
        title: String,
        description: String,
        taskId: String,
        btns: Array<Btn>,
        url: String? = null
    ) : this(InnerTaskCard(title, description, taskId, btns).apply { this.url = url })

    override val content: Any
        get() = taskcard

    override val textContent: String
        get() = taskcard.title

    override lateinit var toUser: String
    override lateinit var toParty: String
    override lateinit var toTag: String

    @Transient
    override lateinit var fromUser: String
    var agentid: Int = 0
    override var enableIdTrans: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    /**
     * 消息类型，此时固定为：taskcard
     */
    override val msgtype: String = "taskcard"

    @Transient
    override val keyword: MutableSet<String> = HashSet(listOf("taskcard"))
}