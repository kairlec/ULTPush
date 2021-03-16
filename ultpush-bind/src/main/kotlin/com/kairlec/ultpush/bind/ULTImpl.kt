package com.kairlec.ultpush.bind

import com.google.inject.Singleton

@Singleton
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ULTImpl(val name: String = "", val default: Boolean = true, val order: Int = Int.MAX_VALUE)
