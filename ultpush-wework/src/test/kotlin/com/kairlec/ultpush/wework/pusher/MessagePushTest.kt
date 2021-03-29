package com.kairlec.ultpush.wework.pusher

import com.kairlec.ultpush.bind.ULTInjector
import com.kairlec.ultpush.core.Application
import com.kairlec.ultpush.wework.message.WeWorkMessage
import com.kairlec.ultpush.wework.toUser
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class MessagePushTest {
    private val pusher: WeWorkPusher = runBlocking {
        Application.start()
        ULTInjector.getGenericInstance(WeWorkPusher::class.java, true)
    }

    private val WeWorkMessage.t: WeWorkMessage
        get() = this.toUser("SuTangHuan")

    @Test
    fun testText() {
        runBlocking {
            pusher.push(Message.text.t)
        }
    }

    @Test
    fun testMpNews() {
        runBlocking {
            // Bug with 'invalid media_id'
            pusher.push(Message.mpNews.t)
        }
    }

    @Test
    fun testTaskCard() {
        runBlocking {
            pusher.push(Message.taskCard.t)
        }
    }

    @Test
    fun testMarkdown() {
        runBlocking {
            pusher.push(Message.markdown.t)
        }
    }

    @Test
    fun testNews() {
        runBlocking {
            pusher.push(Message.news.t)
        }
    }

    @Test
    fun testImage() {
        runBlocking {
            pusher.push(Message.image.t)
        }
    }

    @Test
    fun testVoice() {
        runBlocking {
            pusher.push(Message.voice.t)
        }
    }

    @Test
    fun testFile() {
        runBlocking {
            pusher.push(Message.file.t)
        }
    }

    @Test
    fun testTextCard() {
        runBlocking {
            pusher.push(Message.textCard.t)
        }
    }

    @Test
    fun testVideo() {
        runBlocking {
            // Bug with 'invalid media_id'
            pusher.push(Message.video.t)
        }
    }
}