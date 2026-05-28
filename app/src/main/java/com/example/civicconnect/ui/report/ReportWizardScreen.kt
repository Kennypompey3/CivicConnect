package com.example.civicconnect.ui.report

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
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.civicconnect.ui.theme.CivicConnectTheme

data class ReportFormState(
    val currentStep: Int = 1,
    val selectedCategory: CivicCategory? = null,
    val description: String = "",
    val latitude: Double? = 6.5244,
    val longitude: Double? = 3.3792,
    val isSubmitting: Boolean = false
)

data class CivicCategory(
    val id: String,
    val name: String,
    val displayColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportWizardScreen(
    onDismissWizard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var formState by remember { mutableStateOf(ReportFormState()) }

    Scaffold(
        topBar = {
            if (formState.currentStep < 3) {
                TopAppBar(
                    title = { Text("Create Report", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (formState.currentStep > 1) {
                                formState = formState.copy(currentStep = formState.currentStep - 1)
                            } else {
                                onDismissWizard()
                            }
                        }) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
                )
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { scaffoldPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(horizontal = 24.dp)
        ) {
            if (formState.currentStep < 3) {
                StepProgressBar(currentStep = formState.currentStep)
                Spacer(modifier = Modifier.height(24.dp))
            }

            AnimatedContent(
                targetState = formState.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "WizardStepTransition"
            ) { step ->
                when (step) {
                    1 -> CategorySelectionStep(
                        selectedCategory = formState.selectedCategory,
                        onCategorySelected = { cat ->
                            formState = formState.copy(selectedCategory = cat, currentStep = 2)
                        }
                    )
                    2 -> DetailFormStep(
                        formState = formState,
                        onDescriptionChanged = { text -> formState = formState.copy(description = text) },
                        onSubmitReport = {
                            formState = formState.copy(currentStep = 3)
                        }
                    )
                    3 -> SuccessConfirmationStep(
                        categoryName = formState.selectedCategory?.name ?: "Issue",
                        onClose = onDismissWizard
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySelectionStep(
    selectedCategory: CivicCategory?,
    onCategorySelected: (CivicCategory) -> Unit
) {
    val categories = remember {
        listOf(
            CivicCategory("TRANS", "Transport", Color(0xFF65D6FF)),
            CivicCategory("ELEC", "Electricity", Color(0xFFECE77A)),
            CivicCategory("WASTE", "Waste Dump", Color(0xFFC4FFB0)),
            CivicCategory("DRAIN", "Blocked Drain", Color(0xFFF3D8DB)),
            CivicCategory("WATER", "Water Leak", Color(0xFFCEC8D4)),
            CivicCategory("ROAD", "Road / Pothole", Color(0xFFBDF0A5))
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "What is the issue?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Select a category below to route your complaint to the right municipal department.",
            fontSize = 14.sp,
            color = Color(0xFF90A199),
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categories) { category ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE0E1DD))
                        .clickable { onCategorySelected(category) }
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(category.displayColor)
                    )
                    Text(
                        text = category.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailFormStep(
    formState: ReportFormState,
    onDescriptionChanged: (String) -> Unit,
    onSubmitReport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Add Report Details",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFE0E1DD))
                .border(2.dp, Color(0xFF1B263B).copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Upload",
                    tint = Color(0xFF1B263B),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Take or upload a photo", color = Color(0xFF1B263B), fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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
            Text(
                text = "GPS Location verified automatically",
                fontSize = 13.sp,
                color = Color(0xFF1B263B),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = formState.description,
            onValueChange = onDescriptionChanged,
            placeholder = { Text("Provide specific context or landmarks to help repair crews locate this issue...") },
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
            enabled = formState.description.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B263B))
        ) {
            Text("Submit Report", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
private fun SuccessConfirmationStep(
    categoryName: String,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = Color(0xFF50BB6E),
            modifier = Modifier.size(90.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Report Submitted!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "Your $categoryName log has been completely synchronized and routed to the primary Department of Public Works administration portal.",
            fontSize = 14.sp,
            color = Color(0xFF90A199),
            textAlign = TextAlign.Center,
            // ✅ Cleaned up padding signature mismatch by chaining modifiers cleanly
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 48.dp)
        )

        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(52.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B263B))
        ) {
            Text("Back to Dashboard", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
        }
    }
}

@Composable
private fun StepProgressBar(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(2) { index ->
            val stepIndicator = index + 1
            val alphaColor = if (stepIndicator <= currentStep) Color(0xFF1B263B) else Color(0xFFE0E1DD)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(alphaColor)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF8FAFC)
@Composable
private fun WizardPreview() {
    CivicConnectTheme {
        ReportWizardScreen(onDismissWizard = {})
    }
}