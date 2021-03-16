package com.kairlec.ultpush.user

interface IDUserHelper<ID> : UserHelper {
    fun getUserByUID(uid: ID): IDUser<ID>?
    fun removeUsersByUID(vararg uids: ID)
}