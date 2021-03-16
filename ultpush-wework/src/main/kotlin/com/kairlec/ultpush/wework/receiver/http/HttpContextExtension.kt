package com.kairlec.ultpush.wework.receiver.http

import com.kairlec.ultpush.http.HttpContext
import com.kairlec.ultpush.http.PartFile

fun HttpContext.getRequestParamMust(key: String, default: String? = null): String {
    return getRequestParam(key) ?: default ?: throw MissingRequestParamException(key)
}

fun HttpContext.getMultiPartFileMust(filename: String): PartFile {
    return getMultiPartFile(filename) ?: throw MissingRequestParamException(filename)
}