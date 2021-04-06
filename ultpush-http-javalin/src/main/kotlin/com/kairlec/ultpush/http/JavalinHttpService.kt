package com.kairlec.ultpush.http

import com.google.inject.Inject
import com.kairlec.ultpush.bind.ULTImpl
import com.kairlec.ultpush.component.lifecycle.ULTLoad
import com.kairlec.ultpush.component.lifecycle.ULTRun
import com.kairlec.ultpush.configuration.Config
import com.kairlec.ultpush.configuration.Configuration
import io.javalin.Javalin
import org.slf4j.LoggerFactory
import java.lang.Exception

@ULTImpl("JavalinHttpService")
class JavalinHttpService @Inject constructor(
    private val configuration: Configuration
) : HttpService {
    private val logger = LoggerFactory.getLogger(JavalinHttpService::class.java)

    private lateinit var config: Config

    lateinit var app: Javalin

    @ULTLoad
    fun load() {
        val config = configuration.load("javalin")
        if (config != null) {
            this.config = config
        }
    }

    @ULTRun(async = false)
    fun run() {
        var port = 80
        var host = "0.0.0.0"
        app = Javalin.create {
            if (this::config.isInitialized) {
                config.run {
                    get("MaxRequestSize") {
                        ifInteger { it.maxRequestSize = this.toLong() }
                    }
                    get("DefaultContentType") {
                        ifString { it.defaultContentType = this }
                    }
                    get("port") {
                        ifInteger { port = this }
                    }
                    get("host") {
                        ifString { host = this }
                    }
                    get("showBanner") {
                        ifBoolean { it.showJavalinBanner = this }
                    }
                }
            }
        }.start(host, port)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Throwable> exception(exceptionClass: Class<T>, event: HttpContext.(T) -> Unit): HttpService {
        if (Exception::class.java.isAssignableFrom(exceptionClass)) {
            app.exception(exceptionClass as Class<out Exception>) { exception, ctx ->
                event(
                    JavalinHttpContext(ctx, HttpScope.EXCEPTION),
                    exception as T
                )
            }
        }
        return this
    }

    override fun error(statusCode: Int, event: HttpContext.(Int) -> Unit): HttpService {
        app.error(statusCode) {
            event(JavalinHttpContext(it, HttpScope.ERROR), statusCode)
        }
        return this
    }

    override fun get(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.get(path) {
            event(JavalinHttpContext(it, HttpScope.NORMAL), path)
        }
        return this
    }

    override fun post(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.post(path) {
            event(JavalinHttpContext(it, HttpScope.NORMAL), path)
        }
        return this
    }

    override fun put(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.put(path) {
            event(JavalinHttpContext(it, HttpScope.NORMAL), path)
        }
        return this
    }

    override fun patch(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.patch(path) {
            event(JavalinHttpContext(it, HttpScope.NORMAL), path)
        }
        return this
    }

    override fun delete(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.delete(path) {
            event(JavalinHttpContext(it, HttpScope.NORMAL), path)
        }
        return this
    }

    override fun head(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.head(path) {
            event(JavalinHttpContext(it, HttpScope.NORMAL), path)
        }
        return this
    }

    override fun options(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.options(path) {
            event(JavalinHttpContext(it, HttpScope.NORMAL), path)
        }
        return this
    }

    override fun after(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.after {
            event(JavalinHttpContext(it, HttpScope.AFTER), path)
        }
        return this
    }

    override fun after(event: HttpContext.() -> Unit): HttpService {
        app.after {
            event(JavalinHttpContext(it, HttpScope.AFTER))
        }
        return this
    }

    override fun before(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.before(path) {
            event(JavalinHttpContext(it, HttpScope.BEFORE), path)
        }
        return this
    }

    override fun before(event: HttpContext.() -> Unit): HttpService {
        app.before {
            event(JavalinHttpContext(it, HttpScope.BEFORE))
        }
        return this
    }

    override fun all(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.before(path) {
            event(JavalinHttpContext(it, HttpScope.NORMAL), path)
        }
        return this
    }

    override fun request(array: Array<HttpMethod>, path: String, event: HttpContext.(String) -> Unit): HttpService {
        array.distinct().forEach {
            when (it) {
                HttpMethod.TRACE,
                HttpMethod.CONNECT ->
                    throw MethodNotSupportException(it.name)
                HttpMethod.DELETE ->
                    delete(path, event)
                HttpMethod.GET ->
                    get(path, event)
                HttpMethod.POST ->
                    post(path, event)
                HttpMethod.PATCH ->
                    patch(path, event)
                HttpMethod.PUT ->
                    put(path, event)
                HttpMethod.HEAD ->
                    head(path, event)
                HttpMethod.OPTIONS ->
                    options(path, event)
            }
        }
        return this
    }
}
