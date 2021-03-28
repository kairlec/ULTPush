package com.kairlec.ultpush.component

interface RunDelegate {
    suspend fun runFinish()
    suspend fun runError(e: Exception?, message: String? = null)
}