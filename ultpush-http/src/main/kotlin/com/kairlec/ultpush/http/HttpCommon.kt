package com.kairlec.ultpush.http

/**
 * 双引号包括并转义字符串
 */
private fun String.quoteTo(out: StringBuilder) {
    out.append("\"")
    for (i in 0 until length) {
        when (val ch = this[i]) {
            '\\' -> out.append("\\\\")
            '\n' -> out.append("\\n")
            '\r' -> out.append("\\r")
            '\t' -> out.append("\\t")
            '\"' -> out.append("\\\"")
            else -> out.append(ch)
        }
    }
    out.append("\"")
}

private fun String.quote(): String = buildString { this@quote.quoteTo(this) }

@Suppress("NOTHING_TO_INLINE")
internal inline fun String.escapeIfNeededTo(out: StringBuilder) {
    when {
        checkNeedEscape() -> out.append(this.quote())
        else -> out.append(this)
    }
}

private fun String.checkNeedEscape(): Boolean {
    if (isEmpty()) return true
    if (isQuoted()) return false

    for (index in 0 until length) {
        if (HeaderFieldValueSeparators.contains(this[index])) return true
    }

    return false
}

/**
 * 检查是否被双引号包括
 */
private fun String.isQuoted(): Boolean {
    // 长度小于2肯定不足
    if (length < 2) {
        return false
    }
    // 第一个和最后的不为双引号就不是双引号包括
    if (first() != '"' || last() != '"') {
        return false
    }
    // 如果前后都是双引号,则找中间出现的双引号,看双引号前面是否是\(转义双引号)
    // 同时,要判断\是否被\转义,如果该双引号的前面的\是奇数个,则是有效转义的双引号
    // 否则双引号到该地为止,字符串断开,为非法字符串
    var startIndex = 1
    do {
        val index = indexOf('"', startIndex)
        if (index == lastIndex) {
            break
        }

        var slashesCount = 0
        var slashIndex = index - 1
        while (this[slashIndex] == '\\') {
            slashesCount++
            slashIndex--
        }
        if (slashesCount % 2 == 0) {
            return false
        }

        startIndex = index + 1
    } while (startIndex < length)

    return true
}

fun parseHeaderValue(text: String?): List<HeaderValue> {
    return parseHeaderValue(text, false)
}
fun parseHeaderValue(text: String?, parametersOnly: Boolean): List<HeaderValue> {
    if (text == null) {
        return emptyList()
    }

    var position = 0
    val items = lazy(LazyThreadSafetyMode.NONE) { arrayListOf<HeaderValue>() }
    while (position <= text.lastIndex) {
        position = parseHeaderValueItem(text, position, items, parametersOnly)
    }
    return items.valueOrEmpty()
}
private fun <T> Lazy<List<T>>.valueOrEmpty(): List<T> = if (isInitialized()) value else emptyList()

private fun String.subtrim(start: Int, end: Int): String {
    return substring(start, end).trim()
}
private fun parseHeaderValueItem(
    text: String,
    start: Int,
    items: Lazy<ArrayList<HeaderValue>>,
    parametersOnly: Boolean
): Int {
    var position = start
    val parameters = lazy(LazyThreadSafetyMode.NONE) { arrayListOf<HeaderValueParam>() }
    var valueEnd: Int? = if (parametersOnly) position else null

    while (position <= text.lastIndex) {
        when (text[position]) {
            ',' -> {
                items.value.add(HeaderValue(text.subtrim(start, valueEnd ?: position), parameters.valueOrEmpty()))
                return position + 1
            }
            ';' -> {
                if (valueEnd == null) valueEnd = position
                position = parseHeaderValueParameter(text, position + 1, parameters)
            }
            else -> {
                position = if (parametersOnly) {
                    parseHeaderValueParameter(text, position, parameters)
                } else {
                    position + 1
                }
            }
        }
    }

    items.value.add(HeaderValue(text.subtrim(start, valueEnd ?: position), parameters.valueOrEmpty()))
    return position
}


private fun parseHeaderValueParameter(text: String, start: Int, parameters: Lazy<ArrayList<HeaderValueParam>>): Int {
    fun addParam(text: String, start: Int, end: Int, value: String) {
        val name = text.subtrim(start, end)
        if (name.isEmpty()) {
            return
        }

        parameters.value.add(HeaderValueParam(name, value))
    }

    var position = start
    while (position <= text.lastIndex) {
        when (text[position]) {
            '=' -> {
                val (paramEnd, paramValue) = parseHeaderValueParameterValue(text, position + 1)
                addParam(text, start, position, paramValue)
                return paramEnd
            }
            ';', ',' -> {
                addParam(text, start, position, "")
                return position
            }
            else -> position++
        }
    }

    addParam(text, start, position, "")
    return position
}

private fun parseHeaderValueParameterValue(value: String, start: Int): Pair<Int, String> {
    if (value.length == start) {
        return start to ""
    }

    var position = start
    if (value[start] == '"') {
        return parseHeaderValueParameterValueQuoted(value, position + 1)
    }

    while (position <= value.lastIndex) {
        when (value[position]) {
            ';', ',' -> return position to value.subtrim(start, position)
            else -> position++
        }
    }
    return position to value.subtrim(start, position)
}

private fun parseHeaderValueParameterValueQuoted(value: String, start: Int): Pair<Int, String> {
    var position = start
    val builder = StringBuilder()
    loop@ while (position <= value.lastIndex) {
        val currentChar = value[position]

        when {
            currentChar == '"' && value.nextIsSemicolonOrEnd(position) -> {
                return position + 1 to builder.toString()
            }
            currentChar == '\\' && position < value.lastIndex - 2 -> {
                builder.append(value[position + 1])
                position += 2
                continue@loop
            }
        }

        builder.append(currentChar)
        position++
    }

    // The value is unquoted here
    return position to '"' + builder.toString()
}

private fun String.nextIsSemicolonOrEnd(start: Int): Boolean {
    var position = start + 1
    loop@ while (position < length && get(position) == ' ') {
        position += 1
    }

    return position == length || get(position) == ';'
}
