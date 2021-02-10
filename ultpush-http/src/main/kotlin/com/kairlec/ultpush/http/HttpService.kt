package com.kairlec.ultpush.http

import com.kairlec.ultpush.bind.ULTInterface
import java.net.http.HttpRequest

@ULTInterface
interface HttpService {
    fun <T : Throwable> exception(exceptionClass: Class<T>, event: HttpContext.(T) -> Unit): HttpService

    fun error(statusCode: Int, event: HttpContext.(Int) -> Unit): HttpService

    fun get(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun post(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun put(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun patch(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun delete(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun head(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun options(path: String, event: HttpContext.(String) -> Unit): HttpService

}