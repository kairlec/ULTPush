package com.kairlec.ultpush.http

data class HeaderValue(val value: String, val params: List<HeaderValueParam> = listOf()) {
    /**
     * 如果缺失或无效,则寻找参数q=X,如果q没有或无效,则为1.0
     */
    val quality: Double =
        params.firstOrNull { it.name == "q" }?.value?.toDoubleOrNull()?.takeIf { it in 0.0..1.0 } ?: 1.0
}