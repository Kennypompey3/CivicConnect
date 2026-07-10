package com.kennypompey3.civicconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kennypompey3.civicconnect.data.UserSessionManager

@Composable
fun CivicTopHeader(
    modifier: Modifier = Modifier,
    avatarContent: @Composable (() -> Unit)? = null
) {
    // 🚀 DYNAMIC DATA STREAM: Reads real cached session attributes natively without relying on manual placeholders
    val savedName by UserSessionManager.userName.collectAsState()
    val greetingPrefix = UserSessionManager.getGreetingPrefix()
    val finalName = savedName.ifBlank { "User" }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(), // Ensures the title never clips underneath system overlays
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "CivicConnect",
                color = Color(0xFF0D1B2A),
                fontSize = 26.sp, // Upgraded layout typography profile
                fontWeight = FontWeight.Black
            )
            Text(
                text = "$greetingPrefix, $finalName",
                color = Color(0xFF90A199), // Fixed slate-green theme signature color
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (avatarContent != null) {
            avatarContent()
        } else {
            // High-fidelity profile image slot matching your template coordinates exactly
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E1DD)),
                contentAlignment = Alignment.Center
            ) {
                // Fallback placeholder profile matrix
                Text(
                    text = finalName.take(1).uppercase(),
                    color = Color(0xFF415A77),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}