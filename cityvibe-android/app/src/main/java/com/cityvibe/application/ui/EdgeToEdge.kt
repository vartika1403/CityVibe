package com.cityvibe.application.ui

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

/**
 * Draws behind the system bars on every API level (Android 15+ forces this when
 * targetSdk >= 35). Status bar icons stay white to read on the coloured headers;
 * screens pad their own content with window insets.
 */
fun ComponentActivity.enableCityVibeEdgeToEdge() {
    enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
}
