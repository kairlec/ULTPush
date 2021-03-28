package com.kairlec.ultpush.wework.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * 通过department/list获取到的部门信息
 *
 * API: https://work.weixin.qq.com/api/doc/90001/90143/90344
 */
@Serializable
data class Department(
    /**
     * 创建的部门id
     */
    @SerialName("id")
    val id: Int,
    /**
     * 	部门名称，此字段从2019年12月30日起，对新创建第三方应用不再返回，2020年6月30日起，对所有历史第三方应用不再返回，后续第三方仅通讯录应用可获取，第三方页面需要通过通讯录展示组件来展示部门名称
     */
    @SerialName("name")
    val name: String? = null,
    /**
     * 	英文名称
     */
    @SerialName("name_en")
    val nameEn: String? = null,
    /**
     * 在父部门中的次序值。order值大的排序靠前。值范围是[0, 2^32)
     */
    @SerialName("order")
    val order: Int,
    /**
     * 父部门id。根部门为1
     */
    @SerialName("parentid")
    val parentID: Int
)