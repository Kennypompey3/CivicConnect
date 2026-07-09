package com.kennypompey3.civicconnect.ui.alerts

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import com.kennypompey3.civicconnect.R

enum class AlertSeverity { SEVERE, IN_PROGRESS, RESOLVED }

data class AlertItem(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val severity: AlertSeverity
)

@Composable
fun AlertsScreen(userName: String = "Sarah") {
    val alertsList = remember {
        mutableStateListOf(
            AlertItem("1", "Heavy Rain Alert", "Flash flood warning. Please report blocked drains immediately.", "3h ago", AlertSeverity.SEVERE),
            AlertItem("2", "Power Outage", "Major power outage reported in Sector 7.", "15m ago", AlertSeverity.SEVERE),
            AlertItem("3", "Crew Assigned", "Maintenance crew is heading to your reported broken light.", "1h ago", AlertSeverity.IN_PROGRESS),
            AlertItem("4", "Inspection Scheduled", "Sidewalk damage report is scheduled for inspection.", "1h ago", AlertSeverity.IN_PROGRESS),
            AlertItem("5", "Pothole Fixed", "The pothole on 4th Avenue has been fixed!", "1d ago", AlertSeverity.RESOLVED),
            AlertItem("6", "Trash Cleared", "Waste dump behind Market St has been cleared.", "2d ago", AlertSeverity.RESOLVED)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        // 110.dp bottom padding allows lists to clear your floating navigation bar shield nicely
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AlertsHeader(userName = userName)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Severe Section
        val severeAlerts = alertsList.filter { it.severity == AlertSeverity.SEVERE }
        if (severeAlerts.isNotEmpty()) {
            item { AlertSectionHeader(title = "SEVERE  •  LOCAL ALERTS", color = Color(0xFFF24822)) }
            items(severeAlerts) { alert -> AlertCard(item = alert) }
        }

        // Progress Section
        val progressAlerts = alertsList.filter { it.severity == AlertSeverity.IN_PROGRESS }
        if (progressAlerts.isNotEmpty()) {
            item { AlertSectionHeader(title = "WORK-INPROGRESS", color = Color(0xFF778DA9)) }
            items(progressAlerts) { alert -> AlertCard(item = alert) }
        }

        // Resolved Section
        val resolvedAlerts = alertsList.filter { it.severity == AlertSeverity.RESOLVED }
        if (resolvedAlerts.isNotEmpty()) {
            item { AlertSectionHeader(title = "ISSUES RESOLVED", color = Color(0xFF778DA9)) }
            items(resolvedAlerts) { alert -> AlertCard(item = alert) }
        }
    }
}

@Composable
private fun AlertsHeader(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "CivicConnect", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D1B2A))
            Text(text = "Good Morning, $userName", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF778DA9))
        }
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E1DD)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = Color(0xFF415A77), modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun AlertSectionHeader(title: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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