package com.kennypompey3.civicconnect.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

data class PillNavItem(
    val label: String,
    val iconRes: Int
)

private data class TabMetrics(
    val slotCenterX: Float = 0f,
    val contentWidthPx: Int = 0
)

@Composable
fun FloatingPillBottomNavBar(
    items: List<PillNavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 72.dp,

    // ✅ The "Tomato" push behavior lives here:
    selectedWeight: Float = 2.2f,
    unselectedWeight: Float = 1f,

    containerColor: Color = Color(0xFF2E2E2E),
    indicatorColor: Color = Color(0xFFC9CBD6),
    unselectedIconColor: Color = Color(0xFFC9CBD6),
    selectedContentColor: Color = Color(0xFF2E2E2E),
) {
    val density = LocalDensity.current
    val horizontalInset = 10.dp

    var contentAreaWidthPx by remember { mutableStateOf(0) }

    val metrics = remember(items.size) {
        mutableStateListOf<TabMetrics>().apply { repeat(items.size) { add(TabMetrics()) } }
    }

    // Horizontal-only breath
    val pulseX = remember { Animatable(1f) }
    LaunchedEffect(selectedIndex) {
        pulseX.snapTo(1f)
        pulseX.animateTo(
            targetValue = 1.03f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        pulseX.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Surface(
        modifier = modifier
            .height(height)
            .graphicsLayer { scaleX = pulseX.value; scaleY = 1f },
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        tonalElevation = 6.dp,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalInset)
                .onGloballyPositioned { contentAreaWidthPx = it.size.width }
        ) {
            // ----- Indicator -----
            val selected = metrics.getOrNull(selectedIndex)
            if (selected != null && selected.contentWidthPx > 0 && contentAreaWidthPx > 0) {

                val centerXAnim by animateFloatAsState(
                    targetValue = selected.slotCenterX,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "centerX"
                )

                // Give the pill a little extra width beyond content (feels nicer)
                val targetWPx = (selected.contentWidthPx + with(density) { 12.dp.toPx() }).toFloat()

                val wPxAnim by animateFloatAsState(
                    targetValue = targetWPx,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "indicatorW"
                )

                val leftX = (centerXAnim - (wPxAnim / 2f))
                    .coerceIn(
                        0f,
                        (contentAreaWidthPx.toFloat() - wPxAnim).coerceAtLeast(0f)
                    )

                Box(
                    modifier = Modifier
                        .zIndex(0f)
                        .offset { IntOffset(leftX.roundToInt(), 0) }
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                        .height(height - 20.dp)
                        .width(with(density) { wPxAnim.toDp() })
                        .clip(RoundedCornerShape(999.dp))
                        .background(indicatorColor)
                )
            }

            // ----- Tabs -----
            Row(
                modifier = Modifier
                    .zIndex(1f)
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    val w = if (isSelected) selectedWeight else unselectedWeight

                    // ✅ selected tab gets more width, pushing others
                    Box(
                        modifier = Modifier
                            .weight(w)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(999.dp))
                            .clickable { onSelect(index) }
                            .onGloballyPositioned { coords ->
                                val slotX = coords.positionInParent().x
                                val slotW = coords.size.width
                                val slotCenter = slotX + (slotW / 2f)

                                val old = metrics[index]
                                metrics[index] = old.copy(slotCenterX = slotCenter)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .onGloballyPositioned { coords ->
                                    val old = metrics[index]
                                    metrics[index] = old.copy(contentWidthPx = coords.size.width)
                                }
                                .animateContentSize(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                .padding(
                                    horizontal = if (isSelected) 18.dp else 12.dp,
                                    vertical = 12.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = item.iconRes),
                                contentDescription = item.label,
                                tint = if (isSelected) selectedContentColor else unselectedIconColor
                            )

                            AnimatedVisibility(visible = isSelected) {
                                Row(
                                    modifier = Modifier.padding(start = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.label,
                                        color = selectedContentColor,
                                        style = MaterialTheme.typography.labelLarge,
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
    }
}
