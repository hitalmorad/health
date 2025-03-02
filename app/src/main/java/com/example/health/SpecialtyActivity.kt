package com.example.health

/*import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
*/

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.speciality.NeurologyScreen

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight

class SpecialtyActivity : ComponentActivity() {
    @RequiresApi(35)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val specialty = intent.getStringExtra("SPECIALTY_NAME") ?: "Unknown"

        setContent {
            SpecialtyScreen(specialty)
        }
    }
}

@RequiresApi(35)
@Composable
fun SpecialtyScreen(specialty: String) {
    if (specialty == "Neurology") {
        NeurologyScreen()
    }

    if (specialty == "Orthopedics") {
        OrthopedicsScreen()
    }
}








val customFont = FontFamily(
    Font(R.font.cr, FontWeight.Normal) // Use your actual font file
)

@Composable
fun OrthopedicsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB6E0F3)) // Light blue medical theme
            .padding(16.dp)
    ) {
        TopSection()
        Spacer(modifier = Modifier.height(11.dp))
        ExerciseRehabSection()
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Exercise & Rehab Programs",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        PainRecoveryCard()
        Spacer(modifier = Modifier.weight(1f))
        ReminderCalendar()
        EmergencyButton()
    }
}

@Composable
fun ReminderCalendar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Set Reminder",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Simulated Calendar (Replace with actual calendar component)
        Image(
            painter = painterResource(id = R.drawable.schedule), // Replace with your calendar image
            contentDescription = "Calendar Icon",
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { /* TODO: Open date picker / reminder setup */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)), // Blue color
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Book Slot / Set Reminder", fontSize = 16.sp, color = Color.White)
        }
    }
}


@Composable
fun TopSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent) // Light teal background
            .padding(horizontal = 0.dp, vertical = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            //horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Medical Icon Button (Clickable)
            Box(
                modifier = Modifier
                    .size(119.dp)
                    .background(Color.Transparent, shape = CircleShape)
                    .clickable { /* TODO: Add action */ },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ortho_top), // Replace with your actual icon
                    contentDescription = "Medical Icon",
                    modifier = Modifier.size(118.dp)
                )
            }

            // Orthopedics Title with Stylish Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Orthopedics",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily =customFont,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow( // Add shadow for depth
                            color = Color.Gray, offset = Offset(1f, 1f), blurRadius = 2f
                        )
                    )
                )

                Text(
                    text = "Specialist Care",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily =customFont,  // Use custom font
                    color = Color.White.copy(alpha = 0.8f),
                    style = TextStyle(letterSpacing = 0.8.sp)
                )
            }
        }
    }
}


@Composable
fun ExerciseRehabSection() {
    Column {
        Text(
            text = "Exercise & Rehab Programs",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            VideoThumbnail(R.drawable.doc) // Replace with actual video images
            VideoThumbnail(R.drawable.doc)
            VideoThumbnail(R.drawable.doc)
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "See All",
            color = Color.Blue,
            fontSize = 18.sp,
            modifier = Modifier.clickable { /* Navigate to video list */ }
        )


    }
}

@Composable
fun VideoThumbnail(videoRes: Int) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Image(
            painter = painterResource(id = videoRes),
            contentDescription = "Video Thumbnail",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun PainRecoveryCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { /* Navigate to Pain Tracker */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2FDFF)) // Yellow for attention
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Check Recovery", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmergencyButton() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Button(
            onClick = { /* Emergency action */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(50),
            modifier = Modifier.size(150.dp, 50.dp)
        ) {
            Text(text = "Emergency", fontSize = 18.sp, color = Color.White)
        }
    }
}


