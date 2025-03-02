package com.example.health

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
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
    var showDoctorList by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Poster Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color(0xFFE1BEE7),
            elevation = 18.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Poster Content
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Book and Schedule with \n nearest doctor",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Button(
                            onClick = { /* TODO: Navigate to booking */ },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
                        ) {
                            Text("Find Nearby", color = Color.Magenta)
                        }
                    }
                }

                // Overlapping Doctor Image
                Image(
                    painter = painterResource(R.drawable.doc),
                    contentDescription = "Doctor",
                    modifier = Modifier
                        .size(485.dp)
                        .align(Alignment.CenterEnd) // Align to the right side
                        .offset(x = (105).dp) // Moves image outside the box slightly
                )
            }
        }


        Spacer(modifier = Modifier.height(26.dp))

        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Doctor Specialties - Horizontal Scroll
        Text(text = "Doctor Specialities", fontSize = 18.sp, color = Color.Black)
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val specialties = listOf(
                "Cardiology" to R.drawable.cd,
                "Dentistry" to R.drawable.deti,
                "Neurology" to R.drawable.neur,
                "Orthopedics" to R.drawable.orth
            )

            items(specialties) { (specialty, image) ->
                SpecialtyCard(specialty, image) {
                    // Navigate to SpecialtyActivity
                    val intent = Intent(context, SpecialtyActivity::class.java).apply {
                        putExtra("SPECIALTY_NAME", specialty)
                    }
                    context.startActivity(intent)
                }
            }
        }


        Spacer(modifier = Modifier.height(22.dp))

        // Top Doctors Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Top Doctors", fontSize = 18.sp, color = Color.Black)
            TextButton(onClick = { showDoctorList = true }) {
                Text("See All")
                if (showDoctorList) {
                    val intent = Intent(context, DoctorListActivity::class.java)
                    context.startActivity(intent)

                    showDoctorList = false
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(listOf("Dr. Michael Roberts", "Dr. Richard Lee")) { doctor ->
                DoctorCard(doctor) {
                    // Navigate to DoctorDetailActivity
                    /* val intent = Intent(context, DoctorDetailActivity::class.java).apply {
                         putExtra("DOCTOR_NAME", doctor)
                     }
                     context.startActivity(intent)*/
                }
            }
        }
    }
}

// Composable for Doctor Speciality Card
@Composable
fun SpecialtyCard(specialty: String, imageRes: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Card(
            modifier = Modifier.size(70.dp),
            shape = CircleShape,
            backgroundColor = Color(0xFFB39DDB),
            elevation = 14.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = specialty,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = specialty, fontSize = 12.sp, color = Color.Black)
    }
}

// Composable for Doctor Card
@Composable
fun DoctorCard(name: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(150.dp)
            .padding(8.dp)
            .clickable { onClick() }, // Clickable modifier
        shape = RoundedCornerShape(8.dp),
        elevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Image(
                painter = painterResource(R.drawable.doc),
                contentDescription = "Doctor Image",
                modifier = Modifier.size(100.dp)
            )
            Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = "Specialist", fontSize = 12.sp, color = Color.Gray)
        }
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
            .height(50.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.LightGray)
            .shadow(18.dp, shape = RoundedCornerShape(22.dp)),
        placeholder = {
            Text("Search doctors, specialties...", color = Color.Gray)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = Color.Gray
            )
        },
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color(0xD8FBF7FC),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.Black
        )
    )
}



