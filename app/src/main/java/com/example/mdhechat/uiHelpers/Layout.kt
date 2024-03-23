package com.example.mdhechat.uiHelpers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp


enum class Direction {
    ROW, COL
}

@Composable
fun <ItemType> Spaced(
    direction: Direction,
    gap: Dp,
    items: List<ItemType>,
    modifier: Modifier,
    itemRenderer: @Composable (ItemType) -> Unit
) {
    when (direction) {
        Direction.COL -> {
            Column(modifier = modifier) {
                items.forEach {
                    itemRenderer(it)
                    Spacer(modifier = Modifier.height(gap))
                }
            }

        }
        Direction.ROW -> {
            Row(modifier = modifier) {
                items.forEach {
                    itemRenderer(it)
                    Spacer(modifier = Modifier.width(gap))
                }
            }
        }
    }
}
