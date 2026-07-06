package com.kennypompey3.civicconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
    userName: String = "Sarah",
    modifier: Modifier = Modifier,
    avatarContent: @Composable (() -> Unit)? = null
) {
    // Read from the persistent storage cache state loop
    val savedName by UserSessionManager.userName.collectAsState()
    val greetingPrefix = UserSessionManager.getGreetingPrefix()

    // Intercept default placeholders to reflect the actual user data cleanly
    val finalName = if (userName == "Sarah" && savedName.isNotBlank()) savedName else userName

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "CivicConnect",
                color = Color.Black,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$greetingPrefix, $finalName",
                color = Color(0xFF90A199),
                fontSize = 14.sp
            )
        }

        if (avatarContent != null) {
            avatarContent()
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD28A8A))
            )
        }
    }
}