package com.kennypompey3.civicconnect.ui.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class OnboardingSlide(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingCarouselScreen(onCarouselComplete: () -> Unit) {
    var showIntroScreen by remember { mutableStateOf(true) }

    // --- GPU-DRIVEN INFINITE TRANSITION TIMELINE ---
    val infiniteTransition = rememberInfiniteTransition(label = "ExpressiveBackground")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationDelta"
    )

    // 🚀 FIXED: Changed base canvas background plate to match your signature whitish shade (0xFFF8FAFC)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // --- MULTI-LOBED WAVY RIPPLE SHAPES LAYER (Light Theme Contrast Adjustments) ---

        // Top Right Wavy Ripple Cloud
        Canvas(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-60).dp)
                .graphicsLayer { rotationZ = rotationAngle }
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val path = Path().apply {
                val segments = 120
                for (i in 0..segments) {
                    val angleRad = (i.toFloat() / segments) * 2 * Math.PI
                    val waveFactor = kotlin.math.sin(angleRad * 6).toFloat() * 18f
                    val r = (size.width * 0.42f) + waveFactor
                    val x = cx + r * kotlin.math.cos(angleRad).toFloat()
                    val y = cy + r * kotlin.math.sin(angleRad).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            // 🚀 FIXED: Swapped dark fill opacity for a soft, subtle slate tint that moves cleanly over light backgrounds
            drawPath(path = path, color = Color(0xFF415A77).copy(alpha = 0.08f))
        }

        // Bottom Left Wavy Ripple Cloud
        Canvas(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-90).dp, y = 90.dp)
                .graphicsLayer { rotationZ = -rotationAngle }
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val path = Path().apply {
                val segments = 120
                for (i in 0..segments) {
                    val angleRad = (i.toFloat() / segments) * 2 * Math.PI
                    val waveFactor = kotlin.math.sin(angleRad * 5).toFloat() * 22f
                    val r = (size.width * 0.45f) + waveFactor
                    val x = cx + r * kotlin.math.cos(angleRad).toFloat()
                    val y = cy + r * kotlin.math.sin(angleRad).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(path = path, color = Color(0xFF778DA9).copy(alpha = 0.08f))
        }

        // --- INTERFACE CONTENT HUB (Crossfading Transition layers) ---
        Crossfade(
            targetState = showIntroScreen,
            animationSpec = tween(durationMillis = 600),
            label = "ScreenTransition"
        ) { isIntroActive ->
            if (isIntroActive) {
                OnboardingIntroContent(
                    onGetStartedClick = { showIntroScreen = false }
                )
            } else {
                val scope = rememberCoroutineScope()
                val slides = remember {
                    listOf(
                        OnboardingSlide(
                            title = "Pinpoint Local Issues",
                            description = "Long-press anywhere on your community map to drop an active reporting marker right at the structural problem spot.",
                            icon = Icons.Default.Map,
                            iconTint = Color(0xFF1B263B),
                            iconBackground = Color(0xFF778DA9).copy(alpha = 0.15f)
                        ),
                        OnboardingSlide(
                            title = "Snap & Describe",
                            description = "Provide immediate situational context by attaching real-time photos and custom descriptions so crews arrive fully prepared.",
                            icon = Icons.Default.AddAPhoto,
                            iconTint = Color(0xFF415A77),
                            iconBackground = Color(0xFF415A77).copy(alpha = 0.12f)
                        ),
                        OnboardingSlide(
                            title = "Track Live Progress",
                            description = "Stay informed across the entire lifecycle. Receive instant alerts the moment administrators dispatch contractors and resolve your log.",
                            icon = Icons.Default.NotificationsActive,
                            iconTint = Color(0xFF0D1B2A),
                            iconBackground = Color(0xFF0D1B2A).copy(alpha = 0.10f)
                        )
                    )
                }

                val pagerState = rememberPagerState(pageCount = { slides.size })
                val isLastPage = pagerState.currentPage == slides.size - 1

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        if (!isLastPage) {
                            Text(
                                text = "Skip",
                                color = Color(0xFF778DA9),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onCarouselComplete() }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { index ->
                        val slide = slides[index]
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(slide.iconBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = slide.icon,
                                    contentDescription = null,
                                    tint = slide.iconTint,
                                    modifier = Modifier.size(48.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            Text(
                                text = slide.title,
                                color = Color(0xFF0D1B2A), // 🚀 FIXED: Swapped light gray for crisp high-contrast Dark Navy
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = slide.description,
                                color = Color(0xFF415A77), // 🚀 FIXED: Updated to readable mid-tone Slate blue
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(slides.size) { i ->
                                val active = pagerState.currentPage == i
                                Box(
                                    modifier = Modifier
                                        .size(width = if (active) 22.dp else 8.dp, height = 8.dp)
                                        .clip(CircleShape)
                                        .background(if (active) Color(0xFF0D1B2A) else Color(0xFFE0E1DD)) // 🚀 FIXED: High contrast pagination track dots
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (isLastPage) {
                                    onCarouselComplete()
                                } else {
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF415A77))
                        ) {
                            Text(
                                text = if (isLastPage) "Get Started" else "Next",
                                color = Color.White, // 🚀 FIXED: White text on slate container meets strict contrast safety rules
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- INTRO WELCOME MODULE VIEW SCENE ---
@Composable
fun OnboardingIntroContent(onGetStartedClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Central Welcome Headings Stack
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Welcome to",
                fontSize = 32.sp,
                color = Color(0xFF415A77), // 🚀 FIXED: Inverted theme typography coloring profiles
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "CivicConnect",
                fontSize = 46.sp,
                color = Color(0xFF0D1B2A), // 🚀 FIXED: Strong brand visibility core identity text color anchor
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                letterSpacing = (-1).sp
            )
        }

        // Bottom Navigation Command Dock
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(999.dp), ambientColor = Color.Black.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF415A77), // 🚀 FIXED: Filled primary CTA container block button
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                )
            }

            // Utilities Toolbar Group
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* Language selection options workflow */ },
                    modifier = Modifier.background(Color(0xFFE0E1DD).copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language Selector",
                        tint = Color(0xFF0D1B2A)
                    )
                }

                IconButton(
                    onClick = { /* Core system configuration panels option toggles */ },
                    modifier = Modifier.background(Color(0xFFE0E1DD).copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "App Settings",
                        tint = Color(0xFF0D1B2A)
                    )
                }
            }
        }
    }
}