package com.r8.doitagain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.r8.doitagain.ui.RemindersApp
import com.r8.doitagain.ui.RemindersViewModel
import com.r8.doitagain.ui.theme.DoItAgainTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: RemindersViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoItAgainTheme {
                RemindersApp(viewModel = viewModel)
            }
        }
    }
}
