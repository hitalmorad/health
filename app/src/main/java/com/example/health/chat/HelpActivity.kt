package com.example.health.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

//import com.example.health.speciality.HelpScreen

class HelpActivity : ComponentActivity() {
    @RequiresApi(35)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val chatViewModel = ChatViewModel(application) // Initialize ChatViewModel
            HelpScreen(chatViewModel) // ✅ Show Help Screen in a new Activity
        }
    }
}
@RequiresApi(35)
@Composable
fun HelpScreen(chatViewModel: ChatViewModel)  {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        ChatPage(modifier = Modifier.padding(innerPadding),chatViewModel)
    }
}