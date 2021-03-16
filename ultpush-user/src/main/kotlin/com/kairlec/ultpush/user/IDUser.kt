package com.kairlec.ultpush.user

interface IDUser<ID> : User {
    val uid: ID
}