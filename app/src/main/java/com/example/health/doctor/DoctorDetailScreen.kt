package com.example.health.doctor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.health.api.Address
import com.example.health.api.Doctor
import com.example.health.api.Fees
import com.example.health.api.Practice
import com.example.health.api.Ratings

class DoctorDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val doctorId = intent.getIntExtra("doctorId", -1)
        val doctorName = intent.getStringExtra("doctorName") ?: "Unknown"
        val doctorSpecialty = intent.getStringExtra("doctorSpecialty") ?: "Unknown Specialty"
        val doctorRating : Int= intent.getIntExtra("doctorRating", 0)
        val doctorImageUrl = intent.getStringExtra("doctorImageUrl") ?: ""

        val doctor = Doctor(
            doctorId = doctorId,
            doctorName = doctorName,
            profileUrl = "", // You can add default or fetch from API if missing
            specialization = doctorSpecialty,
            experience = 0, // Provide default experience
            practice = Practice("", Address("", "")), // Provide default values
            fees = Fees(0, ""), // Provide default values
            ratings = Ratings(doctorRating, 0), // Provide default ratings count
            imageUrl = doctorImageUrl
        )

        setContent {
            DoctorDetailScreen(doctor)
        }
    }
}

@Composable
fun DoctorDetailScreen(doctor: Doctor) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = doctor.imageUrl, // ✅ Make sure imageUrl is used correctly
                contentDescription = "Doctor Image",
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(doctor.doctorName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(doctor.specialization, fontSize = 18.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text("⭐ ${doctor.ratings.recommendationPercent}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Dr. ${doctor.doctorName} is an experienced ${doctor.specialization} with an excellent reputation. Book an appointment to get expert consultation.",
                fontSize = 16.sp
            )
        }
    }
}
