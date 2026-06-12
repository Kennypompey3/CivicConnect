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

@Composable
fun ReportWizardScreen(
    onDismissWizard: () -> Unit,
) {

        } else {
            onDismissWizard()
        }
    }
        Column(
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            }

            AnimatedContent(
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "WizardStepTransition"
            ) { step ->
                when (step) {
                    1 -> CategorySelectionStep(
                    )
                    2 -> DetailFormStep(
                    )
                    3 -> SuccessConfirmationStep(
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySelectionStep(
    onCategorySelected: (CivicCategory) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categories) { category ->
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clickable { onCategorySelected(category) }
                ) {
                    )
                    Text(
                        text = category.name,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailFormStep(
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(20.dp))
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(8.dp))
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
        }

        Spacer(modifier = Modifier.height(24.dp))
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            onValueChange = onDescriptionChanged,
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
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B263B))
        ) {
        }
    }
}

@Composable
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            fontSize = 14.sp,
            color = Color(0xFF90A199),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
        )
        Button(
            onClick = onClose,
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B263B))
        ) {
        }
    }
}

@Composable
private fun StepProgressBar(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
            val stepIndicator = index + 1
        }
    }
}

@Composable
private fun WizardPreview() {
    CivicConnectTheme {
    }
}