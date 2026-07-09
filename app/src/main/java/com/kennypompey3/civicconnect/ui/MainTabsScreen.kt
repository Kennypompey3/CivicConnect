package com.kennypompey3.civicconnect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.kennypompey3.civicconnect.R
import com.kennypompey3.civicconnect.ui.components.FloatingPillBottomNavBar
import com.kennypompey3.civicconnect.ui.components.PillNavItem
import com.kennypompey3.civicconnect.ui.home.HomeScreen
import com.kennypompey3.civicconnect.ui.location.LocationScreen
import com.kennypompey3.civicconnect.ui.profile.ProfileScreen
import com.kennypompey3.civicconnect.ui.report.ReportWizardScreen
import com.kennypompey3.civicconnect.ui.alerts.AlertsScreen
import com.google.android.gms.maps.model.LatLng

@Composable
fun MainTabsScreen(windowSizeClass: WindowSizeClass) {
    var selectedTab by remember { mutableIntStateOf(1) }
    var showReportWizard by remember { mutableStateOf(false) }

    // Temporary holding variable to safely pass map coordinates between screens
    var reportLocationTarget by remember { mutableStateOf<LatLng?>(null) }

    val hapticFeedback = LocalHapticFeedback.current

    val tabs = remember {
        listOf(
            PillNavItem("Home", R.drawable.ic_home),
            PillNavItem("Location", R.drawable.ic_location),
            PillNavItem("Alerts", R.drawable.ic_notifications),
            PillNavItem("Profile", R.drawable.ic_profile),
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        // --- LAYER 1: BASE PAGES ---
        when (selectedTab) {
            0 -> HomeScreen(windowSizeClass = windowSizeClass, showBottomNav = false)
            1 -> LocationScreen(
                windowSizeClass = windowSizeClass,
                showBottomNav = false,
                onStartReportCreation = { coordinates ->
                    reportLocationTarget = coordinates
                    showReportWizard = true
                }
            )
            2 -> AlertsScreen(userName = "Sarah")
            3 -> ProfileScreen(windowSizeClass = windowSizeClass)
        }

        // --- LAYER 2: THE WIZARD OVERLAY ---
        if (showReportWizard) {
            ReportWizardScreen(
                initialLocation = reportLocationTarget,
                onDismissWizard = {
                    showReportWizard = false
                    reportLocationTarget = null
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // --- LAYER 3: FLOATING NAVBAR FADE BACKGROUND SHIELD ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFF8FAFC).copy(alpha = 0.45f),
                            Color(0xFFF8FAFC).copy(alpha = 0.8f),
                            Color(0xFFF8FAFC)
                        )
                    )
                )
        )

        // --- LAYER 4: FLOATING NAVBAR NAVIGATION PILL ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 30.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingPillBottomNavBar(
                items = tabs,
                selectedIndex = selectedTab,
                onSelect = { index ->
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedTab = index
                    showReportWizard = false
                    reportLocationTarget = null
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$title Screen", color = Color.Black)
    }
}