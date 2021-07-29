package com.kairlec.ultpush.configuration

interface MutableConfig : Config {
    fun update(target: Config)
}