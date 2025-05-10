package com.infbyte.amuzeo.utils

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.infbyte.amuzeo.presentation.ui.views.FullBannerAdView

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

fun <T> LazyListScope.accommodateFullBannerAds(
    items: List<T>,
    bannerInitialPosition: Int = 9,
    showOnTopWithFewItems: Boolean = true,
    itemView: @Composable (T) -> Unit,
) {
    if (items.size > bannerInitialPosition) {
        val numberOfGroups = items.size / bannerInitialPosition
        var remainingItems = items

        for (group in 0..numberOfGroups) {
            items(remainingItems.take(bannerInitialPosition)) { item ->
                itemView(item)
            }
            if (remainingItems.isNotEmpty()) {
                if (remainingItems.size >= bannerInitialPosition) {
                    item {
                        FullBannerAdView()
                    }
                }
                remainingItems = remainingItems.drop(bannerInitialPosition)
            }
        }
        return
    }
    if (showOnTopWithFewItems) {
        item {
            FullBannerAdView()
        }
    }
    items(items) { item ->
        itemView(item)
    }
}

fun <T> LazyListScope.accommodateFullBannerAds(
    items: List<T>,
    bannerInitialPosition: Int = 9,
    showOnTopWithFewItems: Boolean = true,
    itemView: @Composable (Int, T) -> Unit,
) {
    if (items.size > bannerInitialPosition) {
        val numberOfGroups = items.size / bannerInitialPosition
        var remainingItems = items

        for (group in 0..numberOfGroups) {
            itemsIndexed(remainingItems.take(bannerInitialPosition)) { index, item ->
                itemView(index, item)
            }
            if (remainingItems.isNotEmpty()) {
                if (remainingItems.size >= bannerInitialPosition) {
                    item {
                        FullBannerAdView()
                    }
                }
                remainingItems = remainingItems.drop(bannerInitialPosition)
            }
        }
        return
    }
    if (showOnTopWithFewItems) {
        item {
            FullBannerAdView()
        }
    }
    itemsIndexed(items) { index, item ->
        itemView(index, item)
    }
}
