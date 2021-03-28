@file:Suppress("SpellCheckingInspection", "PropertyName", "unused", "ArrayInDataClass")

package com.kairlec.ultpush.wework.message


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

interface ToAble {
    /**
     * 成员ID列表（消息接收者，多个接收者用‘|’分隔，最多支持1000个）。特殊情况：指定为@all，则向关注该企业应用的全部成员发送
     */
    @SerialName("touser")
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

@Serializable
data class Content(
    val content: String
)


@Serializable
open class Media : IRawMedia {
    var media_id: String?

    @Transient
    final override var isRawData: Boolean = false

    @Transient
    final override var rawMedia: RawMedia? = null

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


@Serializable
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
 * @param title 标题，不超过128个字节，超过会自动截断（支持id转译）
 * @param description 描述，不超过512个字节，超过会自动截断（支持id转译）
 * @param url 点击后跳转的链接。
 */
@Serializable
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
 * @param title 标题，不超过128个字节，超过会自动截断（支持id转译）
 * @param url 点击后跳转的链接。
 */
@Serializable
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
@Serializable
data class InnerNews(
    val articles: Array<NewsArticles>
)

/**
 * @param title 标题，不超过128个字节，超过会自动截断（支持id转译）
 * @param thumb_media_id 图文消息缩略图的media_id, 可以通过素材管理接口获得。此处thumb_media_id即上传接口返回的media_id
 * @param content 图文消息的内容，支持html标签，不超过666 K个字节（支持id转译）
 */
@Serializable
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
@Serializable
data class InnerMpNews(
    val articles: Array<MpNewsArticles>
)


/**
 * TaskCard按钮
 * @param key 按钮key值，用户点击后，会产生任务卡片回调事件，回调事件会带上该key值，只能由数字、字母和“_-@”组成，最长支持128字节
 * @param name 按钮名称
 * @since WeWork 2.8.2
 */
@Serializable
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
@Serializable
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
