package com.kairlec.ultpush.wework.user


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 通过user/list接口获取到的成员信息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90337
 */
@Serializable
data class DetailUser(
    /**
     * 地址。第三方仅通讯录应用可获取
     */
    @SerialName("address")
    val address: String,
    /**
     * 别名；第三方仅通讯录应用可获取
     */
    @SerialName("alias")
    val alias: String,
    /**
     * 头像url。 第三方仅通讯录应用可获取
     */
    @SerialName("avatar")
    val avatar: String,
    /**
     * 成员所属部门id列表，仅返回该应用有查看权限的部门id
     */
    @SerialName("department")
    val department: List<Int>,
    /**
     * 邮箱，第三方仅通讯录应用可获取
     */
    @SerialName("email")
    val email: String,
    /**
     * 英文名
     */
    @SerialName("english_name")
    val englishName: String,
    /**
     * 扩展属性，第三方仅通讯录应用可获取
     */
    @SerialName("extattr")
    val extAttr: ExtAttr? = null,
    /**
     * 对外职务。 第三方仅通讯录应用可获取
     */
    @SerialName("external_position")
    val externalPosition: String,
    /**
     * 成员对外属性，字段详情见对外属性；第三方仅通讯录应用可获取
     */
    @SerialName("external_profile")
    val externalProfile: ExternalProfile? = null,
    /**
     * 性别。0表示未定义，1表示男性，2表示女性
     */
    @SerialName("gender")
    val gender: String,
    /**
     * 是否隐藏手机号
     */
    @SerialName("hide_mobile")
    val hideMobile: Int,
    /**
     * 表示在所在的部门内是否为上级；第三方仅通讯录应用可获取
     */
    @SerialName("is_leader_in_dept")
    val isLeaderInDept: List<Int>,
    /**
     * 主部门
     */
    @SerialName("main_department")
    val mainDepartment: Int,
    /**
     * 手机号码，第三方仅通讯录应用可获取
     */
    @SerialName("mobile")
    val mobile: String,
    /**
     * 成员名称，此字段从2019年12月30日起，对新创建第三方应用不再返回，2020年6月30日起，对所有历史第三方应用不再返回，后续第三方仅通讯录应用可获取，第三方页面需要通过通讯录展示组件来展示名字
     */
    @SerialName("name")
    val name: String,
    /**
     * 全局唯一。对于同一个服务商，不同应用获取到企业内同一个成员的open_userid是相同的，最多64个字节。仅第三方应用可获取
     */
    @SerialName("open_userid")
    var openUserID: String,
    /**
     * 部门内的排序值，32位整数，默认为0。数量必须和department一致，数值越大排序越前面。
     */
    @SerialName("order")
    val order: List<Int>,
    /**
     * 职务信息；第三方仅通讯录应用可获取
     */
    @SerialName("position")
    val position: String,
    /**
     * 员工个人二维码，扫描可添加为外部联系人；第三方仅通讯录应用可获取
     */
    @SerialName("qr_code")
    val qrCode: String,
    /**
     * 激活状态: 1=已激活，2=已禁用，4=未激活，5=退出企业。已激活代表已激活企业微信或已关注微工作台（原企业号）。未激活代表既未激活企业微信又未关注微工作台（原企业号）。
     */
    @SerialName("status")
    val status: Int,
    /**
     * 座机。第三方仅通讯录应用可获取
     */
    @SerialName("telephone")
    val telephone: String,
    /**
     * 头像缩略图url。第三方仅通讯录应用可获取
     */
    @SerialName("thumb_avatar")
    val thumbAvatar: String,
    /**
     * 成员UserID。对应管理端的帐号
     */
    @SerialName("userid")
    val userID: String
) {

    @Serializable
    data class ExtAttr(
        @SerialName("attrs")
        val attrs: List<Attr> = ArrayList()
    ) {
        @Serializable
        data class Attr(
            @SerialName("name")
            val name: String,
            @SerialName("text")
            val text: Text? = null,
            @SerialName("type")
            val type: Int,
            @SerialName("web")
            val web: Web? = null
        ) {
            @Serializable
            data class Text(
                @SerialName("value")
                val value: String
            )

            @Serializable
            data class Web(
                @SerialName("title")
                val title: String,
                @SerialName("url")
                val url: String
            )
        }
    }

    @Serializable
    data class ExternalProfile(
        @SerialName("external_attr")
        val externalAttr: List<ExternalAttr> = ArrayList(),
        @SerialName("external_corp_name")
        val externalCorpName: String
    ) {
        @Serializable
        data class ExternalAttr(
            @SerialName("miniprogram")
            val miniProgram: MiniProgram? = null,
            @SerialName("name")
            val name: String,
            @SerialName("text")
            val text: Text? = null,
            @SerialName("type")
            val type: Int,
            @SerialName("web")
            val web: Web? = null
        ) {
            @Serializable
            data class MiniProgram(
                @SerialName("appid")
                val appID: String,
                @SerialName("pagepath")
                val pagePath: String,
                @SerialName("title")
                val title: String
            )

            @Serializable
            data class Text(
                @SerialName("value")
                val value: String
            )

            @Serializable
            data class Web(
                @SerialName("title")
                val title: String,
                @SerialName("url")
                val url: String
            )
        }
    }
}