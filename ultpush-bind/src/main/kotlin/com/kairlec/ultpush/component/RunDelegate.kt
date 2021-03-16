package com.kairlec.ultpush.component

interface RunDelegate {
    fun runFinish()
    fun runError(e: Exception?, message: String? = null)
}