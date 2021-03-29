package com.kairlec.ultpush.wework


import com.kairlec.ultpush.wework.message.*
import com.kairlec.ultpush.wework.pusher.PusherException
import com.kairlec.ultpush.wework.utils.urlEncode
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.slf4j.LoggerFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.X509TrustManager
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

/**
 * 发送实现
 */
@Suppress("SpellCheckingInspection")
object SenderKtor {
    val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 获取异常构造函数
     * @return 异常构造函数
     */
    inline fun <reified R : PusherException> getExceptionConstructor(): KFunction<R> {
        return R::class.primaryConstructor
            ?: error("Cannot get class[${R::class.java.name}] primary constructor")
    }

    /**
     * 发送get请求并获取json结果映射到指定类
     * @param url 要请求的地址
     * @param validateCertificateChains 是否验证证书
     * @param dataname 返回的json的包装字段,若传入null,则为根
     * @param headers HTTP Header
     * @param warpNode 指示在处理映射前该如何包装JsonNode
     * @return 映射后得到的类
     * @see resultMap
     */
    suspend inline fun <reified T : Any, reified R : PusherException> getResultMap(
        url: String,
        validateCertificateChains: Boolean,
        headers: List<Pair<String, String>>? = null,
    ): T {
        val exceptionConstructor = getExceptionConstructor<R>()
        try {
            return get(url, validateCertificateChains, headers)
        } catch (e: Throwable) {
            throw exceptionConstructor.call(-1, e, null)
        }
    }

    /**
     * 发送post请求并获取json结果映射到指定类
     * @param url 要请求的地址
     * @param data 传入的类,将自动转换为json
     * @param validateCertificateChains 是否验证证书
     * @param dataname 返回的json的包装字段,若传入null,则为根
     * @param headers HTTP Header
     * @param warpNode 指示在处理映射前该如何包装JsonNode
     * @return 映射后得到的类
     * @see resultMap
     */
    suspend inline fun <reified T : Any, reified R : PusherException> postJsonResultMap(
        url: String,
        data: Any,
        validateCertificateChains: Boolean,
        headers: List<Pair<String, String>>? = null,
    ): T {
        val exceptionConstructor = getExceptionConstructor<R>()
        try {
            return postJson(url, data, validateCertificateChains, headers)
        } catch (e: Throwable) {
            throw exceptionConstructor.call(-1, e, null)
        }
    }

    /**
     * 发送post请求并获取json结果映射到指定类
     * @param url 要请求的地址
     * @param validateCertificateChains 是否验证证书
     * @param form 传入的form表单
     * @param dataname 返回的json的包装字段,若传入null,则为根
     * @param headers HTTP Header
     * @param warpNode 指示在处理映射前该如何包装JsonNode
     * @return 映射后得到的类
     * @see resultMap
     */
    suspend inline fun <reified T : Any, reified R : PusherException> postFormResultMap(
        url: String,
        validateCertificateChains: Boolean,
        form: List<Pair<String, String>>? = null,
        headers: List<Pair<String, String>>? = null,
    ): T {
        val exceptionConstructor = getExceptionConstructor<R>()
        try {
            return postForm(url, validateCertificateChains, headers, form)
        } catch (e: Throwable) {
            throw exceptionConstructor.call(-1, e, null)
        }
    }

    /**
     * 发送post请求并获取json结果映射到指定类
     * @param url 要请求的地址
     * @param file 文件内容
     * @param filename 文件名
     * @param validateCertificateChains 是否验证证书
     * @param dataname 返回的json的包装字段,若传入null,则为根
     * @param headers HTTP Header
     * @param warpNode 指示在处理映射前该如何包装JsonNode
     * @return 映射后得到的类
     * @see resultMap
     */
    suspend inline fun <reified T : Any, reified R : PusherException> uploadResultMap(
        url: String,
        file: ByteArray,
        filename: String,
        validateCertificateChains: Boolean,
        headers: List<Pair<String, String>>? = null,
    ): T {
        val exceptionConstructor = getExceptionConstructor<R>()
        try {
            return upload(url, file, filename, validateCertificateChains, headers)
        } catch (e: Throwable) {
            throw exceptionConstructor.call(-1, e, null)
        }
    }

    private val kotlinxJsonSerializer = KotlinxSerializer(json = kotlinx.serialization.json.Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            contextual(Text::class, TextSerializer)
            contextual(Image::class, ImageSerializer)
            contextual(Voice::class, VoiceSerializer)
            contextual(Video::class, VideoSerializer)
            contextual(File::class, FileSerializer)
            contextual(TextCard::class, TextCardSerializer)
            contextual(News::class, NewsSerializer)
            contextual(MpNews::class, MpNewsSerializer)
            contextual(Markdown::class, MarkdownSerializer)
            contextual(TaskCard::class, TaskCardSerializer)
        }
    })

    private val httpClient = HttpClient(CIO) {
        developmentMode = true
        install(JsonFeature) {
            serializer = kotlinxJsonSerializer
        }
    }
    private val httpClientNoValidateCertificateChains = HttpClient(CIO) {
        developmentMode = true
        install(JsonFeature) {
            serializer = kotlinxJsonSerializer
        }
        engine {
            https {
                trustManager = object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return emptyArray()
                    }

                    override fun checkClientTrusted(certs: Array<X509Certificate?>?, authType: String?) {
                    }

                    override fun checkServerTrusted(certs: Array<X509Certificate?>?, authType: String?) {
                    }
                }
            }
        }
    }

    fun getHttpClient(validateCertificateChains: Boolean) = if (validateCertificateChains) {
        httpClientNoValidateCertificateChains
    } else {
        httpClient
    }


    /**
     * 发送带有Form表单的post请求
     * @param url 请求地址
     * @param validateCertificateChains 是否取消证书校验
     * @param headers HTTP Header
     * @param form Form表单
     */
    suspend inline fun <reified T : Any> postForm(
        url: String,
        validateCertificateChains: Boolean,
        headers: List<Pair<String, String>>? = null,
        form: List<Pair<String, String>>? = null
    ): T {
        val formString = form?.let {
            StringBuilder().apply {
                form.forEachIndexed { index, (name, value) ->
                    append(if (index == 0) "" else "&")
                    append(name, "=", value.urlEncode())
                }
            }.toString()
        }
        return getHttpClient(validateCertificateChains).post(url) {
            headers {
                headers?.let {
                    for (header in it) {
                        header(header.first, header.second)
                    }
                }
            }
            body = TextContent(
                text = formString ?: "",
                contentType = ContentType.Application.FormUrlEncoded
            )
        }
    }

    /**
     * 发送post请求
     * @param url 请求地址
     * @param data 请求体,将自动转换为Json
     * @param validateCertificateChains 是否取消证书校验
     * @param headers HTTP Header
     */
    suspend inline fun <reified T : Any> postJson(
        url: String,
        data: Any,
        validateCertificateChains: Boolean,
        headers: List<Pair<String, String>>? = null
    ): T {
        return getHttpClient(validateCertificateChains).post(url) {
            headers {
                headers?.let {
                    for (header in it) {
                        header(header.first, header.second)
                    }
                }
            }
            contentType(ContentType.Application.Json)
            body = data
        }
    }

    /**
     * 发送get请求
     * @param url 请求地址
     * @param validateCertificateChains 是否取消证书校验
     * @param headers HTTP Header
     */
    suspend inline fun <reified T : Any> get(
        url: String,
        validateCertificateChains: Boolean,
        headers: List<Pair<String, String>>? = null
    ): T {
        return getHttpClient(validateCertificateChains).get(url) {
            headers {
                headers?.let {
                    for (header in it) {
                        header(header.first, header.second)
                    }
                }
            }
            //contentType(ContentType.Application.Json)
        }
    }

    /**
     * 上传文件(Post)
     * @param url 请求地址
     * @param file 文件内容
     * @param filename 文件名
     * @param validateCertificateChains 是否取消证书校验
     * @param headers HTTP Header
     */
    suspend inline fun <reified T : Any> upload(
        url: String,
        file: ByteArray,
        filename: String,
        validateCertificateChains: Boolean,
        headers: List<Pair<String, String>>? = null
    ): T {
        val boundary = UUID.randomUUID().toString()
        val first =
            "--${boundary}\r\nContent-Disposition: form-data; name=\"media\"; filename=\"$filename\"; filelength=${file.size}\r\nContent-Type: application/octet-stream\r\n\r\n"
        val bodyByteArray =
            first.toByteArray(Charsets.UTF_8) + file + "\r\n--${boundary}--\r\n".toByteArray(Charsets.UTF_8)
        return getHttpClient(validateCertificateChains).post(url) {
            headers {
                headers?.let {
                    for (header in it) {
                        header(header.first, header.second)
                    }
                }
            }
            body = ByteArrayContent(
                bytes = bodyByteArray,
                contentType = ContentType.MultiPart.FormData.withParameter("boundary", boundary)
            )
        }
    }
}