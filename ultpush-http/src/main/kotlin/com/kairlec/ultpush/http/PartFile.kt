package com.kairlec.ultpush.http

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

interface PartFile {
    val contentAsInputStream: InputStream
    val contentAsByteArray: ByteArray
    val contentType: String?
    val filename: String?
    val contentLength: Long

    fun transferTo(file: File) {
        transferTo(file.outputStream())
    }

    fun transferTo(path: Path) {
        transferTo(path.toFile())
    }

    fun transferTo(outputStream: OutputStream) {
        outputStream.use {
            contentAsInputStream.use { input ->
                input.transferTo(it)
            }
        }
    }
}