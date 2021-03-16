package com.kairlec.ultpush.user

interface User {
    val username: String
    val nickname: String
    fun authPassword(rawPassword: String): Boolean
    fun updatePassword(rawPassword: String)
}