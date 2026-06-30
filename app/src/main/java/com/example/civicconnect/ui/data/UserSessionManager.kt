package com.example.civicconnect.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime

object UserSessionManager {
    private const val PREFS_NAME = "civic_connect_preferences"
    private const val KEY_USERNAME = "session_user_name"
    private const val KEY_IS_ONBOARDED = "session_is_onboarded"

    private lateinit var prefs: SharedPreferences

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    // Invoked instantly at application launch inside MainActivity
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _userName.value = prefs.getString(KEY_USERNAME, "") ?: ""
        _isOnboarded.value = prefs.getBoolean(KEY_IS_ONBOARDED, false)
    }

    fun saveSession(name: String) {
        prefs.edit().apply {
            putString(KEY_USERNAME, name)
            putBoolean(KEY_IS_ONBOARDED, true)
            apply()
        }
        _userName.value = name
        _isOnboarded.value = true
    }

    // Evaluates device hour settings to provide accurate system time windows
    fun getGreetingPrefix(): String {
        return when (LocalTime.now().hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
}