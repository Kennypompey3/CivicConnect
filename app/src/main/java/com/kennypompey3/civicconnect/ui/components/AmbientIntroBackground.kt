package com.kennypompey3.civicconnect.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun AmbientIntroBackground(
    content: @Composable BoxScope.() -> Unit
) {
    // 1. Setup the infinite rotation time-loop
    val infiniteTransition = rememberInfiniteTransition(label = "AmbientRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AngleDelta"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 2. Top Right Rotating Expressive Blob
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight(0.4f)
                .align(androidx.compose.ui.Alignment.TopEnd)
                .offset(x = 60.dp, y = (-40).dp)
                // 🚀 GPU Magic: Rotates the canvas layer without breaking layout tracking
                .graphicsLayer { rotationZ = rotationAngle }
        ) {
            val width = size.width
            val height = size.height
            val path = Path().apply {
                moveTo(width * 0.3f, 0f)
                cubicTo(width * 0.8f, height * 0.1f, width, height * 0.6f, width, height)
                cubicTo(width * 0.7f, height * 1.1f, width * 0.2f, height * 0.9f, 0f, height * 0.5f)
                cubicTo(0f, height * 0.2f, width * 0.1f, 0f, width * 0.3f, 0f)
                close()
            }
            drawPath(path = path, color = Color(0x26415A77)) // Soft alpha version of your brand slate blue
        }

        // 3. Bottom Left Rotating Expressive Blob
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .fillMaxHeight(0.4f)
                .align(androidx.compose.ui.Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp)
                // 🚀 Rotates in the exact same native rhythm
                .graphicsLayer { rotationZ = -rotationAngle }
        ) {
            val width = size.width
            val height = size.height
            val path = Path().apply {
                moveTo(0f, 0f)
                cubicTo(width * 0.6f, 0f, width, height * 0.3f, width * 0.8f, height * 0.8f)
                cubicTo(width * 0.5f, height, width * 0.1f, height * 0.9f, 0f, height * 0.4f)
                close()
            }
            drawPath(path = path, color = Color(0x1A778DA9)) // Subtle secondary accent tint
        }

        // 4. Floating Main Interface Layer
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}