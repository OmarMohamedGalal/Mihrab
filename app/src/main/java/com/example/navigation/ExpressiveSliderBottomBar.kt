package com.example.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class SliderNavItem(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

/**
 * Material 3 Expressive floating slider-style bottom navigation bar.
 * Features a pill track with a spring-animated sliding capsule thumb that supports
 * both direct tapping and smooth horizontal dragging across destinations.
 */
@Composable
fun ExpressiveSliderBottomBar(
    items: List<SliderNavItem>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeIndex = selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
    val haptic = LocalHapticFeedback.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember(safeIndex) {
        mutableFloatStateOf(
            if (items.size <= 1) 0f else safeIndex.toFloat() / (items.size - 1)
        )
    }
    var lastHapticIndex by remember { mutableIntStateOf(safeIndex) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .widthIn(max = 540.dp)
                .fillMaxWidth()
                .height(68.dp)
                .testTag("expressive_slider_bottom_bar")
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
                    .pointerInput(items.size, isRtl) {
                        if (items.isEmpty()) return@pointerInput
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                val w = size.width.toFloat().coerceAtLeast(1f)
                                val raw = (offset.x / w).coerceIn(0f, 1f)
                                val visualRatio = if (isRtl) 1f - raw else raw
                                dragFraction = visualRatio
                                val idx = (visualRatio * (items.size - 1))
                                    .roundToInt()
                                    .coerceIn(0, items.lastIndex)
                                lastHapticIndex = idx
                            },
                            onHorizontalDrag = { change, _ ->
                                change.consume()
                                val w = size.width.toFloat().coerceAtLeast(1f)
                                val raw = (change.position.x / w).coerceIn(0f, 1f)
                                val visualRatio = if (isRtl) 1f - raw else raw
                                dragFraction = visualRatio
                                val hoveredIdx = (visualRatio * (items.size - 1))
                                    .roundToInt()
                                    .coerceIn(0, items.lastIndex)
                                if (hoveredIdx != lastHapticIndex) {
                                    lastHapticIndex = hoveredIdx
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectIndex(hoveredIdx)
                                }
                            },
                            onDragEnd = {
                                val finalIdx = (dragFraction * (items.size - 1))
                                    .roundToInt()
                                    .coerceIn(0, items.lastIndex)
                                isDragging = false
                                onSelectIndex(finalIdx)
                            },
                            onDragCancel = {
                                isDragging = false
                            }
                        )
                    }
            ) {
                val count = items.size.coerceAtLeast(1)
                val slotWidth = maxWidth / count
                val targetVisualIndex = if (isDragging) {
                    dragFraction * (count - 1)
                } else {
                    safeIndex.toFloat()
                }
                val thumbOffset by animateDpAsState(
                    targetValue = slotWidth * targetVisualIndex,
                    animationSpec = if (isDragging) {
                        spring(stiffness = Spring.StiffnessHigh)
                    } else {
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    },
                    label = "slider_thumb_offset"
                )

                // Sliding active pill thumb (M3 Expressive capsule)
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .offset(x = thumbOffset)
                        .width(slotWidth)
                        .fillMaxHeight()
                ) {}

                // Destination segments on top of the sliding pill track
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, item ->
                        val activeIndex = if (isDragging) {
                            (dragFraction * (count - 1)).roundToInt().coerceIn(0, items.lastIndex)
                        } else {
                            safeIndex
                        }
                        val selected = index == activeIndex

                        val contentColor by animateColorAsState(
                            targetValue = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            label = "slider_item_color"
                        )
                        val iconScale by animateFloatAsState(
                            targetValue = if (selected) 1.08f else 0.96f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "slider_item_scale"
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Tab,
                                    onClick = { onSelectIndex(index) }
                                )
                                .testTag(item.tag),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = contentColor,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(iconScale)
                            )
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = contentColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
