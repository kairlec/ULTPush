package com.kairlec.ultpush.bind

import com.google.inject.TypeLiteral

interface TypeStrict {
    val type: TypeLiteral<out Any>?
}