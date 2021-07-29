@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.kairlec.ultpush.http

import com.kairlec.ultpush.util.ReadOnlyEvent
import java.io.OutputStream

abstract class HttpOutputStream : OutputStream() {
    abstract val ready: Boolean
    abstract val writeEvent: ReadOnlyEvent<Any>

    @Deprecated("It is more recommended to handle Null values by yourself", ReplaceWith("print(\"null\")"))
    fun printNull() {
        print("null")
    }

    fun print(str: String) {
        str.forEach {
            if ((it.code and 0xff00) != 0) {
                throw IllegalArgumentException("'$it' is not ISO 8859-1 character")
            }
            write(it.code)
        }
    }

    fun print(boolean: Boolean) {
        print(if (boolean) "true" else "false")
    }

    fun print(char: Char) {
        write(char.code)
    }

    fun print(int: Int) {
        print(int.toString())
    }

    fun print(long: Long) {
        print(long.toString())
    }

    fun print(float: Float) {
        print(float.toString())
    }

    fun print(double: Double) {
        print(double.toString())
    }

    fun println() {
        print("\r\n")
    }

    @Deprecated("It is more recommended to handle Null values by yourself")
    fun printlnNull() {
        print("null")
        println()
    }

    fun println(str: String) {
        str.forEach {
            if ((it.code and 0xff00) != 0) {
                throw IllegalArgumentException("'$it' is not ISO 8859-1 character")
            }
            write(it.code)
        }
        println()
    }

    fun println(boolean: Boolean) {
        print(if (boolean) "true" else "false")
        println()
    }

    fun println(char: Char) {
        write(char.code)
        println()
    }

    fun println(int: Int) {
        print(int.toString())
        println()
    }

    fun println(long: Long) {
        print(long.toString())
        println()
    }

    fun println(float: Float) {
        print(float.toString())
        println()
    }

    fun println(double: Double) {
        print(double.toString())
        println()
    }

}