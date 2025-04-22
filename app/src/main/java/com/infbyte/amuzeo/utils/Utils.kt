package com.infbyte.amuzeo.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

fun String.getInitialChar(): String {
    return first { it.isLetterOrDigit() }.uppercase()
}

fun <T> List<T>.getSubListIfNotEmpty(
    start: Int,
    end: Int,
): List<T> {
    if (start > size || end > size) return emptyList()
    return if (isNotEmpty()) subList(start, end) else emptyList()
}

@Composable
fun Int.toDp(): Dp {
    return with(LocalDensity.current) {
        this@toDp.toDp()
    }
}
