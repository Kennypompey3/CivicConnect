package com.kennypompey3.civicconnect.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kennypompey3.civicconnect.R

@Composable
fun ProfileScreen(
    windowSizeClass: WindowSizeClass? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        ProfileHeaderSection()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Settings & Preferences",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SettingsSection(modifier = Modifier.padding(horizontal = 24.dp))
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "Version 1.0.1",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 120.dp) // Leave space for bottom nav
        )
    }
}

@Composable
fun ProfileHeaderSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Color(0xFFE5E5E0))
            .padding(top = 48.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier
                    .size(28.dp)
                    .clickable { /* Handle Settings click */ }
            )
        }
        
        // Avatar Placeholder (using a generic background as we don't have the image)
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            // Using a text as placeholder if we don't have the DevFest image
            Text("SJ", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Sarah Johnson",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Civic Champion  •  Level 3",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = "12", label = "Reports")
            StatItem(value = "156", label = "Votes")
            StatItem(value = "Top 5%", label = "Votes", valueColor = Color(0xFF4CAF50))
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    valueColor: Color = Color.Black
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun SettingsSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1E293B))
            .padding(vertical = 16.dp)
    ) {
        SettingsMenuItem(
            icon = Icons.Default.PersonOutline,
            text = "Account Details",
            showChevron = true
        )
        
        SettingsMenuItem(
            icon = Icons.Default.NotificationsNone,
            text = "Notifications",
            badgeText = "3 New",
            badgeColor = Color(0xFFF44336)
        )
        
        SettingsMenuItem(
            icon = Icons.Default.ExitToApp,
            text = "Log Out",
            textColor = Color(0xFFF44336),
            iconColor = Color(0xFFF44336)
        )
    }
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    text: String,
    textColor: Color = Color.White,
    iconColor: Color = Color.White,
    showChevron: Boolean = false,
    badgeText: String? = null,
    badgeColor: Color = Color.Transparent,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        
        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        
        if (showChevron) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreen()
    }
}
