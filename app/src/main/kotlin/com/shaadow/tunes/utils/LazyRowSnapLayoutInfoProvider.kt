package com.shaadow.tunes.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.unit.Density
import androidx.compose.ui.util.fastForEach
import kotlin.math.abs
import kotlin.math.sign

context(Density)
@ExperimentalFoundationApi
fun SnapLayoutInfoProvider(
    lazyListState: LazyListState,
    positionInLayout: Density.(layoutSize: Float, itemSize: Float) -> Float =
        { layoutSize, itemSize -> (layoutSize / 2f - itemSize / 2f) }
): SnapLayoutInfoProvider = object : SnapLayoutInfoProvider {

    private val layoutInfo: LazyListLayoutInfo
        get() = lazyListState.layoutInfo

    override fun calculateApproachOffset(velocity: Float, decayOffset: Float): Float {
        val layoutInfo = layoutInfo
        val visibleItemsInfo = layoutInfo.visibleItemsInfo
        if (visibleItemsInfo.isEmpty()) return 0f

        val decelerationRate = 0.78f
        val velocityThreshold = 0.5f
        val snapThreshold = 0.5f

        val averageItemSize = visibleItemsInfo.fastFold(0) { acc, item ->
            acc + item.size
        } / visibleItemsInfo.size.toFloat()

        val approachOffset = (velocity * decelerationRate).let {
            if (abs(it) <= velocityThreshold) {
                it
            } else {
                it.sign * (averageItemSize * snapThreshold)
            }
        }

        return approachOffset
    }

    override fun calculateSnapOffset(velocity: Float): Float {
        val layoutInfo = layoutInfo
        val visibleItemsInfo = layoutInfo.visibleItemsInfo
        if (visibleItemsInfo.isEmpty()) return 0f

        val layoutSize = layoutInfo.viewportSize.width.toFloat()
        val beforeContentPadding = layoutInfo.beforeContentPadding.toFloat()

        val itemsInfo = visibleItemsInfo.fastMap {
            SnapItem(
                index = it.index,
                offset = it.offset.toFloat(),
                size = it.size.toFloat()
            )
        }

        val centerPosition = layoutSize / 2f + beforeContentPadding

        val closestItem = itemsInfo.minByOrNull { item ->
            abs(item.offset + item.size / 2f - centerPosition)
        } ?: return 0f

        val targetOffset = positionInLayout(layoutSize, closestItem.size)
        return targetOffset - closestItem.offset
    }
}

private data class SnapItem(
    val index: Int,
    val offset: Float,
    val size: Float
)

private inline fun <T, R> List<T>.fastFold(initial: R, operation: (acc: R, element: T) -> R): R {
    var accumulator = initial
    fastForEach { element ->
        accumulator = operation(accumulator, element)
    }
    return accumulator
}

private inline fun <T, R> List<T>.fastMap(transform: (T) -> R): List<R> {
    val target = ArrayList<R>(size)
    fastForEach { item ->
        target += transform(item)
    }
    return target
}