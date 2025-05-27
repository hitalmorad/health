package com.example.health.chat

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

//import com.example.health.speciality.HelpScreen

class HelpActivity : ComponentActivity() {
    private lateinit var chatViewModel: ChatViewModel

    @RequiresApi(35)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            chatViewModel = viewModel() // Use viewModel() to properly scope ChatViewModel
            HelpScreen(chatViewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chatViewModel.stopServices() // Stop TTS when the activity is destroyed
    }

    // Ensure back press sets the result and finishes the activity (as per previous navigation setup)
    override fun onBackPressed() {
        val resultIntent = Intent().apply {
            putExtra("navigate_to", "home")
        }
        setResult(RESULT_OK, resultIntent)
        finish()
        super.onBackPressed()
    }
}

@RequiresApi(35)
@Composable
fun HelpScreen(chatViewModel: ChatViewModel) {
    // Stop TTS when the composable is disposed (additional safety)
    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.stopServices()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        ChatPage(modifier = Modifier.padding(innerPadding), chatViewModel)
    }
}