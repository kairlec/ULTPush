package com.kairlec.ultpush.wework.pusher

import com.google.inject.Inject
import com.google.inject.TypeLiteral
import com.kairlec.ultpush.bind.TypeStrict
import com.kairlec.ultpush.bind.ULTImpl
import com.kairlec.ultpush.component.lifecycle.ULTInit
import com.kairlec.ultpush.component.lifecycle.ULTLoad
import com.kairlec.ultpush.configuration.Config
import com.kairlec.ultpush.configuration.Configuration
import com.kairlec.ultpush.core.AuthenticateStatus
import com.kairlec.ultpush.core.AuthenticateSuccess
import com.kairlec.ultpush.core.pusher.Pusher
import com.kairlec.ultpush.core.pusher.PusherMsg
import com.kairlec.ultpush.wework.WeWorkApplicationHelperCreator
import com.kairlec.ultpush.wework.WeWorkEnterpriseHelperCreator
import com.kairlec.ultpush.wework.message.*
import com.kairlec.ultpush.wework.user.User
import com.kairlec.ultpush.wework.user.WeWorkAddressBookHelper
import org.slf4j.LoggerFactory

@ULTImpl("WeWorkPusher", false)
class WeWorkPusher @Inject constructor(
    private val configuration: Configuration
) : Pusher<WeWorkMessage>() {
    companion object : TypeStrict {
        override val type = object : TypeLiteral<Pusher<WeWorkMessage>>() {}
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    private lateinit var config: Config

    private lateinit var senderHelper: WeWorkSenderHelper
    private lateinit var addressBookHelper: WeWorkAddressBookHelper

    private lateinit var applicationHelperCreator: WeWorkApplicationHelperCreator
    private lateinit var enterpriseHelperCreator: WeWorkEnterpriseHelperCreator

    @ULTLoad
    fun load() {
        config = configuration.loadYaml("wework") ?: error("Failed to load wework config")
    }

    @ULTInit(dependNames = ["WeWorkUserHelper"])
    suspend fun init() {
        config.apply {
            val enterpriseID = get("enterpriseID")?.stringValue ?: error("[String]enterpriseID(企业ID)不能为空")
            val applicationKey = get("applicationKey")?.stringValue ?: error("[String]applicationkey(应用密钥)不能为空")
            val applicationID = get("applicationID")?.integerValue ?: error("[Int]applicationID(应用ID)不能为空")
            val corpSecret = get("enterpriseSecret")?.stringValue ?: error("[String]enterpriseSecret(企业通讯录密钥)不能为空")
            val validateCertificateChains = get("validateCertificateChains")?.booleanValue ?: true
            val filterDepartmentList = get("filterDepartmentList")?.arrayValue?.map { it.asString() }
            applicationHelperCreator =
                WeWorkApplicationHelperCreator(enterpriseID, applicationID, applicationKey, validateCertificateChains)
            senderHelper = applicationHelperCreator.newInstant()
            enterpriseHelperCreator = WeWorkEnterpriseHelperCreator(enterpriseID, corpSecret, validateCertificateChains)
            addressBookHelper = enterpriseHelperCreator.newInstant()

            val departmentList = if (filterDepartmentList != null) {
                addressBookHelper.getDepartmentList()
                    .filter { it.name in filterDepartmentList || it.nameEn in filterDepartmentList }
            } else {
                addressBookHelper.getDepartmentList()
            }
            if (departmentList.isEmpty()) {
                logger.warn("no department valid input")
            } else {
                departmentList.forEach {
                    val simpleUsers = addressBookHelper.getUserSimpleList(it.id, true)
                    simpleUsers.forEach { simpleUser ->
                        val user = User.from(simpleUser)
                    }
                }
            }
        }
    }

    override fun authenticate(body: WeWorkMessage): AuthenticateStatus<WeWorkMessage> {
        return AuthenticateSuccess(body)
    }

    override suspend fun push(msg: PusherMsg) {
        if (msg is WeWorkMessage) {
            when (msg) {
                is Text -> {
                    senderHelper.sendText(msg)
                }
                is Image -> {
                    senderHelper.sendImage(msg)
                }
                is Voice -> {
                    senderHelper.sendVoice(msg)
                }
                is Video -> {
                    senderHelper.sendVideo(msg)
                }
                is File -> {
                    senderHelper.sendFile(msg)
                }
                is TextCard -> {
                    senderHelper.sendTextCard(msg)
                }
                is News -> {
                    senderHelper.sendNews(msg)
                }
                is MpNews -> {
                    senderHelper.sendMpNews(msg)
                }
                is Markdown -> {
                    senderHelper.sendMarkdown(msg)
                }
                is TaskCard -> {
                    senderHelper.sendTaskCard(msg)
                }
            }
        }
    }
}