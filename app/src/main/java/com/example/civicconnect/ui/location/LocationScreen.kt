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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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

// Re-added the missing Enum definition right here!
enum class LocationFilter(val label: String) {
    ALL("All Issues"),
    MINE("My Reports"),
    TRENDING("Trending")
}

private object LocationLayoutDefaults {
    val HeaderHorizontalPadding = 18.dp
    val HeaderVerticalPadding = 14.dp
    val ContentHorizontalPadding = 18.dp
    val HeaderToMapSpacing = 18.dp
    val BottomNavHorizontalPadding = 30.dp
    val BottomNavVerticalPadding = 10.dp
    val OverlayFadeHeight = 120.dp
    val OverlayTailSpacer = 120.dp
}

@Composable
fun LocationScreen(
    windowSizeClass: WindowSizeClass,
    showBottomNav: Boolean = true
) {
    var selectedTab by remember { mutableIntStateOf(1) }
    var selectedFilter by remember { mutableStateOf(LocationFilter.ALL) }

    val tabs = remember {
        listOf(
            PillNavItem("Home", R.drawable.ic_home),
            PillNavItem("Location", R.drawable.ic_location),
            PillNavItem("Alerts", R.drawable.ic_notifications),
            PillNavItem("Profile", R.drawable.ic_profile),
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC)).statusBarsPadding()) {
            CivicTopHeader(
                userName = "Sarah",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = LocationLayoutDefaults.HeaderHorizontalPadding,
                        vertical = LocationLayoutDefaults.HeaderVerticalPadding
                    )
            )

            Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = LocationLayoutDefaults.ContentHorizontalPadding)) {
                Spacer(modifier = Modifier.height(LocationLayoutDefaults.HeaderToMapSpacing))

                LocationMapCardPhase2(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )

                Spacer(modifier = Modifier.height(LocationLayoutDefaults.OverlayTailSpacer))
            }
        }

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
    val fallbackLatLng = remember { LatLng(6.5244, 3.3792) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasCenteredOnUser by remember { mutableStateOf(false) }
    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(fallbackLatLng, 12f) }

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)
                }
            }
        }
    }

    LaunchedEffect(userLocation) {
        val loc = userLocation
        if (loc != null && !hasCenteredOnUser) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(loc, 15f)
            hasCenteredOnUser = true
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFFE9EEF2))) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false, compassEnabled = true, mapToolbarEnabled = false)
        ) {
            Marker(state = MarkerState(position = LatLng(6.5030, 3.3600)), title = "Pothole", snippet = "4th Avenue & Main")
            Marker(state = MarkerState(position = LatLng(6.5150, 3.3950)), title = "Faulty Streetlight", snippet = "Central Park Entrance")
            Marker(state = MarkerState(position = LatLng(6.4900, 3.3700)), title = "Waste Dump", snippet = "Behind Market St")
        }

        Row(modifier = Modifier.align(Alignment.TopStart).padding(horizontal = 12.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChipPill(label = LocationFilter.ALL.label, selected = selectedFilter == LocationFilter.ALL, onClick = { onFilterSelected(LocationFilter.ALL) })
            FilterChipPill(label = LocationFilter.MINE.label, selected = selectedFilter == LocationFilter.MINE, onClick = { onFilterSelected(LocationFilter.MINE) })
            FilterChipPill(label = LocationFilter.TRENDING.label, selected = selectedFilter == LocationFilter.TRENDING, onClick = { onFilterSelected(LocationFilter.TRENDING) })
        }

        if (!hasLocationPermission) {
            Box(modifier = Modifier.align(Alignment.Center).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.92f)).padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(text = "Enable location to center map on you", color = Color(0xFF1F2937), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun FilterChipPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Color(0xFF415A77) else Color(0xFF415A77)
    Text(
        text = label,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(999.dp))
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}

@Composable
private fun LocationScreenPreviewContent() {
    var selectedFilter by remember { mutableStateOf(LocationFilter.ALL) }
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC)).statusBarsPadding()) {
            CivicTopHeader(userName = "Sarah", modifier = Modifier.fillMaxWidth().padding(horizontal = LocationLayoutDefaults.HeaderHorizontalPadding, vertical = LocationLayoutDefaults.HeaderVerticalPadding))
            Column(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = LocationLayoutDefaults.ContentHorizontalPadding)) {
                Spacer(modifier = Modifier.height(LocationLayoutDefaults.HeaderToMapSpacing))
                Box(modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(20.dp)).background(Color(0xFFE9EEF2))) {
                    Box(modifier = Modifier.matchParentSize().background(Color(0xFFEFF3F6)))
                    Row(modifier = Modifier.align(Alignment.TopStart).padding(horizontal = 12.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChipPill(label = LocationFilter.ALL.label, selected = selectedFilter == LocationFilter.ALL, onClick = { selectedFilter = LocationFilter.ALL })
                        FilterChipPill(label = LocationFilter.MINE.label, selected = selectedFilter == LocationFilter.MINE, onClick = { selectedFilter = LocationFilter.MINE })
                        FilterChipPill(label = LocationFilter.TRENDING.label, selected = selectedFilter == LocationFilter.TRENDING, onClick = { selectedFilter = LocationFilter.TRENDING })
                    }
                }
                Spacer(modifier = Modifier.height(LocationLayoutDefaults.OverlayTailSpacer))
            }
        }
    }
}

@Preview(name = "Location Screen Mock Preview", showBackground = true, backgroundColor = 0xFFF8FAFC, widthDp = 412, heightDp = 915)
@Composable
private fun LocationScreenPreviewMock() {
    CivicConnectTheme {
        LocationScreenPreviewContent()
    }
}