package com.example.health

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.health.api.Doctor
import com.example.health.doctor.DoctorDetailActivity
import com.example.health.model.DoctorViewModel

@Composable
fun DoctorListScreen(onDoctorClick: (Int) -> Unit, doctorViewModel: DoctorViewModel = viewModel()) {
    val doctorList by doctorViewModel.doctors.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(doctorList) { doctor ->
            DoctorCard(doctor) {
                val intent = Intent(context, DoctorDetailActivity::class.java).apply {
                    putExtra("doctorId", doctor.doctorId)
                    putExtra("doctorName", doctor.doctorName)
                    putExtra("doctorSpecialty", doctor.specialization)
                    putExtra("doctorRating", doctor.ratings.recommendationPercent)
                    putExtra("doctorImageUrl", doctor.imageUrl)
                }
                context.startActivity(intent)
            }
        }
    }
}

@Composable
fun DoctorCard(doctor: Doctor, onClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(doctor.doctorId) },
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = doctor.imageUrl,
                contentDescription = "Doctor Image",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(doctor.doctorName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(doctor.specialization, color = Color.Gray)
                Text("⭐ ${doctor.ratings.recommendationPercent}", fontSize = 14.sp, color = Color(0xFFFFD700))
            }
        }
    }
}
