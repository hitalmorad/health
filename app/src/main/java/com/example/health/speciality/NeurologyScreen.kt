package com.example.health.speciality

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.util.Log
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.health.R
import com.example.health.chat.ChatPage
import com.example.health.chat.ChatViewModel
import com.example.health.chat.HelpActivity
import com.google.accompanist.web.WebView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.internal.platform.android.BouncyCastleSocketAdapter.Companion.factory
import java.util.Locale

//import kotlinx.coroutines.flow.internal.NoOpContinuation.context
//import kotlin.coroutines.jvm.internal.CompletedContinuation.context

@RequiresApi(35)
@Composable
fun NeurologyScreen() {


        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        var user by remember { mutableStateOf<FirebaseUser?>(null) }
        var profileImageUrl by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phoneNumber by remember { mutableStateOf("") }
        //var address by remember { mutableStateOf("") }
        var age by remember { mutableStateOf("") }
        val context = LocalContext.current

        var showWebView by remember { mutableStateOf(false) }

    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    // Initialize TTS
    LaunchedEffect(Unit) {
        tts.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale.US
                tts.value?.setSpeechRate(1.1f)
            }
        }
    }

    // Predefined text to be spoken
    val speechText ="Welcome to the Neurology Screen. Here, you can access virtual consultations, check symptoms, and explore sleep and well-being insights."
    fun speakText(text: String) {
        tts.value?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun updateUserProfile(document: DocumentSnapshot) {
        profileImageUrl = document.getString("profileImage") ?: ""
        username = document.getString("username") ?: ""
        email = document.getString("email") ?: ""
        phoneNumber = document.getString("phoneNumber") ?: ""
        // address = document.getString("address") ?: ""
       // age = (document.getDouble("age") ?: "").toString()
    }

    LaunchedEffect(key1 = auth.currentUser) {
        auth.currentUser?.let {
            firestore.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        updateUserProfile(document)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ProfileScreen", "Error fetching user data", exception)
                    //LoginScreen(navController)
                }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        // Top Profile and Menu
        Row( // Changed to Row to align elements horizontally
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp),
            verticalAlignment = Alignment.CenterVertically, // Align items in the center
            // horizontalArrangement = Arrangement.SpaceBetween // Space between elements
        ) {
            // Left Side: Profile Image & Greeting
            Column(horizontalAlignment = Alignment.Start) {
                if(profileImageUrl.isNotEmpty())
                {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUrl),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape))
                }
                else{
                    Image(
                        painter = painterResource(id = R.drawable.prfl), // Profile image resource
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape))
                }


                Spacer(modifier = Modifier.height(8.dp)) // Space between image and text

                Text(
                    text = "Hello,",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal
                )

                Text(
                    text = "   $username👋",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Right Side: Additional Image
            Image(
                painter = painterResource(id = R.drawable.topneuro), // Replace with your image
                contentDescription = "Right Side Image",
                modifier = Modifier
                    .size(180.dp)
                    //.align(Alignment.CenterEnd)
                    // Align to the right side
                    .offset(x = (45).dp) // Optional rounded corners
            )
        }

        // Spacer(modifier = Modifier.height(0.dp))

        // Search Bar
        TextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFF5F5F5)),
            placeholder = {
                Text("Search for a doctor")
            },
            leadingIcon = {
                Icon(painter = painterResource(id = R.drawable.medi), contentDescription = "Search")
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Feature Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FeatureCard("Consultation", "56 doctors", R.drawable.cons, Color(0xFFFFE0B2)) {
                // TODO: Navigate to Neurologist Consultation Screen
            }
            FeatureCard("Pharmacy", "6 pharmacies", R.drawable.medi, Color(0xFFD1C4E9)) {
                // TODO: Navigate to Pharmacy Screen
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // My Health Section
        Text("My Health", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HealthCard("Heart Rate", "78", "bpm")
            HealthCard("Sleep", "8", "hrs")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Additional Features
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FeatureCard(
                "Symptom Checker",
                "Check your symptoms",
                R.drawable.medi,
                Color(0xFFB3E5FC)
            ) {
                // TODO: Navigate to Symptom Checker Screen
            }

            Spacer(modifier = Modifier.height(12.dp))

            FeatureCard(
                "Sleep & Mental Well-being",
                "Track your sleep",
                R.drawable.medi,
                Color(0xFFB2DFDB)
            ) {
                // TODO: Navigate to Sleep & Well-being Screen
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Push the button to bottom

        // Get Help Button at Bottom Center
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // "Find Help" Button at Bottom-Center
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Bottom, // Align items at the bottom
                horizontalAlignment = Alignment.CenterHorizontally // Center horizontally
            ) {
                Button(
                    onClick = { showWebView = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFAFFB4)), // Light Green
                    shape = RoundedCornerShape(50.dp), // Fully rounded corners
                    modifier = Modifier
                        .fillMaxWidth(0.7f) // Adjust width
                        .height(50.dp) // Adjust height
                ) {
                    if (showWebView) {
                        val intent = Intent(context, HelpActivity::class.java)
                        context.startActivity(intent)
                        showWebView = false
                    }
                    Icon(
                        imageVector = Icons.Default.Add, // "+" icon
                        contentDescription = "Add",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp)) // Space between icon and text
                    Text(
                        text = "Find help",
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // Space between buttons
            }

            // "Speak" Button at Bottom-Right
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomEnd // ✅ Ensures correct alignment inside the Box
            ) {
                Button(
                    onClick = { speakText(speechText) },
                    modifier = Modifier.size(66.dp), // Small button size
                    shape = RoundedCornerShape(50), // Makes it circular
                    colors = ButtonDefaults.buttonColors(Color(0xF23586C9))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.spk), // ✅ Correct way to use drawable
                        contentDescription = "Speak Icon",
                       //tint = Color.White, // Remove this if the drawable should keep its original color
                        modifier = Modifier.size(34.dp) // ✅ Set icon size
                    )
                }
            }
        }

    }
}


@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    bgColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(158.dp, 120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

// Health Info Card
@Composable
fun HealthCard(title: String, value: String, unit: String) {
    Card(
        modifier = Modifier
            .size(160.dp, 100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$value $unit", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}


