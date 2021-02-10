package com.kairlec.ultpush.http

import java.io.InputStream

class MultiPart {
    val _contentByteArray: ByteArray?
    val _contentInputStream: InputStream?
    val size: Long
    val contentType: String
    val contentByteArray: ByteArray
        get() = _contentByteArray ?: _contentInputStream!!.readAllBytes()
    val contentInputStream: InputStream
        get() = _contentInputStream ?: _contentByteArray!!.inputStream()

    constructor(inputStream: InputStream, contentType: String, size: Long) {
        _contentInputStream = inputStream
        _contentByteArray = null
        this.contentType = contentType
        this.size = size
    }

    constructor(byteArray: ByteArray, contentType: String, size: Long = byteArray.size.toLong()) {
        _contentByteArray = byteArray
        _contentInputStream = null
        this.contentType = contentType
        this.size = size
    }
}