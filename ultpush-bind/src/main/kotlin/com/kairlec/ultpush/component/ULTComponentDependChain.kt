package com.kairlec.ultpush.component

import java.io.ByteArrayOutputStream
import java.io.PrintWriter


class ULTComponentStatus(
    val component: ULTComponent,
    val status: Boolean,
    val exception: Throwable? = null,
    val message: String? = exception?.message,
    val parent: ULTComponentStatus? = null
) {
    override fun toString(): String {
        return if (status) {
            "[Success]"
        } else {
            buildString {
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

fun ULTComponent.success(): ULTComponentStatus {
    return ULTComponentStatus(this, true)
}

fun ULTComponent.failed(cause: Throwable?, message: String? = cause?.message): ULTComponentStatus {
    return ULTComponentStatus(this, false, cause, message)
}

fun ULTComponent.failed(message: String? = null): ULTComponentStatus {
    return ULTComponentStatus(this, false, null, message)
}

/**
 * 传递,一般只传递错误链,因为成功链传递无意义
 *
 * 出错:由一个依赖出错导致的整个依赖链无法进行,由最上层的依赖向下传递,直到根需求
 *
 * 成功:向下传递成功
 */
fun ULTComponent.transfer(status: ULTComponentStatus): ULTComponentStatus {
    return ULTComponentStatus(this, status.status, null, null, status)
}