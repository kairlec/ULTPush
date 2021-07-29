package com.kairlec.ultpush.wework.user

//import com.fasterxml.jackson.module.kotlin.convertValue
import com.google.inject.Inject
import com.kairlec.ultpush.bind.ULTImpl
import com.kairlec.ultpush.component.lifecycle.ULTInit
import com.kairlec.ultpush.component.lifecycle.ULTLoad
import com.kairlec.ultpush.configuration.Config
import com.kairlec.ultpush.configuration.Configuration
import com.kairlec.ultpush.user.IDUser
import com.kairlec.ultpush.user.IDUserHelper
import com.kairlec.ultpush.user.StringIdUser
//import com.kairlec.ultpush.wework.pusher.objectMapper
import com.kairlec.ultpush.wework.user.User.Companion.insert
import com.kairlec.ultpush.wework.utils.PBKDF2Util
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection

object Users : IdTable<String>() {
    val token = varchar("token", 128)
    val admin = bool("admin").default(false)
    val disable = bool("disable").default(false)
    val username = varchar("username", 128)
    val userId = varchar("userId", 128)
    val salt = varchar("salt", 32)
    val config = text("config").default("{}")
    val acceptLevel = integer("acceptLevel").default(500)
    val acceptKeywords = text("acceptKeywords").default("")
    val refuseKeywords = text("refuseKeywords").default("")

    override val id: Column<EntityID<String>> = varchar("openUserId", 64).entityId()
    override val primaryKey: PrimaryKey by lazy { super.primaryKey ?: PrimaryKey(id) }
}

internal const val SEPARATOR = ":"

//internal fun Map<String, Any>.genToString() = objectMapper.writeValueAsString(this)

//internal fun String.toStringKeyMap(): HashMap<String, Any> = objectMapper.convertValue(objectMapper.readTree(this))

internal fun Set<String>.genToString() = joinToString(SEPARATOR)

internal fun String.toStringSet() = split(SEPARATOR).toMutableSet()

class User(openUserId: EntityID<String>) : Entity<String>(openUserId), StringIdUser {
    companion object : EntityClass<String, User>(Users) {
        internal fun insert(user: User) {
            User.new(user.openUserId.value) {
                userId = user.userId
                salt = user.salt
//                config = user.config
                token = user.token
                admin = user.admin
                disable = user.disable
                name = user.name
                acceptLevel = user.acceptLevel
                acceptKeywords = user.acceptKeywords
                refuseKeywords = user.refuseKeywords
            }
        }

        fun from(user: SimpleUser): User {
            return transaction {
                val effectCount = Users.update({ Users.id eq user.openUserID }) {
                    it[userId] = user.userID
                    it[username] = user.name
                }
                if (effectCount == 0) {
                    new(user.openUserID) {
                        userId = user.userID
                        name = user.name
                        val newSalt = PBKDF2Util.generateSalt()
                        salt = newSalt
                        token = PBKDF2Util.getEncryptedPassword(user.userID, newSalt)
                    }
                } else {
                    User[user.openUserID!!]
                }
            }
        }

    }

    override val uid get() = openUserId.value

    var admin by Users.admin
        private set
    var disable by Users.disable
        private set
    var acceptLevel by Users.acceptLevel
        private set
    val openUserId by Users.id
    override val username get() = userId
    private var name by Users.username
    override val nickname get() = name
    var userId by Users.userId
        private set
    var salt by Users.salt
        private set
    var token by Users.token
        private set
//    var config: Map<String, Any> by Users.config.transform(
//        { obj: Map<String, Any> -> obj.genToString() },
//        { str: String -> str.toStringKeyMap() }
//    )
//        private set
    var acceptKeywords: Set<String> by Users.acceptKeywords.transform(
        { obj -> obj.genToString() },
        { str -> str.toStringSet() }
    )
        private set

    var refuseKeywords: Set<String> by Users.refuseKeywords.transform(
        { obj -> obj.genToString() },
        { str -> str.toStringSet() }
    )
        private set


    override fun authPassword(rawPassword: String): Boolean {
        return PBKDF2Util.authenticate(rawPassword, token, salt)
    }

    override fun updatePassword(rawPassword: String) {
        token = PBKDF2Util.getEncryptedPassword(rawPassword, salt)
    }
}

@ULTImpl("WeWorkUserHelper", default = false)
class UsersHelper @Inject constructor(
    private val configuration: Configuration
) : IDUserHelper<String> {

    private lateinit var config: Config

    @ULTLoad
    fun load() {
        config = configuration.loadYaml("wework") ?: error("Failed to load wework config")
    }

    @ULTInit
    fun init() {
        if (!Files.exists(Path.of("data"))) {
            Files.createDirectories(Path.of("data"))
        }
        config.run {
            val url = get("url")?.stringValue ?: "jdbc:sqlite:data/data.db"
            val driver = get("driver")?.stringValue ?: "org.sqlite.JDBC"
            Database.connect(url, driver)
            transaction {
                SchemaUtils.create(Users)
            }
        }
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }


    private fun assertSupportOperate(
        users: Array<out com.kairlec.ultpush.user.User>,
        event: (Array<User>.() -> Unit)
    ) {
        if (users.all { it is User }) {
            @Suppress("UNCHECKED_CAST")
            event(users as Array<User>)
        } else {
            throw UnsupportedOperationException("WeWork user helper just can apply to wework user")
        }
    }

    override fun getUser(username: String): com.kairlec.ultpush.user.User? {
        return transaction { User.find { Users.userId eq username }.firstOrNull() }
    }

    override fun getUsers(nickname: String): List<com.kairlec.ultpush.user.User> {
        return transaction { User.find { Users.username eq nickname }.toList() }
    }

    override fun searchUsers(nickname: String): List<com.kairlec.ultpush.user.User> {
        return transaction { User.find { Users.username like "%${nickname}%" }.toList() }
    }

    override fun addUsers(vararg users: com.kairlec.ultpush.user.User) {
        assertSupportOperate(users) {
            forEach {
                transaction {
                    insert(it)
                }
            }
        }
    }

    override fun updateUsers(vararg users: com.kairlec.ultpush.user.User) {
        assertSupportOperate(users) {
            forEach { user ->
                transaction {
                    Users.update({ Users.id eq user.openUserId }) {
                        it[token] = user.token
                        it[salt] = user.salt
                        it[admin] = user.admin
                        it[disable] = user.disable
                        it[acceptLevel] = user.acceptLevel
                        it[acceptKeywords] = user.acceptKeywords.genToString()
                        it[refuseKeywords] = user.refuseKeywords.genToString()
//                        it[config] = user.config.genToString()
                    }
                }
            }
        }
    }

    override fun removeUsers(vararg users: com.kairlec.ultpush.user.User) {
        assertSupportOperate(users) {
            forEach {
                transaction {
                    Users.deleteIgnoreWhere { Users.id eq it.openUserId }
                }
            }
        }
    }

    override fun removeUsers(vararg usernames: String) {
        usernames.forEach {
            transaction {
                Users.deleteIgnoreWhere { Users.userId eq it }
            }
        }
    }

    override fun getUserByUID(uid: String): IDUser<String>? {
        return transaction { User.find { Users.id eq uid }.singleOrNull() }
    }

    override fun removeUsersByUID(vararg uids: String) {
        uids.forEach {
            transaction {
                Users.deleteIgnoreWhere { Users.id eq it }
            }
        }
    }

}