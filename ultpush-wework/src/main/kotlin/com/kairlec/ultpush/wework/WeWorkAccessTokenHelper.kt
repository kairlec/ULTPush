package com.kairlec.ultpush.wework

import com.kairlec.ultpush.wework.utils.UrlBuilder


@Suppress("SpellCheckingInspection")
class WeWorkAccessTokenHelper(corpid: String, corpsecret: String, validateCertificateChains: Boolean) : AccessTokenHelper(validateCertificateChains) {
    override val url: String = UrlBuilder("https://qyapi.weixin.qq.com/cgi-bin/gettoken")
            .addQueryParameter("corpid", corpid)
            .addQueryParameter("corpsecret", corpsecret)
            .build()

    init {
        update()
    }
}