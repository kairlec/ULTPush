package com.kairlec.ultpush.bind

import com.google.inject.TypeLiteral

interface TypeLiteralAble {
    val typeLiteral: TypeLiteral<out Any>?
}