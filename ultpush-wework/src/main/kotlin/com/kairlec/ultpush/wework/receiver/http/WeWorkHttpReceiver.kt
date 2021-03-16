package com.kairlec.ultpush.wework.receiver.http

import com.google.common.io.Resources
import com.google.inject.Inject
import com.google.inject.TypeLiteral
import com.google.inject.name.Named
import com.kairlec.ultpush.bind.TypeLiteralAble
import com.kairlec.ultpush.bind.ULTImpl
import com.kairlec.ultpush.component.lifecycle.ULTLoad
import com.kairlec.ultpush.component.lifecycle.ULTRun
import com.kairlec.ultpush.configuration.Config
import com.kairlec.ultpush.configuration.Configuration
import com.kairlec.ultpush.core.*
import com.kairlec.ultpush.core.receiver.Receiver
import com.kairlec.ultpush.core.util.ClassPathResources
import com.kairlec.ultpush.http.HttpCommon.HTML_CT
import com.kairlec.ultpush.http.HttpContext
import com.kairlec.ultpush.http.HttpMethod
import com.kairlec.ultpush.http.HttpService
import com.kairlec.ultpush.http.PartFile
import com.kairlec.ultpush.user.UserHelper
import com.kairlec.ultpush.wework.fromUser
import com.kairlec.ultpush.wework.message.*
import com.kairlec.ultpush.wework.toUser
import com.kairlec.ultpush.wework.withData
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException

@ULTImpl("WeWorkHttpReceiver", false)
class WeWorkHttpReceiver @Inject constructor(
    private val configuration: Configuration,
    private val httpService: HttpService,
    @Named("WeWorkUserHelper") private val userHelper: UserHelper
) : Receiver<WeWorkMessage>() {
    companion object : TypeLiteralAble {
        private val logger = LoggerFactory.getLogger(WeWorkHttpReceiver::class.java)
        override val typeLiteral = object : TypeLiteral<Receiver<WeWorkMessage>>() {}
    }

    private lateinit var config: Config

    @ULTLoad
    fun load() {
        config = configuration.loadYaml("wework") ?: error("Failed to load wework config")
    }

    private infix fun WeWorkMessage.ftt(httpContext: HttpContext) = apply {
        val fromUser = httpContext.getRequestAttribute("from") as String? ?: throw MissingRequestParamException("from")
        val toUser = httpContext.getRequestAttribute("to") as String? ?: throw MissingRequestParamException("to")
        val data = httpContext.getRequestAttribute("token") ?: throw MissingRequestParamException("token")
        this.fromUser(fromUser).toUser(toUser).withData(data)
    }

    fun HttpContext.receiveResult(result: ReceiverResult) {
        if (result.ok) {
            json(object {
                val code = 0
                val message = "OK"
                val data = null
            })
        } else {
            json(object {
                val code = -1
                val message = result.message
                val data = result.data?.stackTraceToString()
            })
        }
    }

    @ULTRun(
        dependNames = ["WeWorkMessageHandler", "WeWorkUserHelper"],
        dependClasses = [HttpService::class]
    )
    fun run() {
        logger.info("reciever hook http service:${httpService.hashCode()}")
        httpService.before {
            this.getRequestParam("from")?.let {
                this.setRequestAttribute("from", it)
            }
            this.getRequestParam("to")?.let {
                this.setRequestAttribute("to", it)
            }
            this.getRequestParam("token")?.let {
                this.setRequestAttribute("token", it)
            }
        }
        httpService.request(arrayOf(HttpMethod.GET, HttpMethod.POST), "/wework/text") {
            val content = getRequestParamMust("content")
            ULTCore.receiveMessage(Text(content) ftt this, this@WeWorkHttpReceiver, true)
        }
        httpService.post("/wework/markdown") {
            val content = getRequestParamMust("content")
            ULTCore.receiveMessage(Markdown(content) ftt this, this@WeWorkHttpReceiver, true)
        }
        httpService.post("/wework/textcard") {
            val title = getRequestParamMust("title")
            val description = getRequestParamMust("description")
            val url = getRequestParamMust("url")
            val btntxt = getRequestParam("btntxt", "详情")
            ULTCore.receiveMessage(TextCard(title, description, url, btntxt) ftt this, this@WeWorkHttpReceiver, true)
        }

        fun PartFile.getMedia(defaultName: String): Media {
            return Media(RawMedia(filename ?: defaultName, contentAsByteArray))
        }

        fun PartFile.getVideoMedia(defaultName: String): VideoMedia {
            return VideoMedia(RawMedia(filename ?: defaultName, contentAsByteArray))
        }
        httpService.post("/wework/image") {
            val file = getMultiPartFileMust("file")
            ULTCore.receiveMessage(Image(file.getMedia("image")) ftt this, this@WeWorkHttpReceiver, true)
        }
        httpService.post("/wework/voice") {
            val file = getMultiPartFileMust("file")
            ULTCore.receiveMessage(Voice(file.getMedia("voice")) ftt this, this@WeWorkHttpReceiver, true)
        }
        httpService.post("/wework/video") {
            val title = getRequestParam("title")
            val description = getRequestParam("description")
            val file = getMultiPartFileMust("file")
            ULTCore.receiveMessage(Video(file.getVideoMedia("video").apply {
                this.description = description
                this.title = title
            }) ftt this, this@WeWorkHttpReceiver, true)
        }
        httpService.post("/wework/file") {
            val file = getMultiPartFileMust("file")
            ULTCore.receiveMessage(File(file.getMedia("file")) ftt this, this@WeWorkHttpReceiver, true)
        }
        httpService.get("/wework/doc") {
            val lang = this.getRequestParam("lang", "zh_CN")

            val resources =
                ClassPathResources.loadAsStream("/com/kairlec/ultpush/wework/Pusher_${lang}.html", javaClass)
                    ?: throw FileNotFoundException("Pusher_${lang}.html")
            contentType = HTML_CT
            out(resources)
        }

        httpService.exception(MissingRequestParamException::class.java) {
            json(object {
                val code = 500
                val message = it.message
                val data = it.paramName
            })
        }

    }


    override fun authenticate(body: WeWorkMessage): AuthenticateStatus<WeWorkMessage> {
        val result = userHelper.getUser(body.fromUser)?.authPassword(body.withData as String)
            ?: return AuthenticateFailed(body, message = "User Not Found")
        return if (result) {
            AuthenticateSuccess(body)
        } else {
            AuthenticateFailed(body, message = "Auth failed")
        }
    }
}