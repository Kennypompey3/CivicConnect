package com.example.civicconnect.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.civicconnect.R
import com.example.civicconnect.ui.components.CivicTopHeader
import com.example.civicconnect.ui.components.FloatingPillBottomNavBar
import com.example.civicconnect.ui.components.PillNavItem
import com.example.civicconnect.ui.theme.CivicConnectTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// -----------------------------
// Screen-local layout defaults
// (match HomeScreen alignment feel)
// -----------------------------
private object LocationLayoutDefaults {
    val HeaderHorizontalPadding = 18.dp
    val HeaderVerticalPadding = 14.dp
    val ContentHorizontalPadding = 18.dp
    val HeaderToMapSpacing = 18.dp

    // Overlay nav frame (same family as Home)
    val BottomNavHorizontalPadding = 30.dp
    val BottomNavVerticalPadding = 10.dp

    // Bottom fade / tail spacer to support overlay nav
    val OverlayFadeHeight = 120.dp
    val OverlayTailSpacer = 120.dp
}

private enum class LocationFilter(val label: String) {
    ALL("All Issues"),
    MINE("My Reports"),
    TRENDING("Trending")
}

@Composable
fun LocationScreen(
    windowSizeClass: WindowSizeClass,
    showBottomNav: Boolean = true
) {
    var selectedTab by remember { mutableIntStateOf(1) } // Location selected if standalone
    var selectedFilter by remember { mutableStateOf(LocationFilter.ALL) }

    val tabs = remember {
        listOf(
            PillNavItem("Home", R.drawable.ic_home),
            PillNavItem("Location", R.drawable.ic_location),
            PillNavItem("Alerts", R.drawable.ic_notifications),
            PillNavItem("Profile", R.drawable.ic_profile),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // -------------------------
        // CONTENT LAYER
        // -------------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .statusBarsPadding()
        ) {
            CivicTopHeader(
                userName = "Sarah",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = LocationLayoutDefaults.HeaderHorizontalPadding,
                        vertical = LocationLayoutDefaults.HeaderVerticalPadding
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = LocationLayoutDefaults.ContentHorizontalPadding)
            ) {
                Spacer(modifier = Modifier.height(LocationLayoutDefaults.HeaderToMapSpacing))

                LocationMapCardPhase2(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )

                // Tail space helps content breathe if nav overlays (or future bottom content appears)
                Spacer(modifier = Modifier.height(LocationLayoutDefaults.OverlayTailSpacer))
            }
        }

        // -------------------------
        // OPTIONAL: internal fade + nav overlay
        // (used only in standalone mode)
        // In MainTabsScreen, pass showBottomNav = false
        // -------------------------
        if (showBottomNav) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(LocationLayoutDefaults.OverlayFadeHeight)
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
                    .padding(
                        horizontal = LocationLayoutDefaults.BottomNavHorizontalPadding,
                        vertical = LocationLayoutDefaults.BottomNavVerticalPadding
                    ),
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

@SuppressLint("MissingPermission")
@Composable
private fun LocationMapCardPhase2(
    selectedFilter: LocationFilter,
    onFilterSelected: (LocationFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Fallback center (Lagos) if permission denied / no location yet
    val fallbackLatLng = remember { LatLng(6.5244, 3.3792) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasCenteredOnUser by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fallbackLatLng, 12f)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    // Ask once when entering screen
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Fetch last known location if permission granted
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        userLocation = LatLng(location.latitude, location.longitude)
                    }
                }
                .addOnFailureListener {
                    // silent fallback
                }
        }
    }

    // Center camera once when user location is available
    LaunchedEffect(userLocation) {
        val loc = userLocation
        if (loc != null && !hasCenteredOnUser) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(loc, 15f)
            hasCenteredOnUser = true
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE9EEF2))
    ) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
                compassEnabled = true,
                mapToolbarEnabled = false
            )
        ) {
            // Placeholder issue markers (Phase 2.5 can filter these)
            Marker(
                state = MarkerState(position = LatLng(6.5030, 3.3600)),
                title = "Pothole",
                snippet = "4th Avenue & Main"
            )
            Marker(
                state = MarkerState(position = LatLng(6.5150, 3.3950)),
                title = "Faulty Streetlight",
                snippet = "Central Park Entrance"
            )
            Marker(
                state = MarkerState(position = LatLng(6.4900, 3.3700)),
                title = "Waste Dump",
                snippet = "Behind Market St"
            )
        }

        // Filter chips overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FilterChipPill(
                label = LocationFilter.ALL.label,
                selected = selectedFilter == LocationFilter.ALL,
                onClick = { onFilterSelected(LocationFilter.ALL) }
            )
            FilterChipPill(
                label = LocationFilter.MINE.label,
                selected = selectedFilter == LocationFilter.MINE,
                onClick = { onFilterSelected(LocationFilter.MINE) }
            )
            FilterChipPill(
                label = LocationFilter.TRENDING.label,
                selected = selectedFilter == LocationFilter.TRENDING,
                onClick = { onFilterSelected(LocationFilter.TRENDING) }
            )
        }

        // Permission hint overlay
        if (!hasLocationPermission) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Enable location to center map on you",
                    color = Color(0xFF1F2937),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    // You can differentiate selected/unselected styling later
    val bg = if (selected) Color(0xFF4E739A) else Color(0xFF4E739A)
    val textColor = Color.White

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -----------------------------------------------------
// PREVIEW-SAFE MOCK VERSION (for Android Studio Preview)
// -----------------------------------------------------

@Composable
private fun LocationScreenPreviewContent() {
    var selectedFilter by remember { mutableStateOf(LocationFilter.ALL) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .statusBarsPadding()
        ) {
            CivicTopHeader(
                userName = "Sarah",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = LocationLayoutDefaults.HeaderHorizontalPadding,
                        vertical = LocationLayoutDefaults.HeaderVerticalPadding
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = LocationLayoutDefaults.ContentHorizontalPadding)
            ) {
                Spacer(modifier = Modifier.height(LocationLayoutDefaults.HeaderToMapSpacing))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE9EEF2))
                ) {
                    FakeMapBackground(modifier = Modifier.matchParentSize())

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChipPill(
                            label = LocationFilter.ALL.label,
                            selected = selectedFilter == LocationFilter.ALL,
                            onClick = { selectedFilter = LocationFilter.ALL }
                        )
                        FilterChipPill(
                            label = LocationFilter.MINE.label,
                            selected = selectedFilter == LocationFilter.MINE,
                            onClick = { selectedFilter = LocationFilter.MINE }
                        )
                        FilterChipPill(
                            label = LocationFilter.TRENDING.label,
                            selected = selectedFilter == LocationFilter.TRENDING,
                            onClick = { selectedFilter = LocationFilter.TRENDING }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(LocationLayoutDefaults.OverlayTailSpacer))

            }
        }

        // Preview fade only (no nav in preview mock)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(LocationLayoutDefaults.OverlayFadeHeight)
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
    }
}

@Composable
private fun FakeMapBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFFEFF3F6))
    ) {
        // Water-ish zones
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFFD6EEF1))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 24.dp, y = (-24).dp)
                .width(120.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFCDEFF4))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-30).dp, y = (-42).dp)
                .width(90.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFCDEFF4))
        )

        // Roads
        FakeRoad(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 40.dp),
            widthFraction = 0.95f,
            height = 6.dp,
            color = Color(0xFFC5CCD3)
        )
        FakeRoad(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 48.dp),
            widthFraction = 0.95f,
            height = 2.dp,
            color = Color(0xFFB0BAC3)
        )
        FakeRoad(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 120.dp),
            widthFraction = 0.8f,
            height = 3.dp,
            color = Color(0xFFC9D1D8)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 90.dp, y = (-20).dp)
                .width(3.dp)
                .height(180.dp)
                .background(Color(0xFFD0D7DD))
        )

        // Tiny POIs
        repeat(6) { i ->
            Box(
                modifier = Modifier
                    .offset(
                        x = (32 + (i * 44)).dp,
                        y = (90 + ((i % 3) * 70)).dp
                    )
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (i % 2 == 0) Color(0xFFE573B7) else Color(0xFF7C8A97)
                    )
            )
        }

        // Mock center marker
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(30.dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6).copy(alpha = 0.18f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(14.dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6))
        )

        Text(
            text = "Google Maps",
            color = Color(0xFF5F6368),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
private fun FakeRoad(
    modifier: Modifier = Modifier,
    widthFraction: Float,
    height: Dp,
    color: Color
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .width(maxWidth * widthFraction)
                .height(height)
                .clip(RoundedCornerShape(999.dp))
                .background(color)
                .align(Alignment.Center)
        )
    }
}

// -----------------------------
// Preview (mock map, preview-safe)
// -----------------------------
@Preview(
    name = "Location Screen Mock Preview",
    showBackground = true,
    backgroundColor = 0xFFF8FAFC,
    widthDp = 412,
    heightDp = 915
)
@Composable
private fun LocationScreenPreviewMock() {
    CivicConnectTheme {
        LocationScreenPreviewContent()
    }
}