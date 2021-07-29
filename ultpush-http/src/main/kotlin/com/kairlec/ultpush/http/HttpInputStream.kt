package com.kairlec.ultpush.http

import com.kairlec.ultpush.util.ReadOnlyEvent
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

abstract class HttpInputStream : InputStream() {
    @Throws(IOException::class)
    fun readLine(byteArray: ByteArray, offset: Int, length: Int): Int {
        if (length <= 0) {
            return 0
        }
        var realOffset = offset
        var count = 0
        var c: Int
        while (read().also { c = it } != -1) {
            byteArray[realOffset++] = c.toByte()
            count++
            if (c == '\n'.code || count == length) {
                break
            }
        }
        return if (count > 0) count else -1
    }

    abstract val finished: Boolean
    abstract val ready: Boolean
    abstract val readEvent: ReadOnlyEvent<Any>
}