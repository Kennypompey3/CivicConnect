package com.example.civicconnect.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.windowsizeclass.WindowSizeClass as M3WindowSizeClass
import com.example.civicconnect.R
import com.example.civicconnect.ui.components.FloatingPillBottomNavBar
import com.example.civicconnect.ui.components.PillNavItem
import com.example.civicconnect.ui.theme.CivicConnectTheme

// ----------------------
// Models (same idea as yours, dynamic-ready)
// ----------------------

data class Issue(
    val id: Int,
    val title: String,
    val location: String,
    val time: String,
    val verification: String,
    val status: IssueStatus,
    val icon: Int
)

enum class IssueStatus(
    val displayName: String,
    val textColor: Color,
    val pillBgColor: Color,
    val iconBgColor: Color
) {
    IN_PROGRESS(
        "In-Progress",
        textColor = Color(0xFF3113F4),
        pillBgColor = Color(0xFF65D6FF),
        iconBgColor = Color(0xFFECE77A)
    ),
    PENDING(
        "Pending",
        textColor = Color(0xFF322F35),
        pillBgColor = Color(0xFFCEC8D4),
        iconBgColor = Color(0xFFBDF0A5)
    ),
    RESOLVED(
        "Resolved",
        textColor = Color(0xFF13521B),
        pillBgColor = Color(0xFFC4FFB0),
        iconBgColor = Color(0xFFF3D8DB)
    )
}

// ----------------------
// Home Screen (drop-in)
// ----------------------

@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    showBottomNav: Boolean = true
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isExpanded by remember { mutableStateOf(false) }

    val tabs = remember {
        listOf(
            PillNavItem("Home", R.drawable.ic_home),
            PillNavItem("Location", R.drawable.ic_location),
            PillNavItem("Alerts", R.drawable.ic_notifications),
            PillNavItem("Profile", R.drawable.ic_profile),
        )
    }

    val visibleIssues = remember(isExpanded) {
        if (isExpanded) sampleIssuesExpanded else sampleIssues
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // CONTENT LAYER
        Scaffold(
            containerColor = Color(0xFFF8FAFC),
            bottomBar = {} // ✅ no bottomBar here; nav is overlaid separately
        ) { scaffoldPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(scaffoldPadding)
            ) {
                // ✅ STATIC HEADER (never scrolls)
                HomeHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                )

                // ✅ BODY (scroll only when expanded)
                if (isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                    ) {
                        HomeBody(
                            isExpanded = true,
                            issues = visibleIssues,
                            onToggleExpanded = { isExpanded = false }
                        )

                        // ✅ Tail space so the LAST card can scroll past the overlay nav comfortably
                        // This DOES NOT create overlay. Overlay comes from layering.
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                    ) {
                        HomeBody(
                            isExpanded = false,
                            issues = visibleIssues.take(3),
                            onToggleExpanded = { isExpanded = true }
                        )
                    }
                }
            }
        }
        if (showBottomNav) {
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

            // NAV OVERLAY LAYER (Google Photos-style)
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
}

// ----------------------
// Header (fixed)
// ----------------------

@Composable
private fun HomeHeader(modifier: Modifier = Modifier) {
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
                text = "Good Morning, Sarah",
                color = Color(0xFF90A199),
                fontSize = 14.sp
            )
        }

        // Placeholder avatar (replace with Coil/AsyncImage later)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFD28A8A))
        )
    }
}

// ----------------------
// Body (score + nearby issues + toggle)
// ----------------------

@Composable
private fun HomeBody(
    isExpanded: Boolean,
    issues: List<Issue>,
    onToggleExpanded: () -> Unit
) {
    Spacer(modifier = Modifier.height(8.dp))

    ImpactScoreCard()

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Nearby Issues",
            color = Color.Black,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (isExpanded) "Show Less" else "View All",
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier.clickable { onToggleExpanded() }
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        issues.forEach { issue ->
            IssueCard(issue = issue)
        }
    }

    if (!isExpanded) {
        // Small bottom spacing in collapsed mode
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ----------------------
// Impact Score Card
// ----------------------

@Composable
private fun ImpactScoreCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Impact Score",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You’ve helped fix 12 issues!",
                        color = Color(0xFF90A199),
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF415A77))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "A+",
                        color = Color(0xFF50BB6E),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    value = "3",
                    label = "Pending",
                    modifier = Modifier.weight(1f)
                )

                MetricTile(
                    value = "8",
                    label = "Resolved",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF415A77))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color(0xFF938F99),
            fontSize = 16.sp
        )
    }
}

// ----------------------
// Issue Card
// ----------------------

@Composable
private fun IssueCard(issue: Issue) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE0E1DD))
            .padding(horizontal = 10.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(issue.status.iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                painter = painterResource(id = issue.icon),
                contentDescription = issue.title,
                tint = issue.status.textColor
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = issue.title,
                color = Color.Black,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = issue.location,
                color = Color(0xFF49454F),
                fontSize = 11.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${issue.time}  •  ${issue.verification}",
                color = Color(0xFF2B2731),
                fontSize = 11.sp,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(52.dp))
                .background(issue.status.pillBgColor)
                .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Text(
                text = issue.status.displayName,
                color = issue.status.textColor,
                fontSize = 13.sp
            )
        }
    }
}

// ----------------------
// Sample data (dynamic-ready placeholders)
// ----------------------

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

// Expanded sample to simulate growing user submissions
val sampleIssuesExpanded = buildList {
    addAll(sampleIssues)
    add(
        Issue(
            id = 4,
            title = "Pothole",
            location = "4th Avenue & Main",
            time = "2h ago",
            verification = "12 neighbors verified",
            status = IssueStatus.IN_PROGRESS,
            icon = R.drawable.ic_pothole
        )
    )
    add(
        Issue(
            id = 5,
            title = "Faulty Streetlight",
            location = "Central Park Entrance",
            time = "1d ago",
            verification = "34 neighbors verified",
            status = IssueStatus.PENDING,
            icon = R.drawable.ic_streetlight
        )
    )
}

// ----------------------
// Preview
// ----------------------

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun HomeScreenPreview() {
    CivicConnectTheme {
        HomeScreen(
            windowSizeClass = M3WindowSizeClass.calculateFromSize(androidx.compose.ui.unit.DpSize(412.dp, 915.dp))
        )
    }
}