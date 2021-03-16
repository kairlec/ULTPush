package com.kairlec.ultpush.core

import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

interface Filterable {
    val keyword: Set<String>

    val level: FilterLevel
}

class FilterLevel private constructor(
    val value: Int
) {
    companion object {
        val OFF = FilterLevel(100)
        val FATAL = FilterLevel(200)
        val ERROR = FilterLevel(300)
        val WARN = FilterLevel(400)
        val INFO = FilterLevel(500)
        val DEBUG = FilterLevel(600)
        val TRACE = FilterLevel(700)
        val ALL = FilterLevel(800)

        fun custom(value: Int): FilterLevel {
            return FilterLevel(value)
        }

        fun parse(value: Int): FilterLevel {
            var target: FilterLevel? = null
            this::class.declaredMemberProperties.any {
                val level = (it as KProperty1<Companion, FilterLevel>).get(this)
                if (level.value == value) {
                    target = level
                    true
                } else {
                    false
                }
            }
            return target ?: custom(value)
        }
    }
}