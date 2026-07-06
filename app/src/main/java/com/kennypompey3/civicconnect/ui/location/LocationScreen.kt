package com.kennypompey3.civicconnect.ui.location

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
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kennypompey3.civicconnect.R
import com.kennypompey3.civicconnect.ui.components.CivicTopHeader
import com.kennypompey3.civicconnect.ui.components.FloatingPillBottomNavBar
import com.kennypompey3.civicconnect.ui.components.PillNavItem
import com.kennypompey3.civicconnect.ui.theme.CivicConnectTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

enum class LocationFilter(val label: String) {
    ALL("All Issues"), MINE("My Reports"), TRENDING("Trending")
}

private object LocationLayoutDefaults {
    val HeaderHorizontalPadding = 18.dp
    val HeaderVerticalPadding = 14.dp
    val ContentHorizontalPadding = 18.dp
    val HeaderToMapSpacing = 18.dp
    val OverlayTailSpacer = 120.dp
}

// ✅ Restored: The main public interface screen that MainTabsScreen connects to directly
@Composable
fun LocationScreen(
    windowSizeClass: WindowSizeClass,
    showBottomNav: Boolean = true,
    onStartReportCreation: (LatLng) -> Unit = {} // The parameter the compiler was missing!
) {
    var selectedFilter by remember { mutableStateOf(LocationFilter.ALL) }

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
                onReportConfirmed = onStartReportCreation, // Forwards data directly down the chain
                modifier = Modifier.fillMaxWidth().weight(1f)
            )

            Spacer(modifier = Modifier.height(LocationLayoutDefaults.OverlayTailSpacer))
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

        // Smooth recerting FAB button anchor
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
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(currentGPS, 16f),
                                    durationMs = 800
                                )
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

@Composable
private fun CardModalPopup(onCancelClick: () -> Unit, onStartClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF1B263B)).padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Report Issue Here?", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Create a new report at this location", color = Color(0xFF90A199), fontSize = 13.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onCancelClick, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(999.dp), contentPadding = PaddingValues(horizontal = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC9CBD6))) {
                Text(text = "Cancel", color = Color(0xFF1B263B), fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            }
            Button(onClick = onStartClick, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(999.dp), contentPadding = PaddingValues(horizontal = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF415A77))) {
                Text(text = "Start Report", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun FilterChipPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Color(0xFF415A77) else Color(0xFF415A77).copy(alpha = 0.7f)
    Text(
        text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
        modifier = Modifier.shadow(elevation = 8.dp, shape = RoundedCornerShape(999.dp)).clip(RoundedCornerShape(999.dp)).background(bg).clickable { onClick() }.padding(horizontal = 14.dp, vertical = 8.dp)
    )
}