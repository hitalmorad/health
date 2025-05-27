package com.example.health

import android.content.Intent
import android.speech.tts.TextToSpeech
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.health.reminder.ReminderActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun HomeScreen(parentNavController: NavController? = null) {
    val context = LocalContext.current
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    // State for user data
    val userName = remember { mutableStateOf("User") }
    val userImageUrl = remember { mutableStateOf<String?>(null) }

    // Fetch user data from Firestore
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        println(uid + "this is user id")
        if (uid != null) {
            try {
                val document = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .await()
                if (document.exists()) {
                    println("it is exists")
                    userName.value = document.getString("username") ?: "User"
                    userImageUrl.value = document.getString("profileImage")
                } else {
                    Log.e("Firestore", "No user document found for UID: $uid")
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching user data", e)
            }
        } else {
            Log.e("Firestore", "No user is currently signed in")
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
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(userImageUrl = userImageUrl.value)
        Spacer(modifier = Modifier.height(16.dp))
        WelcomeSection(userName = userName.value)
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Button(
            onClick = { speakText(speechText) },
            modifier = Modifier
                .size(66.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(Color(0xF23586C9))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.spk),
                contentDescription = "Speak Icon",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
fun TopBar(userImageUrl: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (userImageUrl != null && userImageUrl.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = userImageUrl,
                    placeholder = painterResource(id = R.drawable.prfl), // Add a placeholder drawable
                    error = painterResource(id = R.drawable.prfl) // Add an error drawable
                ),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(50))
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(50))
            )
        }

        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun WelcomeSection(userName: String) {
    Column {
        Text(text = "Hello,", fontSize = 20.sp, color = Color.Gray)
        Text(
            text = "$userName 👋",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
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
                imageRes = R.drawable.hi,
                color = Color(0xFFB3E5FC),
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                title = "Fitness",
                subtitle = "Workouts & Training",
                imageRes = R.drawable.splashim,
                color = Color(0xFFB2DFDB),
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                title = "Food",
                subtitle = "Nutrition & Diet",
                imageRes = R.drawable.fd,
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
            .clickable {
                when (title) {
                    "Health" -> {
                        val intent = Intent(context, HomeScreenActivity::class.java)
                        context.startActivity(intent)
                    }

                    "Fitness" -> {
                        val intent = Intent(context, FitnessScreenActivity::class.java)
                        context.startActivity(intent)
                    }

                    "Food" -> {
                        val intent = Intent(context, FoodScreenActivity::class.java)
                        context.startActivity(intent)
                    }
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
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
                if (title == "Set Notification") {
                    val intent = Intent(context, ReminderActivity::class.java)
                    context.startActivity(intent)
                } else {
                    val intent = Intent(context, HealthActivity::class.java)
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
                painter = painterResource(id = R.drawable.doc),
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
fun HealthCheckItem(title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
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
                painter = painterResource(id = R.drawable.doc),
                contentDescription = title,
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}