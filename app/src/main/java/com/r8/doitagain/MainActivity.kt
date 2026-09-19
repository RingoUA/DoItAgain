package com.r8.doitagain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.r8.doitagain.ui.HomeScreen
import com.r8.doitagain.ui.theme.DoItAgainTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoItAgainTheme {
                HomeScreen(reminders = emptyList())
            }
        }
    }
}
