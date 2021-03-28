package com.kairlec.ultpush.wework

import com.kairlec.ultpush.wework.pusher.PusherExceptions
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime
import kotlin.properties.Delegates


@Serializable
data class AccessToken(
    val errcode: Int,
    val errmsg: String,
    val access_token: String,
    val expires_in: Long
)

/**
 * Token鉴权辅助器
 */
@Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
abstract class AccessTokenHelper(protected open val validateCertificateChains: Boolean) {
    /**
     * token字符串
     */
    protected open lateinit var token: String

    /**
     * 下一次过期时间
     */
    protected open lateinit var expiredTime: LocalDateTime

    /**
     * 过期时间
     */
    protected open var expiresIn by Delegates.notNull<Long>()

    /**
     * 更新锁
     */
    protected open val updateLock = Mutex()

    /**
     * 更新url
     */
    protected abstract val url: String

    /**
     * 更新token
     */
    suspend fun update() {
        updateLock.withLock {
            val accessToken:AccessToken = try {
                SenderKtor.get(url, validateCertificateChains)
            } catch (e: Exception) {
                throw PusherExceptions.AccessTokenException(-1, e)
            }
            if (accessToken.errcode != 0) {
                throw PusherExceptions.AccessTokenException(
                    accessToken.errcode,
                    null,
                    accessToken.errmsg
                )
            }
            token = accessToken.access_token
            expiresIn = accessToken.expires_in
            expiredTime = LocalDateTime.now().plusSeconds(expiresIn)
        }
    }

    /**
     * 获取token,若即将过期,则更新token后返回
     */
    suspend fun get(): String {
        updateLock.withLock { }
        if (!this::expiredTime.isInitialized) {
            update()
        } else {
            val duration = Duration.between(LocalDateTime.now(), expiredTime)
            if (duration.toMinutes() < 5) {
                update()
            }
        }
        return token
    }

//    val accessToken: String
//        get() {
//            return runBlocking {
//                updateLock.withLock { }
//                val duration = Duration.between(LocalDateTime.now(), expiredTime)
//                if (duration.toMinutes() < 5) {
//                    update()
//                }
//                token
//            }
//        }
}