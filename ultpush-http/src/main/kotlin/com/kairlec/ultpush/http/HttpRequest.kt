package com.kairlec.ultpush.http

import java.io.BufferedReader
import java.util.*

interface HttpRequest {
    fun <T : Any> attribute(name: String): T?
    fun attributeNames(): Iterable<String>
    fun <T : Any> attribute(name: String, obj: T?)

    var characterEncoding: String
    val contentLength: Long
    val contentType: String
    val inputStream: HttpInputStream

    fun parameter(name: String): String?
    fun parameterNames(): Iterable<String>

    val protocol: String
    val scheme: String
    val serverName: String
    val serverPort: Int
    val reader: BufferedReader
    val remoteAddress: String
    val remoteHost: String
    val locale: Locale
    val locales: Iterable<Locale>
    val secure: Boolean
    val remotePort: Int
    val localName: String
    val localAddress: String
    val localPort: Int

    fun cookie(name: String): HttpCookie
    fun cookie(cookie: HttpCookie)
    fun cookies(): Iterable<HttpCookie>

    fun header(name: String): String
    fun header(name: String, value: String)
    fun headers(): Iterable<Pair<String, String>>

    val method:HttpMethod
    val queryString:String

}