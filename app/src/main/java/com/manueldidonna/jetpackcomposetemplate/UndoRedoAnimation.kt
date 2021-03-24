package com.manueldidonna.jetpackcomposetemplate

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Redo
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun UndoRedoAnimation() {
    Box(
        modifier = Modifier
            .requiredSize(height = 56.dp, width = 200.dp)
            .background(
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.28f),
                shape = CircleShape
            )
            .clip(CircleShape)
    ) {
        val scope = rememberCoroutineScope()

        val undoIconRipple = remember { Ripple() }
        val redoIconRipple = remember { Ripple() }

        var isUndoEnabled by remember { mutableStateOf(true) }
        var isRedoEnabled by remember { mutableStateOf(true) }

        UndoIcon(ripple = undoIconRipple, enabled = isUndoEnabled)
        RedoIcon(ripple = redoIconRipple, enabled = isRedoEnabled)

        val density = LocalDensity.current
        val dragMaxDistancePx = remember { with(density) { 200.dp.toPx() - 56.dp.toPx() } }
        val offsetPosition = remember { Animatable(initialValue = dragMaxDistancePx / 2) }
        val undoIconPosition = remember { with(density) { 16.dp.toPx() + 12.dp.toPx() } }
        val redoIconPosition = remember { with(density) { dragMaxDistancePx - 12.dp.toPx() } }

        Clock(
            modifier = Modifier
                .zIndex(8f)
                .size(56.dp)
                .offset { IntOffset(x = offsetPosition.value.roundToInt(), 0) }
                .draggable(
                    state = rememberDraggableState { deltaPx ->
                        val newValue = offsetPosition.value + deltaPx
                        scope.launch {
                            offsetPosition.snapTo(newValue.coerceIn(0f, dragMaxDistancePx))
                        }
                    },
                    orientation = Orientation.Horizontal,
                    startDragImmediately = true,
                    onDragStopped = {
                        val currentDragPosition = offsetPosition.value

                        if (currentDragPosition < undoIconPosition && isUndoEnabled) {
                            isRedoEnabled = true
                            isUndoEnabled = false
                            launch { undoIconRipple.animate() }
                        } else if (currentDragPosition > redoIconPosition && isRedoEnabled) {
                            isRedoEnabled = false
                            isUndoEnabled = true
                            launch { redoIconRipple.animate() }
                        }

                        launch {
                            offsetPosition.animateTo(
                                targetValue = dragMaxDistancePx / 2,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                ),
            progress = offsetPosition.value / (dragMaxDistancePx / 2)
        )
    }
}

@Composable
private fun BoxScope.UndoIcon(ripple: Ripple, enabled: Boolean) {
    val iconAlpha by animateAlphaAsState(enabled)
    Icon(
        imageVector = Icons.Rounded.Undo,
        contentDescription = "undo icon",
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                clip = true
                alpha = iconAlpha
            }
    )

    val rippleColor = MaterialTheme.colors.onSurface
    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(fraction = 0.5f)
            .align(Alignment.CenterStart)
    ) {
        drawCircle(
            center = Offset(0f, size.height / 2f),
            color = rippleColor.copy(alpha = ripple.alpha),
            radius = ripple.radiusPercent * size.width
        )
    }
}

@Composable
private fun BoxScope.RedoIcon(ripple: Ripple, enabled: Boolean) {
    val iconAlpha by animateAlphaAsState(enabled)
    Icon(
        imageVector = Icons.Rounded.Redo,
        contentDescription = "redo icon",
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                clip = true
                alpha = iconAlpha
            }
    )

    val rippleColor = MaterialTheme.colors.onSurface

    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(fraction = 0.5f)
            .align(Alignment.CenterEnd)
    ) {
        drawCircle(
            center = Offset(size.width, size.height / 2f),
            color = rippleColor.copy(alpha = ripple.alpha),
            radius = ripple.radiusPercent * size.width
        )
    }
}

@Composable
private fun animateAlphaAsState(enabled: Boolean): State<Float> {
    return animateFloatAsState(
        targetValue = if (enabled) ContentAlpha.high else ContentAlpha.disabled,
        animationSpec = tween(350, easing = FastOutSlowInEasing)
    )
}

@Stable
private class Ripple {
    private val animatedAlpha = Animatable(0f)
    private val animatedRadiusPercent = Animatable(0f)

    val alpha get() = animatedAlpha.value
    val radiusPercent get() = animatedRadiusPercent.value

    suspend fun animate() {
        coroutineScope {
            launch {
                animatedAlpha.animateTo(0.36f, tween(175, easing = LinearEasing))
                animatedAlpha.animateTo(0f, tween(275, easing = LinearEasing))
            }
            launch {
                animatedRadiusPercent.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
                animatedRadiusPercent.snapTo(0f)
            }
        }
    }
}

@Composable
private fun Clock(modifier: Modifier, progress: Float) {
    val handColor = MaterialTheme.colors.surface
    Box(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = CircleShape, clip = true)
            .background(color = MaterialTheme.colors.onSurface, shape = CircleShape)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            val radius = size.width / 2
            val angle1 = PI * (60 * progress) / 30 - PI / 2
            drawLine(
                color = handColor,
                start = center,
                end = center + Offset(
                    x = (cos(angle1) * radius).toFloat(),
                    y = (sin(angle1) * radius).toFloat()
                ),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            val angle2 = PI * (0.4 + 0.4 * progress) - PI / 2
            drawLine(
                color = handColor,
                start = center,
                end = center + Offset(
                    x = (cos(angle2) * radius * 0.6).toFloat(),
                    y = (sin(angle2) * radius * 0.6).toFloat()
                ),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
