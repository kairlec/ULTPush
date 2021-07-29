package com.kairlec.ultpush.configuration

import java.io.File
import java.io.InputStream

val File.text
    get() = if (exists()) {
        readText(Charsets.UTF_8)
    } else {
        null
    }

val InputStream?.text
    get() = this?.use { it.reader(Charsets.UTF_8).use { isr -> isr.readText() } }
