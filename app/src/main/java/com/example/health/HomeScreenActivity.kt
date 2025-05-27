package com.example.health

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.health.doctor.DoctorListActivity

class HomeScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeScreens()  // Call your Composable function here
        }
    }
}

@Composable
fun HomeScreens() {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        // Poster Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color(0xFFE1BEE7),
            elevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Book and Schedule with \nnearest doctor",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(context, LocationActivity::class.java)
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFAB47BC))
                        ) {
                            Text(
                                "Find Nearby",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Overlapping com.example.health.api.Doctor Image
                Image(
                    painter = painterResource(R.drawable.doc),
                    contentDescription = "com.example.health.api.Doctor",
                    modifier = Modifier
                        .size(485.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = (105).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Appointment and Consultation Section (In-Clinic and Video Consultation in a Row)
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppointmentCard(
                title = "Book In-Clinic Appointment",
                imageRes = R.drawable.doc,
                onClick = {
                    // Add navigation logic for in-clinic appointment
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            AppointmentCard(
                title = "Instant Video Consultation",
                imageRes = R.drawable.doc ,
                onClick = {
                    // Add navigation logic for video consultation
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .align(Alignment.CenterVertically)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Find a com.example.health.api.Doctor for Your Health Problem Section
        Text(
            text = "Find a com.example.health.api.Doctor for your Health Problem",
            fontSize = 18.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val healthProblems = listOf(
                "General Physician" to R.drawable.healthcare,
                "Skin & Hair" to R.drawable.skin,
                "Women's Health" to R.drawable.women,
                "Dental Care" to R.drawable.dental,
                "Child Specialist" to R.drawable.child,
                "Ear, Nose, Throat" to R.drawable.ent,
                "Mental Wellness" to R.drawable.health,
                "More" to R.drawable.more
            )

            items(healthProblems) { (problem, image) ->
                HealthProblemCard(problem, image) {
                    val intent = Intent(context, DoctorListActivity::class.java).apply {
                        putExtra("specialization", problem)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}

// Composable for Appointment Card (In-Clinic and Video Consultation)
@Composable
fun AppointmentCard(title: String, imageRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(140.dp) // Increased height to accommodate new layout
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFE3F2FD),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f)) // Pushes image to the center vertically
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.weight(1f)) // Balances spacing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    lineHeight = 16.sp,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Arrow Forward",
                    tint = Color.Black,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
// Composable for Health Problem Card
@Composable
fun HealthProblemCard(
    problem: String,
    imageRes: Int,
    onClick:  () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(12.dp),
            backgroundColor = Color(0xFFE3F2FD),
            elevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = problem,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = problem,
            fontSize = 12.sp,
            color = Color.Black,
            maxLines = 2,
            lineHeight = 14.sp
        )
    }
}


@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF5F5F5))
            .shadow(4.dp, shape = RoundedCornerShape(24.dp)),
        placeholder = {
            Text(
                "Search doctors, specialties...",
                color = Color.Gray,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color(0xFFF5F5F5),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.Black
        )
    )
}