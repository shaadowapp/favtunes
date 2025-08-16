package com.shaadow.tunes.utils

import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.unit.Density
import androidx.compose.ui.util.fastForEach
import kotlin.math.abs

fun SnapLayoutInfoProvider(
    lazyListState: LazyListState,
    positionInLayout: (layoutSize: Float, itemSize: Float) -> Float
): SnapLayoutInfoProvider = object : SnapLayoutInfoProvider {
    override fun calculateApproachOffset(velocity: Float, decayOffset: Float): Float = 0f

    override fun calculateSnapOffset(velocity: Float): Float {
        val layoutInfo = lazyListState.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty()) return 0f

        val layoutSize = layoutInfo.viewportSize.width.toFloat()
        val itemSize = visibleItems.first().size.toFloat()
        val targetPosition = positionInLayout(layoutSize, itemSize)

        var closestItem = visibleItems.first()
        var minDistance = abs(closestItem.offset - targetPosition)

        visibleItems.fastForEach { item ->
            val distance = abs(item.offset - targetPosition)
            if (distance < minDistance) {
                minDistance = distance
                closestItem = item
            }
        }

        return targetPosition - closestItem.offset
    }
}

fun SnapLayoutInfoProvider(
    lazyGridState: LazyGridState,
    positionInLayout: (layoutSize: Float, itemSize: Float) -> Float
): SnapLayoutInfoProvider = object : SnapLayoutInfoProvider {
    override fun calculateApproachOffset(velocity: Float, decayOffset: Float): Float = 0f

    override fun calculateSnapOffset(velocity: Float): Float {
        val layoutInfo = lazyGridState.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty()) return 0f

        val layoutSize = layoutInfo.viewportSize.width.toFloat()
        val itemSize = visibleItems.first().size.width.toFloat()
        val targetPosition = positionInLayout(layoutSize, itemSize)

        var closestItem = visibleItems.first()
        var minDistance = abs(closestItem.offset.x - targetPosition)

        visibleItems.fastForEach { item ->
            val distance = abs(item.offset.x - targetPosition)
            if (distance < minDistance) {
                minDistance = distance
                closestItem = item
            }
        }

        return targetPosition - closestItem.offset.x
    }
}