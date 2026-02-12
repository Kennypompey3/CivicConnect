package com.example.civicconnect.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.civicconnect.R
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import com.example.civicconnect.ui.components.FloatingPillBottomNavBar
import com.example.civicconnect.ui.components.PillNavItem
import androidx.compose.ui.unit.IntSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import com.example.civicconnect.ui.theme.CivicConnectTheme

data class Issue(
    val id: Int,
    val title: String,
    val location: String,
    val time: String,
    val verification: String,
    val status: IssueStatus,
    val icon: Int
)

enum class IssueStatus(val displayName: String, val color: Color) {
    IN_PROGRESS("in-progress", Color(0xFF00BFFF)),
    PENDING("pending", Color(0xFFD3D3D3)),
    RESOLVED("resolved", Color(0xFF32CD32))
}

@Composable
fun HomeScreen(windowSizeClass: WindowSizeClass) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Use your existing icons from drawables
    val tabs = remember {
        listOf(
            PillNavItem("Home", R.drawable.ic_home),
            PillNavItem("Location", R.drawable.ic_location),
            PillNavItem("Alerts", R.drawable.ic_notifications),
            PillNavItem("Profile", R.drawable.ic_profile),
        )
    }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 50.dp, vertical = 10.dp),
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

    ) { paddingValues ->
        // ✅ Your existing UI stays exactly the same here:
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Header() }
            item { ImpactScoreCard() }
            item { NearbyIssuesHeader() }
            items(sampleIssues) { issue -> IssueItem(issue = issue) }
        }
    }
}


@Composable
fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "CivicConnect", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = "Good Morning, Sarah", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFC97B7B))
        )
    }
}

@Composable
fun ImpactScoreCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2A47))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Impact Score", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(text = "You've helped fix 12 issues!", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF32CD32)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "A+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF2E3B5B), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "3", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Pending", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF2E3B5B), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "8", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Resolved", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun NearbyIssuesHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Nearby Issues", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "View All", color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun IssueItem(issue: Issue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(issue.status.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = issue.icon),
                    contentDescription = issue.title,
                    tint = issue.status.color
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = issue.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = issue.location, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(text = "${issue.time}  •  ${issue.verification}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(
                text = issue.status.displayName,
                color = issue.status.color,
                modifier = Modifier
                    .background(issue.status.color.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

val sampleIssues = listOf(
    Issue(
        id = 1,
        title = "Pothole",
        location = "4th Avenue & Main",
        time = "2h ago",
        verification = "12 neighbors verified",
        status = IssueStatus.IN_PROGRESS,
        icon = R.drawable.ic_pothole
    ),
    Issue(
        id = 2,
        title = "Faulty Streetlight",
        location = "Central Park Entrance",
        time = "1d ago",
        verification = "34 neighbors verified",
        status = IssueStatus.PENDING,
        icon = R.drawable.ic_streetlight
    ),
    Issue(
        id = 3,
        title = "Unlawful Waste Dump",
        location = "Behind Market St",
        time = "1d ago",
        verification = "34 neighbors verified",
        status = IssueStatus.RESOLVED,
        icon = R.drawable.ic_waste
    )
)
