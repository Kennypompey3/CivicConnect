package com.example.civicconnect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.example.civicconnect.R
import com.example.civicconnect.ui.components.FloatingPillBottomNavBar
import com.example.civicconnect.ui.components.PillNavItem
import com.example.civicconnect.ui.home.HomeScreen
import com.example.civicconnect.ui.location.LocationScreen
import com.example.civicconnect.ui.profile.ProfileScreen
// Make sure this import is here to access your new wizard!
import com.example.civicconnect.ui.report.ReportWizardScreen

@Composable
fun MainTabsScreen(windowSizeClass: WindowSizeClass) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // 1. The state variable that controls the wizard's visibility
    var showReportWizard by remember { mutableStateOf(false) }

    val tabs = remember {
        listOf(
            PillNavItem("Home", R.drawable.ic_home),
            PillNavItem("Location", R.drawable.ic_location),
            PillNavItem("Alerts", R.drawable.ic_notifications),
            PillNavItem("Profile", R.drawable.ic_profile),
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        // --- BASE LAYER: TABS ---
        when (selectedTab) {
            0 -> HomeScreen(windowSizeClass = windowSizeClass, showBottomNav = false)
            1 -> LocationScreen(
                windowSizeClass = windowSizeClass,
                showBottomNav = false,
                onStartReportCreation = { coordinates -> showReportWizard = true }
            )
            2 -> PlaceholderScreen(title = "Alerts")
            3 -> ProfileScreen(windowSizeClass = windowSizeClass)
        }

        // --- MIDDLE LAYER: THE WIZARD ---
        if (showReportWizard) {
            ReportWizardScreen(
                onDismissWizard = { showReportWizard = false },
                modifier = Modifier.fillMaxSize()
            )
        }

        // --- TOP LAYER: NAV BAR GRADIENT & PILL ---
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
                onSelect = { selectedTab = it },
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