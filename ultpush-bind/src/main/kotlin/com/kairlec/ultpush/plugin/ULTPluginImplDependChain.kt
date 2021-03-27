package com.kairlec.ultpush.plugin

class ULTPluginImplStatusChain(
    val pluginImpl: ULTPluginImpl,
    val status: Boolean,
    val exception: Throwable? = null,
    val message: String? = exception?.message,
    val parent: ULTPluginImplStatusChain? = null
) {
    override fun toString(): String {
        return if (status) {
            "[Success]"
        } else {
            buildString {
                append("[name=${pluginImpl.name}][class=${pluginImpl.clazz.name}]")
                if (message != null) {
                    append("message:")
                    append(message)
                }
                if (exception != null) {
                    append(System.lineSeparator())
                    append("stackTrace:")
                    append(exception.stackTraceToString())
                }
                if (parent != null) {
                    append(System.lineSeparator())
                    append("by:")
                    append(parent.toString())
                }
            }
        }
    }
}

fun ULTPluginImpl.success(): ULTPluginImplStatusChain {
    return ULTPluginImplStatusChain(this, true)
}

fun ULTPluginImpl.failed(cause: Throwable?, message: String? = cause?.message): ULTPluginImplStatusChain {
    return ULTPluginImplStatusChain(this, false, cause, message)
}

fun ULTPluginImpl.failed(message: String? = null): ULTPluginImplStatusChain {
    return ULTPluginImplStatusChain(this, false, null, message)
}

/**
 * 传递,一般只传递错误链,因为成功链传递无意义
 *
 * 出错:由一个依赖出错导致的整个依赖链无法进行,由最上层的依赖向下传递,直到根需求
 *
 * 成功:向下传递成功
 */
fun ULTPluginImpl.transfer(status: ULTPluginImplStatusChain): ULTPluginImplStatusChain {
    return ULTPluginImplStatusChain(this, status.status, null, null, status)
}