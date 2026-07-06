package com.kennypompey3.civicconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.kennypompey3.civicconnect.data.UserSessionManager
import com.kennypompey3.civicconnect.ui.MainTabsScreen
import com.kennypompey3.civicconnect.ui.onboarding.OnboardingCarouselScreen
import com.kennypompey3.civicconnect.ui.onboarding.OnboardingLoginScreen
import com.kennypompey3.civicconnect.ui.theme.CivicConnectTheme

enum class OnboardingStage {
    CAROUSEL,
    LOGIN
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Spin up preference links before evaluating layout composition states
        UserSessionManager.initialize(applicationContext)

        setContent {
            CivicConnectTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                val isOnboarded by UserSessionManager.isOnboarded.collectAsState()
                var currentStage by remember { mutableStateOf(OnboardingStage.CAROUSEL) }

                if (!isOnboarded) {
                    when (currentStage) {
                        OnboardingStage.CAROUSEL -> {
                            OnboardingCarouselScreen(
                                onCarouselComplete = { currentStage = OnboardingStage.LOGIN }
                            )
                        }
                        OnboardingStage.LOGIN -> {
                            OnboardingLoginScreen(
                                onLoginSuccess = {
                                    // Global storage triggers state recomposition automatically here
                                }
                            )
                        }
                    }
                } else {
                    MainTabsScreen(windowSizeClass = windowSizeClass)
                }
            }
        }
    }
}