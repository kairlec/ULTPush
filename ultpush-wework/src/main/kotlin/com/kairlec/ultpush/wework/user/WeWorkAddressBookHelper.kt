package com.kairlec.ultpush.wework.user

import com.kairlec.ultpush.wework.WeWorkAccessTokenHelper
import com.kairlec.ultpush.wework.WeWorkHelper
import com.kairlec.ultpush.wework.pusher.PusherExceptions
import com.kairlec.ultpush.wework.SenderKtor
import com.kairlec.ultpush.wework.utils.UrlBuilder
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

@Suppress("unused", "SpellCheckingInspection")
open class WeWorkAddressBookHelper(
    private val validateCertificateChains: Boolean,
    private val accessTokenHelper: WeWorkAccessTokenHelper
) : WeWorkHelper {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Serializable
    private data class DepartmentList(
        val errcode: Int,
        val errmsg: String,
        val department: List<Department>
    )

    @Serializable
    private data class DetailUserList(
        val errcode: Int,
        val errmsg: String,
        val userlist: List<DetailUser>
    )

    @Serializable
    private data class SimpleUserList(
        val errcode: Int,
        val errmsg: String,
        val userlist: List<SimpleUser>
    )

    @Serializable
    private data class OpenID(
        val errcode: Int,
        val errmsg: String,
        val openid: String
    )

    /**
     * 获取用户
     * @param userID 用户ID
     * @see InUser
     */
    suspend fun getUser(userID: String): InUser {
        val url = UrlBuilder("https://qyapi.weixin.qq.com/cgi-bin/user/get")
            .addQueryParameter("access_token", accessTokenHelper.get())
            .addQueryParameter("userid", userID)
            .build()
        return SenderKtor.getResultMap<InUser, PusherExceptions.AddressBookUserException>(
            url,
            validateCertificateChains
        )
    }

    /**
     * 获取部门列表
     * @param id 部门id
     * @see Department
     */
    suspend fun getDepartmentList(id: Int? = null): List<Department> {
        val urlBuilder = UrlBuilder("https://qyapi.weixin.qq.com/cgi-bin/department/list")
            .addQueryParameter("access_token", accessTokenHelper.get())
        id?.let { urlBuilder.addQueryParameter("id", id.toString()) }
        val url = urlBuilder.build()
        return SenderKtor.getResultMap<DepartmentList, PusherExceptions.AddressBookDepartmentListException>(
            url,
            validateCertificateChains
        ).department
    }

    /**
     * 获取基本用户信息列表
     * @param departmentID 部门id
     * @see SimpleUser
     */
    suspend fun getUserSimpleList(departmentID: Int, fetchChild: Boolean = true): List<SimpleUser> {
        val url = UrlBuilder("https://qyapi.weixin.qq.com/cgi-bin/user/simplelist")
            .addQueryParameter("access_token", accessTokenHelper.get())
            .addQueryParameter("department_id", departmentID.toString())
            .addQueryParameter("fetch_child", if (fetchChild) "1" else "0")
            .build()
        return SenderKtor.getResultMap<SimpleUserList, PusherExceptions.AddressBookUserListException>(
            url,
            validateCertificateChains
        ).userlist.onEach {
            logger.error(it.toString())
            it.openUserID = getOpenUserId(it.userID)
        }
    }

    /**
     * 获取详细用户信息列表
     * @param departmentID 部门id
     * @param fetchChild 是否递归获取子用户
     * @see DetailUser
     */
    suspend fun getUserList(departmentID: Int, fetchChild: Boolean = true): List<DetailUser> {
        val url = UrlBuilder("https://qyapi.weixin.qq.com/cgi-bin/user/list")
            .addQueryParameter("access_token", accessTokenHelper.get())
            .addQueryParameter("department_id", departmentID.toString())
            .addQueryParameter("fetch_child", if (fetchChild) "1" else "0")
            .build()
        return SenderKtor.getResultMap<DetailUserList, PusherExceptions.AddressBookUserListException>(
            url,
            validateCertificateChains
        ).userlist.onEach {
            it.openUserID = getOpenUserId(it.userID)
        }
    }

    @Serializable
    private data class UserID(
        val userid: String
    )

    /**
     * 根据用户userid,获取其openUserId
     * @param userID UserId
     * @return OpenUserId
     */
    private suspend fun getOpenUserId(userID: String): String {
        val url = UrlBuilder("https://qyapi.weixin.qq.com/cgi-bin/user/convert_to_openid")
            .addQueryParameter("access_token", accessTokenHelper.get())
            .build()
        return SenderKtor.postJsonResultMap<OpenID, PusherExceptions.AddressBookUserListException>(
            url,
            UserID(userID),
            validateCertificateChains
        ).openid
    }

}