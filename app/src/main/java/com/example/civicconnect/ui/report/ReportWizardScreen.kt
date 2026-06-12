package com.example.civicconnect.ui.report

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.civicconnect.ui.theme.CivicConnectTheme
import com.google.android.gms.maps.model.LatLng

@Composable
fun ReportWizardScreen(
    initialLocation: LatLng?,
    onDismissWizard: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Seeds the exact Map coordinates directly into the reporting flow upon launch
    LaunchedEffect(initialLocation) {
        initialLocation?.let {
            viewModel.setLocation(it.latitude, it.longitude)
        }
    }

    // Intercepts the physical system back swipe gesture natively
    BackHandler {
        if (uiState.currentStep > 1) {
            viewModel.navigateBack()
        } else {
            viewModel.resetForm()
            onDismissWizard()
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // The clean, thin 3-segment progress bar matching your mockup
            if (uiState.currentStep < 3) {
                StepProgressBar(currentStep = uiState.currentStep)
                Spacer(modifier = Modifier.height(32.dp))
            }

            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "WizardStepTransition"
            ) { step ->
                when (step) {
                    1 -> CategorySelectionStep(
                        categories = uiState.categories,
                        onCategorySelected = { category -> viewModel.selectCategory(category) }
                    )
                    2 -> DetailFormStep(
                        uiState = uiState,
                        onDescriptionChanged = { text -> viewModel.updateDescription(text) },
                        onImageSelected = { uri -> viewModel.updateSelectedImageUri(uri) },
                        onSubmitReport = { viewModel.submitReport() }
                    )
                    3 -> SuccessConfirmationStep(
                        categoryName = uiState.selectedCategory?.name ?: "Issue",
                        onClose = {
                            viewModel.resetForm()
                            onDismissWizard()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySelectionStep(
    categories: List<CivicCategory>,
    onCategorySelected: (CivicCategory) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "What's The Issue?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 140.dp), // Pushes bottom items safely above navigation bar
            modifier = Modifier.weight(1f)
        ) {
            items(categories) { category ->
                val parsedColor = remember(category.colorHex) {
                    try {
                        Color(android.graphics.Color.parseColor(category.colorHex))
                    } catch (_: Exception) {
                        Color(0xFFE6E6E6) // Fallback gray if Hex parsing throws an exception
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(parsedColor)
                        .clickable { onCategorySelected(category) }
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = category.iconRes),
                        contentDescription = category.name,
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = category.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailFormStep(
    uiState: ReportUiState,
    onDescriptionChanged: (String) -> Unit,
    onImageSelected: (android.net.Uri?) -> Unit,
    onSubmitReport: () -> Unit
) {
    // Media intent system bridge contract configuration
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onImageSelected(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 140.dp)
    ) {
        Text(
            text = "Add Report Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Secure photo picker box container card
        val hasImage = uiState.selectedImageUri != null
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (hasImage) Color(0xFF415A77).copy(alpha = 0.15f) else Color(0xFFE0E1DD))
                .border(
                    width = 2.dp,
                    color = if (hasImage) Color(0xFF50BB6E) else Color(0xFF1B263B).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (hasImage) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF50BB6E), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Photo Attached Successfully", color = Color(0xFF1B263B), fontWeight = FontWeight.Bold)
                    Text(text = "Tap again to change image", color = Color(0xFF90A199), fontSize = 12.sp)
                } else {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF1B263B), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Take or upload a photo", color = Color(0xFF1B263B), fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Displays live, truncated coordinate values passed straight down from the long-pressed map location
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF415A77).copy(alpha = 0.1f))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF50BB6E)))
            Spacer(modifier = Modifier.width(12.dp))

            val locationText = if (uiState.latitude != null && uiState.longitude != null) {
                val latStr = uiState.latitude.toString().take(7)
                val lngStr = uiState.longitude.toString().take(7)
                "GPS Verified: $latStr, $lngStr"
            } else {
                "GPS Location pending..."
            }

            Text(text = locationText, fontSize = 13.sp, color = Color(0xFF1B263B), fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Description", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChanged,
            placeholder = { Text("Provide specific context or landmarks...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1B263B),
                unfocusedBorderColor = Color(0xFFE0E1DD)
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onSubmitReport,
            enabled = uiState.description.isNotBlank() && hasImage && !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B263B))
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(text = "Submit Report", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun SuccessConfirmationStep(categoryName: String, onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 120.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF50BB6E), modifier = Modifier.size(90.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Report Submitted!", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your $categoryName log has been completely synchronized.",
            fontSize = 14.sp,
            color = Color(0xFF90A199),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 48.dp)
        )
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(0.7f).height(52.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B263B))
        ) {
            Text(text = "Back to Dashboard", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
        }
    }
}

@Composable
private fun StepProgressBar(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { index ->
            val stepIndicator = index + 1
            val alphaColor = if (stepIndicator <= currentStep) Color.Black else Color(0xFFD3D3D3)
            Box(modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(999.dp)).background(alphaColor))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun WizardPreview() {
    CivicConnectTheme {
        ReportWizardScreen(initialLocation = LatLng(6.5244, 3.3792), onDismissWizard = {})
    }
}