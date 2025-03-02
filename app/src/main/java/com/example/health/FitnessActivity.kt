package com.example.health

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

const val API_KEY = "dH8MhO2M81FRnG+HEkvQoA==p6AMxWcSPHny6Vyu"
const val BASE_URL = "https://api.api-ninjas.com/v1/exercises?muscle="

class FitnessScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitnessScreen()
        }
    }
}

@Composable
fun FitnessScreen() {
    var selectedSkill by remember { mutableStateOf<String?>(null) }
    val skills = remember { mutableStateListOf<String>() }
    val exercises = remember { mutableStateListOf<Exercise>() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            skills.addAll(fetchMuscleGroups())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        HeroSection()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Choose a Workout",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (selectedSkill == null) {
                SkillsGrid(skills) { skill ->
                    selectedSkill = skill
                    coroutineScope.launch {
                        val fetchedExercises = fetchExercisesForSkill(skill)
                        exercises.clear()
                        exercises.addAll(fetchedExercises)
                    }
                }
            } else {
                ExerciseList(selectedSkill!!, exercises) { selectedSkill = null }
            }
        }
    }
}

// 📌 Hero Section (Workout Banner + Profile Icon)
@Composable
fun HeroSection() {
    Box(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.body),
            contentDescription = "Workout",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

    }
}


// 📌 Fetch all skills
suspend fun fetchMuscleGroups(): List<String> {
    return listOf("biceps", "triceps", "chest", "forearms", "traps", "lats", "cardio")
}

// 📌 Fetch exercises from API-Ninja
suspend fun fetchExercisesForSkill(skill: String): List<Exercise> {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("$BASE_URL$skill")
                .addHeader("X-Api-Key", API_KEY)
                .build()

            val response = client.newCall(request).execute()
            val jsonData = response.body?.string() ?: return@withContext emptyList()

            val jsonArray = JSONArray(jsonData)
            val exercises = mutableListOf<Exercise>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                exercises.add(
                    Exercise(
                        name = obj.getString("name"),
                        type = obj.getString("type"),
                        muscle = obj.getString("muscle"),
                        equipment = obj.getString("equipment"),
                        difficulty = obj.getString("difficulty"),
                        instructions = obj.getString("instructions")
                    )
                )
            }
            exercises
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// 📌 Data class for Exercise
data class Exercise(
    val name: String,
    val type: String,
    val muscle: String,
    val equipment: String,
    val difficulty: String,
    val instructions: String
)

// 📌 Skills Grid (Clickable)
@Composable
fun SkillsGrid(skills: List<String>, onSkillSelected: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(skills) { skill ->
            SkillCard(skill, onSkillSelected)
        }
    }
}

// 📌 Individual Skill Card
@Composable
fun SkillCard(skill: String, onSkillSelected: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSkillSelected(skill) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDEDED)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = getSkillImage(skill)),
                contentDescription = "$skill icon",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = skill.uppercase(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

// Function to get the appropriate skill image
@DrawableRes
fun getSkillImage(skill: String): Int {
    return when (skill.lowercase()) {
        "biceps" -> R.drawable.biceps
        "triceps" -> R.drawable.triceps
        "chest" -> R.drawable.chest
        "forearms" -> R.drawable.forearm
        "traps" -> R.drawable.trap
        "lats" -> R.drawable.lats
        "cardio" -> R.drawable.cardio
        else -> R.drawable.doc// Fallback image
    }
}


// 📌 Exercise List (Fetched from API)
@Composable
fun ExerciseList(skill: String, exercises: List<Exercise>, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = skill.uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "← Back",
                fontSize = 18.sp,
                color = Color.Blue,
                modifier = Modifier.clickable { onBack() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (exercises.isEmpty()) {
            Text(
                text = "Fetching exercises...",
                fontSize = 18.sp,
                color = Color.Gray
            )
        } else {
            LazyColumn {
                items(exercises) { exercise ->
                    ExerciseCard(exercise)
                }
            }
        }
    }
}

// 📌 Exercise Card with Details
@Composable
fun ExerciseCard(exercise: Exercise) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exercise.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Type: ${exercise.type}", fontSize = 16.sp, color = Color.DarkGray)
            Text(text = "Muscle: ${exercise.muscle}", fontSize = 16.sp, color = Color.DarkGray)
            Text(text = "Equipment: ${exercise.equipment}", fontSize = 16.sp, color = Color.DarkGray)
            Text(text = "Difficulty: ${exercise.difficulty}", fontSize = 16.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Instructions:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = exercise.instructions, fontSize = 16.sp, color = Color.DarkGray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFitnessScreen() {
    FitnessScreen()
}
