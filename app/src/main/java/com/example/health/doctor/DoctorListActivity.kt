package com.example.health.doctor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.health.DoctorListScreen
import com.example.health.model.DoctorViewModel

class DoctorListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val doctorViewModel = DoctorViewModel()
            DoctorListScreen(onDoctorClick = { doctorId ->
                val intent = Intent(this, DoctorDetailActivity::class.java)
                intent.putExtra("doctorId", doctorId)
                startActivity(intent)
            }, doctorViewModel)
            }
        }
}

