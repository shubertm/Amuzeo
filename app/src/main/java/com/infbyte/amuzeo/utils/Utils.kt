package com.infbyte.amuzeo.utils

fun String.getInitialChar(): String {
    return first { it.isLetterOrDigit() }.uppercase()
}

fun <T> List<T>.getSubListIfNotEmpty(
    start: Int,
    end: Int,
): List<T> {
    return if (isNotEmpty()) subList(start, end) else emptyList()
}
