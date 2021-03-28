package com.kairlec.ultpush.wework.message

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject

abstract class WeWorkMessageSubMessageSerializer<T : WeWorkMessage>(serializer: KSerializer<T>) :
    JsonTransformingSerializer<T>(serializer) {
    private fun translate(input: String): String {
        return input.replace("to([A-Z])".toRegex()) {
            "to${it.groupValues[1].toLowerCase()}"
        }.replace("[A-Z]".toRegex()) {
            "_${it.groupValues[0].toLowerCase()}"
        }
    }

    private fun deTranslate(input: String): String {
        return input.replace("_([a-z])".toRegex()) {
            it.groupValues[1].toUpperCase()
        }.replace("to([a-z])".toRegex()) {
            "to${it.groupValues[1].toUpperCase()}"
        }
    }

    override fun transformSerialize(element: JsonElement): JsonElement =
        JsonObject(element.jsonObject.mapKeys { (key, _) ->
            translate(key)
        }/*.filterNot { it.key == "from_user" }*/)

    override fun transformDeserialize(element: JsonElement): JsonElement =
        JsonObject(element.jsonObject.mapKeys { (key, _) ->
            println("key=$key ,deKey=${deTranslate(key)}")
            deTranslate(key)
        })
}


object TextSerializer : WeWorkMessageSubMessageSerializer<Text>(Text.serializer())
object ImageSerializer : WeWorkMessageSubMessageSerializer<Image>(Image.serializer())
object VoiceSerializer : WeWorkMessageSubMessageSerializer<Voice>(Voice.serializer())
object VideoSerializer : WeWorkMessageSubMessageSerializer<Video>(Video.serializer())
object FileSerializer : WeWorkMessageSubMessageSerializer<File>(File.serializer())
object TextCardSerializer : WeWorkMessageSubMessageSerializer<TextCard>(TextCard.serializer())
object NewsSerializer : WeWorkMessageSubMessageSerializer<News>(News.serializer())
object MpNewsSerializer : WeWorkMessageSubMessageSerializer<MpNews>(MpNews.serializer())
object MarkdownSerializer : WeWorkMessageSubMessageSerializer<Markdown>(Markdown.serializer())
object TaskCardSerializer : WeWorkMessageSubMessageSerializer<TaskCard>(TaskCard.serializer())