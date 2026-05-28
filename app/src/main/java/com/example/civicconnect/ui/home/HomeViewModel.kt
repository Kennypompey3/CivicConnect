package com.example.civicconnect.ui.home

import androidx.lifecycle.ViewModel
import com.example.civicconnect.Issue
import com.example.civicconnect.sampleIssues
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Holds all the data the home dashboard needs
data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "Sarah",
    val impactScore: String = "A+",
    val issuesResolvedCount: Int = 12,
    val pendingCount: Int = 3,
    val resolvedCount: Int = 8,
    val nearbyIssues: List<Issue> = emptyList(),
    val isExpanded: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        _uiState.update { currentState ->
            currentState.copy(
                nearbyIssues = sampleIssues,
                isLoading = false
            )
        }
    }

    fun toggleExpanded() {
        _uiState.update { currentState ->
            currentState.copy(isExpanded = !currentState.isExpanded)
        }
    }
}