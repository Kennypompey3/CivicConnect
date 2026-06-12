package com.example.civicconnect.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import kotlinx.coroutines.launch
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.*

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
    showBottomNav: Boolean = true,
    onStartReportCreation: (LatLng) -> Unit = {} // Event handler linking to our wizard setup
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
                    onReportConfirmed = onStartReportCreation,
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
    onReportConfirmed: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 1. Grab our coroutine scope handler to manage background thread frame execution
    val coroutineScope = rememberCoroutineScope()
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val defaultLagos = remember { LatLng(6.5244, 3.3792) }

    var droppedPinCoordinates by remember { mutableStateOf<LatLng?>(null) }
    var isPopupVisible by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isCameraAnchoredToUser by remember { mutableStateOf(true) }

    var hasLocationPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLagos, 12f)
    }

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
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    userLocation = currentLatLng
                    if (isCameraAnchoredToUser) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 16f)
                    }
                }
            }
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            isCameraAnchoredToUser = false
        }
    }

    Box(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(Color(0xFFE9EEF2))) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false, compassEnabled = true, mapToolbarEnabled = false),
            onMapLongClick = { coordinates ->
                droppedPinCoordinates = coordinates
                isPopupVisible = true
            }
        ) {
            droppedPinCoordinates?.let { pinLocation ->
                Marker(state = MarkerState(position = pinLocation), title = "Selected Location")
            }
        }

        Row(modifier = Modifier.align(Alignment.TopStart).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChipPill(label = LocationFilter.ALL.label, selected = selectedFilter == LocationFilter.ALL, onClick = { onFilterSelected(LocationFilter.ALL) })
            FilterChipPill(label = LocationFilter.MINE.label, selected = selectedFilter == LocationFilter.MINE, onClick = { onFilterSelected(LocationFilter.MINE) })
            FilterChipPill(label = LocationFilter.TRENDING.label, selected = selectedFilter == LocationFilter.TRENDING, onClick = { onFilterSelected(LocationFilter.TRENDING) })
        }

        // Custom Floating Recenter Button in the Bottom-Left corner
        if (!isCameraAnchoredToUser && !isPopupVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 24.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        userLocation?.let { currentGPS ->
                            // 2. Open an asynchronous coroutine lane to slide frames smoothly over time
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(currentGPS, 16f),
                                    durationMs = 800 // Smooth 800ms glide duration curve matching premium map behaviors
                                )
                                // Re-anchor user tracking once the camera arrives safely at the destination
                                isCameraAnchoredToUser = true
                            }
                        }
                    }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Recenter Map to Me",
                    tint = Color(0xFF1B263B),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isPopupVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { h -> h }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { h -> h }) + fadeOut()
        ) {
            CardModalPopup(
                onCancelClick = {
                    isPopupVisible = false
                    droppedPinCoordinates = null
                },
                onStartClick = {
                    isPopupVisible = false
                    droppedPinCoordinates?.let { location -> onReportConfirmed(location) }
                }
            )
        }
    }
}

// ---------------------------------------------------------------------
// Popup Card Blueprint Layout Widget
// ---------------------------------------------------------------------
@Composable
private fun CardModalPopup(
    onCancelClick: () -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp) // Relaxed outer boundary margins
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1B263B))
            .padding(horizontal = 16.dp, vertical = 20.dp), // Optimized inner container walls
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Pin Location Drop",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Report Issue Here?",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Create a new report at this location",
            color = Color(0xFF90A199),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp) // Crisp, unified gap between choices
        ) {
            // Cancel Action Button
            Button(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(999.dp),
                contentPadding = PaddingValues(horizontal = 4.dp), // Clears text suffocation
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC9CBD6))
            ) {
                Text(
                    text = "Cancel",
                    color = Color(0xFF1B263B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
            }

            // Confirm / Start Action Button
            Button(
                onClick = onStartClick,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(999.dp),
                contentPadding = PaddingValues(horizontal = 4.dp), // Gives the phrase room to breathe
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF415A77))
            ) {
                Text(
                    text = "Start Report",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
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