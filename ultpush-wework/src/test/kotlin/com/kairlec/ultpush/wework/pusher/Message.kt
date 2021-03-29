package com.kairlec.ultpush.wework.pusher

import com.kairlec.ultpush.wework.message.*
import kotlin.random.Random


object Message {
    fun CharRange.randomString(length: Int) = (1..length).map { randomChar }.joinToString("")

    val CharRange.randomChar: Char
        get() = Random.nextInt(first.toInt(), last.toInt()).toChar()

    fun CharRange.Companion.randomString(length: Int, vararg ranges: CharRange): String {
        return (1..length).map { ranges[(Random.nextInt(ranges.size))].randomChar }.joinToString("")
    }

    private fun load(filename: String): ByteArray {
        return Message::class.java.classLoader.getResourceAsStream(filename)!!.readAllBytes()
    }

    val video = Video(VideoMedia(RawMedia("testVideo.mp4", load("video.mp4"))))
    val voice = Voice(Media(RawMedia("testVoice.amr", load("voice.amr"))))
    val image = Image(Media(RawMedia("testImage.png", load("image.jpg"))))
    val text = Text("testText")

    val file = File(Media(RawMedia("testFile.txt", load("file.txt"))))
    val textCard = TextCard("testTextCardTitle", "testTextCardDescription", "https://www.kairlec.com", "测试按钮")
    val news = News(
        "testNewsTitle",
        "https://www.kairlec.com",
        "testNewsDescription",
        "https://pic-cdn.kairlec.com/acg/st/63861216.jpg"
    )
    val mpNews = MpNews(
        "testMpNewsTitle",
        RawMedia("testImage,png", load("image.jpg")),
        "testMpNewsContent",
        "Kairlec",
        "https://www.kairlec.com"
    )
    val markdown = Markdown("### testMarkdownTitle\n> Testlined`ok`")
    val taskCard = TaskCard(
        "testTaskCardTitle",
        "testTaskCardDescription",
        ('a'..'z').randomString(16),
        arrayOf(Btn("ok", "OK"), Btn("cancel", "Cancel")),
        "https://www.kairlec.com"
    )
}