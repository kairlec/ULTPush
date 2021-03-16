@file:Suppress("SpellCheckingInspection", "PropertyName", "unused", "ArrayInDataClass")

package com.kairlec.ultpush.wework.message

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.kairlec.ultpush.core.FilterLevel
import com.kairlec.ultpush.core.pusher.PusherMsg
import com.kairlec.ultpush.core.receiver.ReceiverMsg
import kotlin.properties.Delegates

@JsonIgnoreProperties("allow_super_class_push", "keyword", "level", "from_user", "create_time")
sealed class WeWorkMessage : PusherMsg, ReceiverMsg {
    override var allowSuperClassPush: Boolean = true
    override val keyword: Set<String> = emptySet()
    override var level: FilterLevel = FilterLevel.INFO

    override val textContent: String
        get() = content.toString()

    override val createTime: Long
        get() = System.currentTimeMillis()

    override val allowSuperClassHandle: Boolean
        get() = false

    abstract val msgtype: String
    var withData: Any? = null
}

interface ToAble {
    /**
     * 成员ID列表（消息接收者，多个接收者用‘|’分隔，最多支持1000个）。特殊情况：指定为@all，则向关注该企业应用的全部成员发送
     */
    var toUser: String

    /**
     * 部门ID列表，多个接收者用‘|’分隔，最多支持100个。当toUser为@all时忽略本参数
     */
    var toParty: String

    /**
     * 标签ID列表，多个接收者用‘|’分隔，最多支持100个。当toUser为@all时忽略本参数
     */
    var toTag: String
}

interface ISaveAble {
    /**
     * 表示是否是保密消息，0表示否，1表示是，默认0
     */
    var safe: Int?
}

interface IEnableIdTransAble {
    /**
     * 表示是否开启id转译，0表示否，1表示是，默认0
     */
    var enableIdTrans: Int?
}

interface IDuplicateCheckAble {
    /**
     * 表示是否开启重复消息检查，0表示否，1表示是，默认0
     */
    var enableDuplicateCheck: Int?

    /**
     * 表示是否重复消息检查的时间间隔，默认1800s，最大不超过4小时
     */
    var duplicateCheckInterval: Int?
}

interface IRawMedia {
    var isRawData: Boolean
    val rawMedia: RawMedia?
}


class RawMedia(
    val mediaName: String,
    val mediaData: ByteArray,
)

data class Content(
    val content: String
)

/**
 * 文本消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E6%96%87%E6%9C%AC%E6%B6%88%E6%81%AF
 * @param text content字段可以支持换行、以及A标签，即可打开自定义的网页（可参考以上示例代码）(注意：换行符请用转义过的\n)
 */
data class Text(
    val text: Content,
) : ToAble, IDuplicateCheckAble, IEnableIdTransAble, ISaveAble, WeWorkMessage() {
    constructor(text: String) : this(Content(text))

    override lateinit var toParty: String
    override lateinit var toTag: String
    override lateinit var toUser: String
    override lateinit var fromUser: String
    var agentid by Delegates.notNull<Int>()
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
    override val keyword: MutableSet<String> = HashSet(listOf("text"))
    override val createTime: Long
        get() = System.currentTimeMillis()
}

open class Media : IRawMedia {
    var media_id: String?
    final override var isRawData: Boolean
    final override val rawMedia: RawMedia?

    fun uploaded(media_id: String) {
        this.isRawData = false
        this.media_id = media_id
    }

    constructor(media_id: String) {
        this.media_id = media_id
        this.isRawData = false
        this.rawMedia = null
    }

    constructor(rawMedia: RawMedia) {
        isRawData = true
        this.media_id = null
        this.rawMedia = rawMedia
    }
}

/**
 * 图片消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E5%9B%BE%E7%89%87%E6%B6%88%E6%81%AF
 * @param image 图像最小5B，最大2MB，支持JPG,PNG格式
 */
data class Image(
    val image: Media,
) : ToAble, ISaveAble, IDuplicateCheckAble, WeWorkMessage() {
    constructor(imageID: String) : this(Media(imageID))


    override lateinit var toUser: String
    override lateinit var toParty: String
    override lateinit var toTag: String
    override lateinit var fromUser: String
    var agentid by Delegates.notNull<Int>()
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
    override val keyword: MutableSet<String> = HashSet(listOf("image"))
    override val createTime: Long
        get() = System.currentTimeMillis()
}

/**
 * 语音消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E8%AF%AD%E9%9F%B3%E6%B6%88%E6%81%AF
 * @param voice 音频,最小5B，最大2MB，播放长度不超过60s，仅支持AMR格式
 */
data class Voice(
    val voice: Media,
) : ToAble, ISaveAble, IDuplicateCheckAble, WeWorkMessage() {
    constructor(voiceID: String) : this(Media(voiceID))

    override lateinit var toUser: String
    override lateinit var toParty: String
    override lateinit var toTag: String
    override lateinit var fromUser: String
    var agentid by Delegates.notNull<Int>()
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
    override val keyword: MutableSet<String> = HashSet(listOf("voice"))
    override val createTime: Long
        get() = System.currentTimeMillis()
}

class VideoMedia : Media {
    /**
     * 视频消息的标题，不超过128个字节，超过会自动截断
     */
    var title: String? = null

    /**
     * 视频消息的描述，不超过512个字节，超过会自动截断
     */
    var description: String? = null

    constructor(media_id: String) : super(media_id)
    constructor(rawMedia: RawMedia) : super(rawMedia)
}

/**
 * 视频消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E8%A7%86%E9%A2%91%E6%B6%88%E6%81%AF
 * @param video 视频，最小5B，最大10MB，支持MP4格式
 */
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
    override lateinit var fromUser: String
    var agentid by Delegates.notNull<Int>()
    override var safe: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    /**
     * 消息类型，此时固定为：video
     */
    override val msgtype: String = "video"
    override val keyword: MutableSet<String> = HashSet(listOf("video"))
    override val createTime: Long
        get() = System.currentTimeMillis()
}

/**
 * 文件消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E6%96%87%E4%BB%B6%E6%B6%88%E6%81%AF
 * @param file 文件，最小5B，最大20MB
 */
data class File(
    val file: Media,
) : ToAble, ISaveAble, IDuplicateCheckAble, WeWorkMessage() {
    constructor(fileID: String) : this(Media(fileID))

    override lateinit var toUser: String
    override lateinit var toParty: String
    override lateinit var toTag: String
    override lateinit var fromUser: String
    var agentid by Delegates.notNull<Int>()
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
    override val keyword: MutableSet<String> = HashSet(listOf("file"))
    override val createTime: Long
        get() = System.currentTimeMillis()
}

/**
 * @param title 标题，不超过128个字节，超过会自动截断（支持id转译）
 * @param description 描述，不超过512个字节，超过会自动截断（支持id转译）
 * @param url 点击后跳转的链接。
 */
data class InnerTextCard(
    val title: String,
    val description: String,
    val url: String,
) {
    /**
     * 按钮文字。 默认为“详情”， 不超过4个文字，超过自动截断。
     */
    var btntxt: String? = "详情"
}

/**
 * 文本卡片消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E6%96%87%E6%9C%AC%E5%8D%A1%E7%89%87%E6%B6%88%E6%81%AF
 * @param textcard 文本卡片消息体
 */
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
    override lateinit var fromUser: String
    var agentid by Delegates.notNull<Int>()
    override var enableIdTrans: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    /**
     * 消息类型，此时固定为：textcard
     */
    override val msgtype: String = "textcard"
    override val keyword: MutableSet<String> = HashSet(listOf("textcard"))
    override val createTime: Long
        get() = System.currentTimeMillis()
}

/**
 * @param title 标题，不超过128个字节，超过会自动截断（支持id转译）
 * @param url 点击后跳转的链接。
 */
data class NewsArticles(
    val title: String,
    val url: String,
) {
    /**
     * 描述，不超过512个字节，超过会自动截断（支持id转译）
     */
    var description: String? = null

    /**
     * 图文消息的图片链接，支持JPG、PNG格式，较好的效果为大图 1068*455，小图150*150。
     */
    var picurl: String? = null
}

/**
 * @param articles 图文消息，一个图文消息支持1到8条图文
 */
data class InnerNews(
    val articles: Array<NewsArticles>
)

/**
 * 图文消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E5%9B%BE%E6%96%87%E6%B6%88%E6%81%AF
 * @param news 图文消息体
 */
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
    override lateinit var fromUser: String
    var agentid by Delegates.notNull<Int>()
    override var enableIdTrans: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    /**
     * 消息类型，此时固定为：news
     */
    override val msgtype: String = "news"
    override val keyword: MutableSet<String> = HashSet(listOf("news"))
    override val createTime: Long
        get() = System.currentTimeMillis()
}

/**
 * @param title 标题，不超过128个字节，超过会自动截断（支持id转译）
 * @param thumb_media_id 图文消息缩略图的media_id, 可以通过素材管理接口获得。此处thumb_media_id即上传接口返回的media_id
 * @param content 图文消息的内容，支持html标签，不超过666 K个字节（支持id转译）
 */
data class MpNewsArticles(
    val title: String,
    val thumb_media_id: String,
    val content: String,
) {
    /**
     * 图文消息的作者，不超过64个字节
     */
    var author: String? = null

    /**
     * 图文消息点击“阅读原文”之后的页面链接
     */
    var content_source_url: String? = null

    /**
     * 图文消息的描述，不超过512个字节，超过会自动截断（支持id转译）
     */
    var digest: String? = null
}

/**
 * @param articles 图文消息，一个图文消息支持1到8条图文
 */
data class InnerMpNews(
    val articles: Array<MpNewsArticles>
)

/**
 * 图文消息（mpnews）
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E5%9B%BE%E6%96%87%E6%B6%88%E6%81%AF%EF%BC%88mpnews%EF%BC%89
 * @param mpnews 图文消息体
 */
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
    override lateinit var fromUser: String
    var agentid by Delegates.notNull<Int>()
    override var safe: Int? = null
    override var enableIdTrans: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    /**
     * 消息类型，此时固定为：mpnews
     */
    override val msgtype: String = "mpnews"
    override val keyword: MutableSet<String> = HashSet(listOf("mpnews"))
    override val createTime: Long
        get() = System.currentTimeMillis()
}

/**
 * Markdown消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#markdown%E6%B6%88%E6%81%AF
 *
 * 支持的语法: https://work.weixin.qq.com/api/doc/90000/90135/90236#%E6%94%AF%E6%8C%81%E7%9A%84markdown%E8%AF%AD%E6%B3%95
 * @param markdown 图文消息的内容，支持html标签，不超过666 K个字节（支持id转译）
 */
data class Markdown(
    val markdown: Content,
) : ToAble, IDuplicateCheckAble, WeWorkMessage() {
    constructor(content: String) : this(Content(content))

    override lateinit var toUser: String
    override lateinit var toParty: String
    override lateinit var toTag: String
    override lateinit var fromUser: String
    var agentid by Delegates.notNull<Int>()
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
    override val keyword: MutableSet<String> = HashSet(listOf("markdown"))
    override val createTime: Long
        get() = System.currentTimeMillis()
}

/**
 * TaskCard按钮
 * @param key 按钮key值，用户点击后，会产生任务卡片回调事件，回调事件会带上该key值，只能由数字、字母和“_-@”组成，最长支持128字节
 * @param name 按钮名称
 * @since WeWork 2.8.2
 */
data class Btn(
    val key: String,
    val name: String,
) {
    /**
     * 点击按钮后显示的名称，默认为“已处理”
     */
    var replace_name: String? = "已处理"

    /**
     * 按钮字体颜色，可选“red”或者“blue”,默认为“blue”
     */
    var color: String? = "blue"

    /**
     * 按钮字体是否加粗，默认false
     */
    var is_bold: Boolean? = false
}

/**
 * TaskCard内容体
 * @param title 标题，不超过128个字节，超过会自动截断（支持id转译）
 * @param description 描述，不超过512个字节，超过会自动截断（支持id转译）
 * @param task_id 任务id，同一个应用发送的任务卡片消息的任务id不能重复，只能由数字、字母和“_-@”组成，最长支持128字节
 * @param btn 按钮[Btn]列表,按钮个数为1~2个
 * @since WeWork 2.8.2
 */
data class InnerTaskCard(
    val title: String,
    val description: String,
    val task_id: String,
    val btn: Array<Btn>
) {
    /**
     * 点击后跳转的链接。最长2048字节，请确保包含了协议头(http/https)
     */
    var url: String? = null
}

/**
 * 任务卡片消息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90372#%E4%BB%BB%E5%8A%A1%E5%8D%A1%E7%89%87%E6%B6%88%E6%81%AF
 * @param taskcard TaskCard
 * @since WeWork 2.8.2
 */
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
    override lateinit var fromUser: String
    var agentid by Delegates.notNull<Int>()
    override var enableIdTrans: Int? = null
    override var enableDuplicateCheck: Int? = null
    override var duplicateCheckInterval: Int? = null

    /**
     * 消息类型，此时固定为：taskcard
     */
    override val msgtype: String = "taskcard"
    override val keyword: MutableSet<String> = HashSet(listOf("taskcard"))
    override val createTime: Long
        get() = System.currentTimeMillis()
}