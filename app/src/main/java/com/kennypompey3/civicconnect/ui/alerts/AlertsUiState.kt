package com.kennypompey3.civicconnect.ui.alerts

data class AlertsUiState(
    val alerts: List<AlertItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)