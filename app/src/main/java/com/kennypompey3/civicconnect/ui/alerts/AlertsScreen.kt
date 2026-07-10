package com.kennypompey3.civicconnect.ui.alerts

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kennypompey3.civicconnect.R
import com.kennypompey3.civicconnect.ui.components.CivicTopHeader

// Keep your base data models intact at the top of the file
enum class AlertSeverity { SEVERE, IN_PROGRESS, RESOLVED }

data class AlertItem(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val severity: AlertSeverity
)

@Composable
fun AlertsScreen(
    // 🎯 STEP 3 INTEGRATION: Feed your ViewModel straight into the screen contract
    viewModel: AlertsViewModel = viewModel()
) {
    // Collect the dynamic UI state stream natively
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        bottomBar = {}
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(scaffoldPadding) // Preserves the exact same spatial insets as your HomeScreen
        ) {

            // Anchored title axis matches the precise pixel coordinates of your Home tab
            CivicTopHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            )

            // Viewport container managing asynchronous layout content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    // Modern production-grade feedback loop while network requests resolve
                    CircularProgressIndicator(
                        color = Color(0xFF1B263B)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Severe Section (Now filtering dynamically off your live state stream)
                        val severeAlerts = uiState.alerts.filter { it.severity == AlertSeverity.SEVERE }
                        if (severeAlerts.isNotEmpty()) {
                            item { AlertSectionHeader(title = "SEVERE  •  LOCAL ALERTS", color = Color(0xFFF24822)) }
                            items(severeAlerts, key = { it.id }) { alert -> AlertCard(item = alert) }
                        }

                        // Progress Section
                        val progressAlerts = uiState.alerts.filter { it.severity == AlertSeverity.IN_PROGRESS }
                        if (progressAlerts.isNotEmpty()) {
                            item { AlertSectionHeader(title = "WORK-IN-PROGRESS", color = Color(0xFF778DA9)) }
                            items(progressAlerts, key = { it.id }) { alert -> AlertCard(item = alert) }
                        }

                        // Resolved Section
                        val resolvedAlerts = uiState.alerts.filter { it.severity == AlertSeverity.RESOLVED }
                        if (resolvedAlerts.isNotEmpty()) {
                            item { AlertSectionHeader(title = "ISSUES RESOLVED", color = Color(0xFF778DA9)) }
                            items(resolvedAlerts, key = { it.id }) { alert -> AlertCard(item = alert) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertSectionHeader(title: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (color == Color(0xFFF24822)) {
            Icon(imageVector = Icons.Outlined.WarningAmber, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun AlertCard(item: AlertItem) {
    val (themeColor, statusIcon) = when (item.severity) {
        AlertSeverity.SEVERE -> Color(0xFFF24822) to Icons.Outlined.WarningAmber
        AlertSeverity.IN_PROGRESS -> Color(0xFFA09600) to Icons.Outlined.PendingActions
        AlertSeverity.RESOLVED -> Color(0xFF50BB6E) to Icons.Outlined.CheckCircle
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(10.dp).fillMaxHeight().heightIn(min = 84.dp).background(themeColor))
        Spacer(modifier = Modifier.width(14.dp))
        Box(modifier = Modifier.size(38.dp).background(themeColor.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(imageVector = statusIcon, contentDescription = null, tint = themeColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f).padding(vertical = 12.dp, horizontal = 4.dp)) {
            Text(text = item.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B2A))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = item.description, fontSize = 11.sp, color = Color(0xFF415A77), lineHeight = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = item.timestamp, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF778DA9))
        }
        Spacer(modifier = Modifier.width(12.dp))
    }
}