@file:Suppress("unused", "SpellCheckingInspection")

package com.kairlec.ultpush.http

import java.io.Serializable
import java.util.*
import kotlin.IllegalArgumentException
import kotlin.String

class HttpCookie(
    val name: String,
    var value: String,
    var comment: String? = null,
    domain: String? = null,
    var maxAge: Int = -1,
    var path: String? = null,
    var secure: Boolean? = null,
    var version: Int = 1,
    var httpOnly: Boolean = false
) : Serializable {
    companion object {
        const val serialVersionUID = -6379304469934892971L
        private val TSPECIALS =
            if (System.getProperty("org.glassfish.web.rfc2109_cookie_names_enforced", "true")
                    .toBooleanStrictOrNull() != false
            ) {
                "/()<>@,;:\\\"[]?={} \t"
            } else {
                ",; "
            }

        private fun tokenCheck(value: String): Boolean {
            return !value.any { it.code < 0x20 || it.code > 0x7f || it in TSPECIALS }
        }
    }

    init {
        if (name.isBlank()) {
            throw IllegalArgumentException("Cookie name must not be null or empty")
        }
        if (!tokenCheck(name) ||
            name.equals("Comment", true) || // rfc2109
            name.equals("Discard", true) || // rfc2109
            name.equals("Domain", true) || // rfc2109
            name.equals("Expires", true) || // (old cookies)
            name.equals("Max-Age", true) || // rfc2019
            name.equals("Path", true) || // rfc2109
            name.equals("Secure", true) || // rfc2109
            name.equals("Version", true) || // rfc2109
            name.startsWith("$") // rfc2109
        ) {
            throw IllegalArgumentException("Cookie name $name is a reserved token")
        }
    }

    var domain: String? = domain
        set(value) {
            field = value?.lowercase(Locale.ENGLISH)
        }

}