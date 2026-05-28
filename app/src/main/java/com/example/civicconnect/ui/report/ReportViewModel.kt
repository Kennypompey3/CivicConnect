package com.example.civicconnect.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.civicconnect.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 1. The Data Model (Ready for JSON parsing from your API)
data class CivicCategory(
    val id: String,
    val name: String,
    val colorHex: String, // APIs send colors as Hex strings (e.g., "#65D6FF")
    val iconRes: Int      // We use local drawables for now, easily swappable to URLs later
)

// 2. The Form State Payload
data class ReportUiState(
    val currentStep: Int = 1,
    val categories: List<CivicCategory> = emptyList(),
    val selectedCategory: CivicCategory? = null,
    val description: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class ReportViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        // Automatically fetch the categories when the user opens the wizard
        fetchCategories()
    }

    private fun fetchCategories() {
        // TODO: Replace this with your actual Retrofit/Node.js network call later
        val mockApiCategories = listOf(
            CivicCategory("TRANS", "Transport", "#E6E6E6", R.drawable.ic_home), // Note: swap ic_home for your actual icons later!
            CivicCategory("ELEC", "Electricity", "#E6E6E6", R.drawable.ic_home),
            CivicCategory("DRAIN", "Blocked Drain", "#E6E6E6", R.drawable.ic_home),
            CivicCategory("WASTE", "Waste Dump", "#E6E6E6", R.drawable.ic_home),
            CivicCategory("DANGER", "Danger", "#E6E6E6", R.drawable.ic_home),
            CivicCategory("OTHER", "Other", "#E6E6E6", R.drawable.ic_home)
        )

        _uiState.update { it.copy(categories = mockApiCategories) }
    }

    // --- User Interaction Events ---

    fun setLocation(lat: Double, lng: Double) {
        _uiState.update { it.copy(latitude = lat, longitude = lng) }
    }

    fun selectCategory(category: CivicCategory) {
        _uiState.update { it.copy(selectedCategory = category, currentStep = 2) }
    }

    fun updateDescription(text: String) {
        _uiState.update { it.copy(description = text) }
    }

    fun navigateBack() {
        if (_uiState.value.currentStep > 1) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun submitReport() {
        // Lock the UI while submitting
        _uiState.update { it.copy(isSubmitting = true) }

        // TODO: Map the state to a DTO and send via Retrofit here.
        // We are simulating a 1.5 second network delay for now.
        viewModelScope.launch {
            delay(1500)
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    currentStep = 3 // Move to success screen
                )
            }
        }
    }
}