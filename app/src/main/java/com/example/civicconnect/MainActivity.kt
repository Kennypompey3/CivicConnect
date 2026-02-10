package com.example.civicconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.civicconnect.ui.home.HomeScreen
import com.example.civicconnect.ui.theme.CivicConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CivicConnectTheme {
                HomeScreen()
            }
        }
    }
}
