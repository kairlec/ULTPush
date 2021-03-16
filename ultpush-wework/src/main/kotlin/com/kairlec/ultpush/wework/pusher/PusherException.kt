package com.kairlec.ultpush.wework.pusher

open class PusherException(open val code: Int, cause: Throwable?, message: String) : Exception(message, cause)

sealed class PusherExceptions {
    /**
     * Token鉴权异常
     * @param code 错误代码
     * @param cause 错误原因
     * @param message 错误消息
     */
    class AccessTokenException(
        override val code: Int,
        cause: Throwable? = null,
        message: String = "Failed to post access token"
    ) : PusherException(code, cause, message)

    /**
     * 发送消息异常
     * @param code 错误代码
     * @param cause 错误原因
     * @param message 错误消息
     */
    class SendMessageException(
        override val code: Int,
        cause: Throwable? = null,
        message: String = "Failed to post message to send"
    ) : PusherException(code, cause, message)

    /**
     * 通讯录列出异常
     * @param code 错误代码
     * @param cause 错误原因
     * @param message 错误消息
     */
    class AddressBookDepartmentListException(
        override val code: Int,
        cause: Throwable? = null,
        message: String = "Failed to post department list"
    ) : PusherException(code, cause, message)

    /**
     * 通讯录用户列表获取异常
     * @param code 错误代码
     * @param cause 错误原因
     * @param message 错误消息
     */
    class AddressBookUserListException(
        override val code: Int,
        cause: Throwable? = null,
        message: String = "Failed to post user list"
    ) : PusherException(code, cause, message)

    /**
     * 通讯录用户获取异常
     * @param code 错误代码
     * @param cause 错误原因
     * @param message 错误消息
     */
    class AddressBookUserException(
        override val code: Int,
        cause: Throwable? = null,
        message: String = "Failed to post user"
    ) : PusherException(code, cause, message)

    /**
     * 通讯录标签异常
     * @param code 错误代码
     * @param cause 错误原因
     * @param message 错误消息
     */
    class AddressBookTagException(
        override val code: Int,
        cause: Throwable? = null,
        message: String = "Failed to post tag"
    ) : PusherException(code, cause, message)

    /**
     * 媒体上传异常
     * @param code 错误代码
     * @param cause 错误原因
     * @param message 错误消息
     */
    class UploadMediaException(
        override val code: Int,
        cause: Throwable? = null,
        message: String = "Failed to upload media file"
    ) : PusherException(code, cause, message)

    /**
     * 重试异常
     * @param code 错误代码
     * @param cause 错误原因
     * @param message 错误消息
     */
    class RetryException(
        override val code: Int,
        cause: Throwable? = null,
        message: String = "Retries exceeded"
    ) : PusherException(code, cause, message)
}