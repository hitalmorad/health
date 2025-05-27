package com.example.health.doctor

import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.rememberAsyncImagePainter
import com.example.health.Dao.DatabaseProvider
import com.example.health.R
import com.example.health.api.Doctor
import com.example.health.api.DoctorRepository
import com.example.health.api.RetrofitClient

class DoctorListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure the app is fullscreen by hiding the status bar
        // Method 1: Using WindowInsetsControllerCompat (modern approach)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars()) // Hide the status bar

        // Method 2: Using Window flags (legacy approach for compatibility)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Method 3: Additional flags for immersive mode (for Android 4.4 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        // Initialize the repository
        val database = DatabaseProvider.getDatabase(applicationContext)
        val repository = DoctorRepository(database.doctorDao(), RetrofitClient.doctorApiService)
        val specializationFilter = intent.getStringExtra("specialization")

        setContent {
            DoctorListScreen(repository = repository, specializationFilter = specializationFilter)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorListScreen(repository: DoctorRepository, specializationFilter: String?) {
    var doctors by remember { mutableStateOf(listOf<Doctor>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val activity = currentActivity()

    LaunchedEffect(Unit) {
        repository.fetchAndStoreDoctors().fold(
            onSuccess = {
                val allDoctors = repository.getAllDoctors()
                doctors = if (specializationFilter != null) {
                    allDoctors.filter { it.specialization.equals(specializationFilter, ignoreCase = true) }
                } else {
                    allDoctors
                }
                isLoading = false
            },
            onFailure = { e ->
                errorMessage = "Failed to load doctors: ${e.message}"
                isLoading = false
            }
        )
    }

    val groupedDoctors = doctors.groupBy { it.specialization }

    // Use a Box to set a consistent background color for the entire screen
    Box(
        modifier = Modifier
            .fillMaxSize()
           // .background(Color(0xFF4CAF50)) // Match the TopAppBar color for consistency
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "All Doctors", color = Color.White) },
                    modifier = Modifier.background(Color(0xFF4CAF50)), // TopAppBar background color
                    navigationIcon = {
                        IconButton(onClick = { activity?.finish() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.back),
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent
                    ,contentWindowInsets = WindowInsets(0)// Make Scaffold background transparent
        ) { paddingValues ->
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                    }
                }
                else -> {
                    if (doctors.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No doctors available",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues), // Use paddingValues from Scaffold
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            groupedDoctors.forEach { (specialty, doctorList) ->
                                item {
                                    Text(
                                        text = specialty,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                                items(doctorList) { doctor ->
                                    DoctorCard(doctor = doctor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorCard(doctor: Doctor) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (doctor.imageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(doctor.imageUrl),
                    contentDescription = doctor.doctorName,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.prfl),
                    contentDescription = "Default Doctor Image",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = doctor.doctorName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Experience: ${doctor.experience} years",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Practice: ${doctor.practice.name}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Address: ${doctor.practice.address.line1}, ${doctor.practice.address.city}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Fees: ${doctor.fees.amount} ${doctor.fees.currency}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.star),
                        contentDescription = "Recommendation",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${doctor.ratings.recommendationPercent}% Recommended",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "Patients: ${doctor.ratings.patientsCount}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(38.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:+1234567890")
                            }
                            context.startActivity(intent)
                        }
                )
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Chat",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://wa.me/+1234567890")
                            }
                            context.startActivity(intent)
                        }
                )
            }
        }
    }
}

@Composable
fun currentActivity(): ComponentActivity? {
    val context = LocalContext.current
    return when (context) {
        is ComponentActivity -> context
        is ContextWrapper -> context.baseContext as? ComponentActivity ?: currentActivity()
        else -> null
    }
}