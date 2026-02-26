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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.civicconnect.R
import com.example.civicconnect.ui.components.CivicTopHeader
import com.example.civicconnect.ui.components.PillNavItem
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

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
    var selectedTab by remember { mutableIntStateOf(1) } // Location selected
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
        // CONTENT LAYER
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            CivicTopHeader(
                userName = "Sarah",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                LocationMapCardPhase2(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(120.dp))
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

    // Lagos-ish fallback (so map still works without permission)
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

    // Request permission on first composition if not granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Fetch last known location when permission becomes available
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        userLocation = LatLng(location.latitude, location.longitude)
                    }
                }
                .addOnFailureListener {
                    // silent fallback to default center
                }
        }
    }

    // Center map on user once
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
            .aspectRatio(0.86f)
    ) {
        GoogleMap(
            modifier = Modifier.matchParentSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false, // we can add our own recenter button later
                zoomControlsEnabled = false,
                compassEnabled = true
            )
        ) {
            // Placeholder markers (Phase 2.5 can wire real issue markers)
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

        // Filter chips (top overlay on map)
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

        // Permission hint overlay (only when denied/not yet granted)
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
            fontWeight = FontWeight.Medium
        )
    }
}