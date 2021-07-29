@file:Suppress("unused", "SpellCheckingInspection")

package com.kairlec.ultpush.http

import java.nio.charset.Charset
import java.util.*

/**
 * ContentType头,组成为"[contentType]/[contentSubtype]"
 * @property contentType 首要类型
 * @property contentSubtype 次要类型
 */
class ContentType private constructor(
    val contentType: String,
    val contentSubtype: String,
    existingContent: String,
    parameters: List<HeaderValueParam> = mutableListOf()
) : HeaderValueWithParameters(existingContent, parameters) {

    constructor(
        contentType: String,
        contentSubtype: String,
        parameters: List<HeaderValueParam> = mutableListOf()
    ) : this(
        contentType,
        contentSubtype,
        "$contentType/$contentSubtype",
        parameters
    )

    /**
     * 拷贝原有
     */
    fun withParameter(name: String, value: String): ContentType {
        if (hasParameter(name, value)) return this
        return if (parameters is MutableList) {
            parameters.add(HeaderValueParam(name, value))
            this
        } else {
            ContentType(contentType, contentSubtype, content, parameters + HeaderValueParam(name, value))
        }
    }

    private fun hasParameter(name: String, value: String): Boolean = when (parameters.size) {
        0 -> false
        1 -> parameters[0].let { it.name.equals(name, ignoreCase = true) && it.value.equals(value, ignoreCase = true) }
        else -> parameters.any { it.name.equals(name, ignoreCase = true) && it.value.equals(value, ignoreCase = true) }
    }

    fun withoutParameters(): ContentType = when {
        parameters.isEmpty() -> this
        parameters is MutableList -> apply { (parameters as MutableList).clear() }
        else -> ContentType(contentType, contentSubtype)
    }

    fun match(pattern: ContentType): Boolean {
        if (pattern.contentType != "*" && !pattern.contentType.equals(contentType, ignoreCase = true)) {
            return false
        }

        if (pattern.contentSubtype != "*" && !pattern.contentSubtype.equals(contentSubtype, ignoreCase = true)) {
            return false
        }

        for ((patternName, patternValue) in pattern.parameters) {
            val matches = when (patternName) {
                "*" -> {
                    when (patternValue) {
                        "*" -> true
                        else -> parameters.any { p -> p.value.equals(patternValue, ignoreCase = true) }
                    }
                }
                else -> {
                    val value = parameter(patternName)
                    when (patternValue) {
                        "*" -> value != null
                        else -> value.equals(patternValue, ignoreCase = true)
                    }
                }
            }

            if (!matches) {
                return false
            }
        }
        return true
    }

    override fun equals(other: Any?): Boolean =
        other is ContentType &&
                contentType.equals(other.contentType, ignoreCase = true) &&
                contentSubtype.equals(other.contentSubtype, ignoreCase = true) &&
                parameters == other.parameters

    override fun hashCode(): Int {
        var result = contentType.lowercase(Locale.getDefault()).hashCode()
        result += 31 * result + contentSubtype.lowercase(Locale.getDefault()).hashCode()
        result += 31 * parameters.hashCode()
        return result
    }

    companion object {
        fun parse(value: String): ContentType {
            if (value.isBlank()) return Any

            return parse(value) { parts, parameters ->
                val slash = parts.indexOf('/')

                if (slash == -1) {
                    if (parts.trim() == "*") {
                        return Any
                    }

                    throw BadContentTypeFormatException(value)
                }

                val type = parts.substring(0, slash).trim()

                if (type.isEmpty()) {
                    throw BadContentTypeFormatException(value)
                }

                val subtype = parts.substring(slash + 1).trim()

                if (subtype.isEmpty() || subtype.contains('/')) {
                    throw BadContentTypeFormatException(value)
                }

                ContentType(type, subtype, parameters)
            }
        }

        val Any: ContentType = ContentType("*", "*")

        object Application {
            val Any = ContentType("application", "*")
            val Atom: ContentType = ContentType("application", "atom+xml")
            val Cbor: ContentType = ContentType("application", "cbor")
            val Json: ContentType = ContentType("application", "json")
            val HalJson: ContentType = ContentType("application", "hal+json")
            val JavaScript: ContentType = ContentType("application", "javascript")
            val OctetStream: ContentType = ContentType("application", "octet-stream")
            val FontWoff: ContentType = ContentType("application", "font-woff")
            val Rss: ContentType = ContentType("application", "rss+xml")
            val Xml: ContentType = ContentType("application", "xml")
            val XmlDtd: ContentType = ContentType("application", "xml-dtd")
            val Zip: ContentType = ContentType("application", "zip")
            val GZip: ContentType = ContentType("application", "gzip")

            val FormUrlEncoded: ContentType =
                ContentType("application", "x-www-form-urlencoded")

            val Pdf: ContentType = ContentType("application", "pdf")
            val ProtoBuf: ContentType = ContentType("application", "protobuf")
            val Wasm: ContentType = ContentType("application", "wasm")
            val ProblemJson: ContentType = ContentType("application", "problem+json")
            val ProblemXml: ContentType = ContentType("application", "problem+xml")
        }

        object Audio {
            val Any: ContentType = ContentType("audio", "*")
            val MP4: ContentType = ContentType("audio", "mp4")
            val MPEG: ContentType = ContentType("audio", "mpeg")
            val OGG: ContentType = ContentType("audio", "ogg")
        }

        object Image {
            val Any: ContentType = ContentType("image", "*")
            val APng: ContentType = ContentType("image", "apng")
            val AVif: ContentType = ContentType("image", "avif")
            val GIF: ContentType = ContentType("image", "gif")
            val JPEG: ContentType = ContentType("image", "jpeg")
            val PNG: ContentType = ContentType("image", "png")
            val SVG: ContentType = ContentType("image", "svg+xml")
            val WebP: ContentType = ContentType("image", "webp")
            val XIcon: ContentType = ContentType("image", "x-icon")
        }

        object Message {
            val Any: ContentType = ContentType("message", "*")
            val Http: ContentType = ContentType("message", "http")
        }

        object MultiPart {
            val Any: ContentType = ContentType("multipart", "*")
            val Mixed: ContentType = ContentType("multipart", "mixed")
            val Alternative: ContentType = ContentType("multipart", "alternative")
            val Related: ContentType = ContentType("multipart", "related")
            val FormData: ContentType = ContentType("multipart", "form-data")
            val Signed: ContentType = ContentType("multipart", "signed")
            val Encrypted: ContentType = ContentType("multipart", "encrypted")
            val ByteRanges: ContentType = ContentType("multipart", "byteranges")
        }

        object Text {
            val Any: ContentType = ContentType("text", "*")
            val Plain: ContentType = ContentType("text", "plain")
            val CSS: ContentType = ContentType("text", "css")
            val CSV: ContentType = ContentType("text", "csv")
            val Html: ContentType = ContentType("text", "html")
            val JavaScript: ContentType = ContentType("text", "javascript")
            val VCard: ContentType = ContentType("text", "vcard")
            val Xml: ContentType = ContentType("text", "xml")
            val EventStream: ContentType = ContentType("text", "event-stream")
        }

        object Video {
            val Any: ContentType = ContentType("video", "*")
            val MPEG: ContentType = ContentType("video", "mpeg")
            val MP4: ContentType = ContentType("video", "mp4")
            val OGG: ContentType = ContentType("video", "ogg")
            val QuickTime: ContentType = ContentType("video", "quicktime")
        }
    }
}

fun ContentType.withCharset(charset: Charset): ContentType = withParameter("charset", charset.name())

