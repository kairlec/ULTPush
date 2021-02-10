package com.kairlec.ultpush.bind

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ULTImpl(val name: String = "", val default: Boolean = true)
