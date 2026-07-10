package com.kennypompey3.civicconnect.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlertsViewModel : ViewModel() {

    // Backing property to avoid exposing mutable state directly to composables
    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    init {
        // Automatically request data updates as soon as this component initializes
        fetchAlerts()
    }

    fun fetchAlerts() {
        // Trigger loading state and wipe out previous errors before the asynchronous fetch starts
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // Simulating a 1-second network/database latency delay to test progress indicators
                delay(1000)

                // Static source-of-truth container ready to be swapped with a real Retrofit/Room repository call later
                val sampleAlerts = listOf(
                    AlertItem("1", "Heavy Rain Alert", "Flash flood warning. Please report blocked drains immediately.", "3h ago", AlertSeverity.SEVERE),
                    AlertItem("2", "Power Outage", "Major power outage reported in Sector 7.", "15m ago", AlertSeverity.SEVERE),
                    AlertItem("3", "Crew Assigned", "Maintenance crew is heading to your reported broken light.", "1h ago", AlertSeverity.IN_PROGRESS),
                    AlertItem("4", "Inspection Scheduled", "Sidewalk damage report is scheduled for inspection.", "1h ago", AlertSeverity.IN_PROGRESS),
                    AlertItem("5", "Pothole Fixed", "The pothole on 4th Avenue has been fixed!", "1d ago", AlertSeverity.RESOLVED),
                    AlertItem("6", "Trash Cleared", "Waste dump behind Market St has been cleared.", "2d ago", AlertSeverity.RESOLVED)
                )

                _uiState.update {
                    it.copy(alerts = sampleAlerts, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.localizedMessage ?: "Failed to synchronize alerts.")
                }
            }
        }
    }
}