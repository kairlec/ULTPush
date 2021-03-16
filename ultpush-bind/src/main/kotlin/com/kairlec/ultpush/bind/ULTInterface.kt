package com.kairlec.ultpush.bind

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ULTInterface(val order: Int = Int.MAX_VALUE)