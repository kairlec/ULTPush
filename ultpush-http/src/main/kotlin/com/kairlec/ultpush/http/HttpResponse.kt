@file:Suppress("unused")

package com.kairlec.ultpush.http

import java.io.PrintWriter
import java.util.*

interface HttpResponse {
    var characterEncoding: String
    var contentType: String
    val outputStream: HttpOutputStream
    val writer: PrintWriter
    var contentLength: Long
    var bufferSize: Int
    fun flushBuffer()
    fun resetBuffer()
    val committed: Boolean
    fun reset()
    var locale: Locale

    fun cookie(cookie: HttpCookie)
    fun cookie(name: String): HttpCookie
    fun cookies(): Iterable<HttpCookie>
    fun containCookie(name: String): Boolean

    fun header(header: String, value: String)
    fun header(name: String): String
    fun headers(): Iterable<Pair<String, String>>
    fun headerNames(): Iterable<String>
    fun containHeader(header: String): Boolean

    var statusCode: HttpStatusCode

    fun respond(statusCode: HttpStatusCode, msg: String)
}