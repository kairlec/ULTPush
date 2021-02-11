package com.kairlec.ultpush.http

import com.google.inject.Inject
import com.google.inject.Singleton
import com.kairlec.ultpush.bind.ULTImpl
import com.kairlec.ultpush.bind.ULTInject
import com.kairlec.ultpush.component.ULTInit
import com.kairlec.ultpush.component.ULTLoad
import com.kairlec.ultpush.component.ULTRun
import com.kairlec.ultpush.configuration.Config
import com.kairlec.ultpush.configuration.Configuration
import io.javalin.Javalin
import io.javalin.http.Context
import io.javalin.http.ExceptionHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.lang.Exception

@Singleton
@ULTImpl("JavalinHttpService")
class JavalinHttpService @Inject constructor(
    private val configuration: Configuration
) : HttpService {
    private val logger = LoggerFactory.getLogger(javaClass)

    private lateinit var config: Config

    lateinit var app: Javalin

    @ULTLoad
    fun load() {
        val config = configuration.loadYaml("javalin")
        if (config != null) {
            this.config = config
        }
    }

    @ULTRun(async = false, asyncTimeout = 100)
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

    override fun <T : Throwable> exception(exceptionClass: Class<T>, event: HttpContext.(T) -> Unit): HttpService {
        if (Exception::class.java.isAssignableFrom(exceptionClass)) {
            app.exception(exceptionClass as Class<out Exception>) { exception, ctx ->
                event(
                    ctx.custom,
                    exception as T
                )
            }
        }
        return this
    }

    override fun error(statusCode: Int, event: HttpContext.(Int) -> Unit): HttpService {
        app.error(statusCode) {
            event(it.custom, statusCode)
        }
        return this
    }

    override fun get(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.get(path) {
            event(it.custom, path)
        }
        return this
    }

    override fun post(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.post(path) {
            event(it.custom, path)
        }
        return this
    }

    override fun put(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.put(path) {
            event(it.custom, path)
        }
        return this
    }

    override fun patch(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.patch(path) {
            event(it.custom, path)
        }
        return this
    }

    override fun delete(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.delete(path) {
            event(it.custom, path)
        }
        return this
    }

    override fun head(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.head(path) {
            event(it.custom, path)
        }
        return this
    }

    override fun options(path: String, event: HttpContext.(String) -> Unit): HttpService {
        app.options(path) {
            event(it.custom, path)
        }
        return this
    }
}

interface TRR

fun main() {
    val javalin = ULTInject.getInstance(JavalinHttpService::class.java)
    javalin.get("/") {
        json("OK")
    }
}