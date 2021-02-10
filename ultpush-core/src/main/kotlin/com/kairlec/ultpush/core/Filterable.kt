package com.kairlec.ultpush.core

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
    }
}