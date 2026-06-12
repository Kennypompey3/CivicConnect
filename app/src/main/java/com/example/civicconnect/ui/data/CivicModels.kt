package com.example.civicconnect

import androidx.compose.ui.graphics.Color

// 1. The blueprint of what an issue is
data class Issue(
    val id: Int,
    val title: String,
    val location: String,
    val time: String,
    val verification: String,
    val status: IssueStatus,
    val icon: Int
)

// 2. The status states matching your UI design tags
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

// 3. Your placeholder datasets that your ViewModels will read from
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

val sampleIssuesExpanded = buildList {
    addAll(sampleIssues)
    add(
        Issue(
            id = 4,
            title = "Damaged Drainage",
            location = "6th Street Expressway",
            time = "3h ago",
            verification = "5 neighbors verified",
            status = IssueStatus.IN_PROGRESS,
            icon = R.drawable.ic_pothole
        )
    )
}