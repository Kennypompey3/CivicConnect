package com.example.civicconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // ✅ Fixed: Resolves property delegate getValue error
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.example.civicconnect.data.UserSessionManager
import com.example.civicconnect.ui.MainTabsScreen
import com.example.civicconnect.ui.onboarding.OnboardingCarouselScreen
import com.example.civicconnect.ui.onboarding.OnboardingLoginScreen
import com.example.civicconnect.ui.theme.CivicConnectTheme

enum class OnboardingStage {
    CAROUSEL,
    LOGIN
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CivicConnectTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                val currentUserState by UserSessionManager.currentUser.collectAsState()
                var currentStage by remember { mutableStateOf(OnboardingStage.CAROUSEL) }

                if (currentUserState == null) {
                    when (currentStage) {
                        OnboardingStage.CAROUSEL -> {
                            OnboardingCarouselScreen(
                                onCarouselComplete = { currentStage = OnboardingStage.LOGIN }
                            )
                        }
                        OnboardingStage.LOGIN -> {
                            OnboardingLoginScreen(onLoginSuccess = {})
                        }
                    }
                } else {
                    MainTabsScreen(windowSizeClass = windowSizeClass)
                }
            }
        }
    }
}