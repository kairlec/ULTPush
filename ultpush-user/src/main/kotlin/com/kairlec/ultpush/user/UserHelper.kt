package com.kairlec.ultpush.user

import com.kairlec.ultpush.bind.ULTInterface

@ULTInterface(40)
interface UserHelper {
    fun getUser(username: String): User?
    fun getUsers(nickname: String): List<User>
    fun searchUsers(nickname: String): List<User>
    fun addUsers(vararg users: User)
    fun updateUsers(vararg users: User)
    fun removeUsers(vararg users: User)
    fun removeUsers(vararg usernames: String)
}