package com.kairlec.ultpush.user

interface UserConfig {
    val acceptKeywords: Set<String>
    val refuseKeywords: Set<String>
    val acceptLevel: Int
}