package com.kairlec.ultpush.http

import io.javalin.http.UploadedFile
import java.io.InputStream

class JavalinPartFile : PartFile {
    private val _contentByteArray: ByteArray?
    private val _contentInputStream: InputStream?
    override val filename: String?
    val size: Long
    override val contentLength: Long
        get() = size
    override val contentType: String
    override val contentAsByteArray: ByteArray
        get() = _contentByteArray ?: _contentInputStream!!.readAllBytes()
    override val contentAsInputStream: InputStream
        get() = _contentInputStream ?: _contentByteArray!!.inputStream()

    constructor(uploadedFile: UploadedFile) {
        _contentInputStream = uploadedFile.content
        _contentByteArray = null
        this.contentType = uploadedFile.contentType
        this.filename = uploadedFile.filename
        this.size = uploadedFile.size
    }

    constructor(inputStream: InputStream, contentType: String, size: Long, filename: String? = null) {
        _contentInputStream = inputStream
        _contentByteArray = null
        this.contentType = contentType
        this.size = size
        this.filename = filename
    }

    constructor(
        byteArray: ByteArray,
        contentType: String,
        size: Long = byteArray.size.toLong(),
        filename: String? = null
    ) {
        _contentByteArray = byteArray
        _contentInputStream = null
        this.contentType = contentType
        this.size = size
        this.filename = filename
    }
}