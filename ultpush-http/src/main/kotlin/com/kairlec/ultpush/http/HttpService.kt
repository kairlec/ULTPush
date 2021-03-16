package com.kairlec.ultpush.http

import com.kairlec.ultpush.bind.ULTInterface
import java.net.http.HttpRequest

@ULTInterface
interface HttpService {
    fun <T : Throwable> exception(exceptionClass: Class<T>, event: HttpContext.(T) -> Unit): HttpService

    fun error(statusCode: Int, event: HttpContext.(Int) -> Unit): HttpService

    fun all(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun get(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun post(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun put(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun patch(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun delete(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun head(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun options(path: String, event: HttpContext.(String) -> Unit): HttpService

    fun request(array: Array<HttpMethod>, path: String, event: HttpContext.(String) -> Unit): HttpService

    fun before(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun before(event: HttpContext.() -> Unit): HttpService
    fun after(path: String, event: HttpContext.(String) -> Unit): HttpService
    fun after(event: HttpContext.() -> Unit): HttpService

}