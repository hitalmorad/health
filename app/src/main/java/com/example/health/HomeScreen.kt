package com.example.health

import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.layout.BoxScopeInstance.align
//import androidx.compose.foundation.layout.FlowRowScopeInstance.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.reminder.ReminderScreenActivity
//import com.example.health.ui.ReminderScreenActivity
import java.util.Locale


@Composable
fun HomeScreen() {
    val context = LocalContext.current
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
    val speechText = "Welcome to the Home Screen. Here you can search for health services, check your medical reports, and monitor your fitness progress."

    fun speakText(text: String) {
        tts.value?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Enable scrolling
    ) {
        TopBar()
        Spacer(modifier = Modifier.height(16.dp))
        WelcomeSection()
        Spacer(modifier = Modifier.height(16.dp))
        SearchBar()
        Spacer(modifier = Modifier.height(16.dp))
        CategorySection()
        Spacer(modifier = Modifier.height(24.dp))
        MedicalCheckupSection()
        Spacer(modifier = Modifier.height(24.dp))
        HealthCheckOptions()
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd // ✅ Ensures correct alignment inside the Box
    ) {
        Button(
            onClick = { speakText(speechText) },
            modifier = Modifier
                .size(66.dp) ,// Small button size
            //.padding(16.dp), // Padding to prevent overlap with screen edge
            shape = RoundedCornerShape(50), // Makes it circular
            colors = ButtonDefaults.buttonColors(Color(0xF23586C9))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.spk), // ✅ Correct way to use drawable
                contentDescription = "Speak Icon",
                tint = Color.White, // Remove this if the drawable should keep its original color
                modifier = Modifier.size(34.dp) // ✅ Set icon size
            )
        }
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            modifier = Modifier.size(28.dp)
        )
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun WelcomeSection() {
    Column {
        Text(text = "Hello,", fontSize = 20.sp, color = Color.Gray)
        Text(text = "Julia James 👋", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SearchBar() {
    TextField(
        value = "",
        onValueChange = {},
        placeholder = { Text("Search") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(8.dp))
    )
}

@Composable
fun CategorySection() {
    Column {
        Text(
            text = "Categories",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryCard(
                title = "Health",
                subtitle = "Wellness & Care",
                imageRes = R.drawable.doc, // Replace with actual drawable resource
                color = Color(0xFFB3E5FC),
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                title = "Fitness",
                subtitle = "Workouts & Training",
                imageRes = R.drawable.doc, // Replace with actual drawable resource
                color =  Color(0xFFB2DFDB),
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                title = "Food",
                subtitle = "Nutrition & Diet",
                imageRes = R.drawable.doc, // Replace with actual drawable resource
                color = Color(0xFFFFE0B2),
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
fun CategoryCard(title: String, subtitle: String, imageRes: Int, color: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .height(120.dp)
            //.padding(4.dp)
            .clickable {
                if (title == "Health") {
                    val intent = Intent(context, HomeScreenActivity::class.java)
                    context.startActivity(intent)
                }
                if (title == "Fitness") {
                   val intent = Intent(context, FitnessScreenActivity::class.java)
                   context.startActivity(intent)
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(color),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                painter = painterResource(id = imageRes), // Now this will work
                contentDescription = title,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}


@Composable
fun MedicalCheckupSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Hasil Medical Check-up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "View all", fontSize = 14.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(8.dp))
        MedicalCheckupItem("General Blood Analysis")
        MedicalCheckupItem("Set Notification")
    }
}

@Composable
fun MedicalCheckupItem(title: String) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                if(title == "Set Notification") {
                    val intent = Intent(context, ReminderScreenActivity::class.java)
                    context.startActivity(intent)
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.doc), // Replace with actual icon
                contentDescription = title,
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun HealthCheckOptions() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Check Your Own Health", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "View all", fontSize = 14.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HealthCheckItem("Heart Risk", Modifier.weight(1f))
            HealthCheckItem("Risk Calculator", Modifier.weight(1f))
            HealthCheckItem("Menstruation Calendar", Modifier.weight(1f))
        }
    }
}

@Composable
fun HealthCheckItem(title: String,modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier
            //.weight(0.1f)
            //.weight(1f)
            .padding(4.dp)
            .height(80.dp)
            .clickable { /* Handle Click */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.doc), // Replace with actual icon
                contentDescription = title,
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}