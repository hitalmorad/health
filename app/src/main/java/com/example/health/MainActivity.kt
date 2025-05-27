package com.example.health

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.example.health.chat.HelpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

object CloudinaryConfig {
    val cloudinary: Cloudinary by lazy {
        Cloudinary(
            mapOf(
                "cloud_name" to "duhc5fja9",
                "api_key" to "597938773129959",
                "api_secret" to "DotZ3z0sHqt3UxDARBwRB9DXBtg"
            )
        )
    }
}

class MainActivity : ComponentActivity() {
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val config = mapOf(
                "cloud_name" to "duhc5fja9",
                "api_key" to "597938773129959",
                "api_secret" to "DotZ3z0sHqt3UxDARBwRB9DXBtg"
            )
            MediaManager.init(this, config)
            Log.d("Cloudinary", "Cloudinary initialized successfully.")
        } catch (e: Exception) {
            Log.e("Cloudinary", "Cloudinary initialization failed: ${e.message}")
        }

        setContent {
            AppNavigator()
        }
    }
}

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) "main" else "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("main") { MainScreen(navController) }
    }
}

@Composable
fun MainScreen(parentNavController: NavController? = null) {
    val navController = rememberNavController()
    Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { HomeScreen(parentNavController) }
            composable("profile") {
                ProfileScreen(onNavigateToLogin = {
                    parentNavController?.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                })
            }
            composable("ai_assistant") { AIAssistantScreen(navController) }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    BottomNavigation(
        backgroundColor = Color(0xFFE1BEE7),
        contentColor = Color.White
    ) {
        val items = listOf(
            "home" to Icons.Default.Home,
            "profile" to Icons.Default.Person,
            "ai_assistant" to Icons.Default.SmartToy
        )
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { (screen, icon) ->
            BottomNavigationItem(
                icon = { Icon(imageVector = icon, contentDescription = screen) },
                label = { Text(screen.replaceFirstChar { it.uppercase() }) },
                selected = currentRoute == screen,
                onClick = {
                    navController.navigate(screen) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun ProfileScreen(onNavigateToLogin: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var profileImageUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }


    // Store initial values to revert if user cancels
    var initialUsername by remember { mutableStateOf("") }
    var initialSurname by remember { mutableStateOf("") }
    var initialEmail by remember { mutableStateOf("") }
    var initialPhoneNumber by remember { mutableStateOf("") }


    var isEditing by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val PoppinsMedium = FontFamily(
        Font(R.font.plight, FontWeight.Medium)
    )

    val MontserratBold = FontFamily(
        Font(R.font.ploopetregular, FontWeight.Bold)
    )

    fun updateUserProfile(document: DocumentSnapshot) {
        profileImageUrl = document.getString("profileImage") ?: ""
        username = document.getString("username") ?: ""

        email = document.getString("email") ?: ""
        phoneNumber = document.getString("phoneNumber") ?: ""

        // Store initial values
        initialUsername = username

        initialEmail = email
        initialPhoneNumber = phoneNumber

    }

    LaunchedEffect(key1 = auth.currentUser) {
        auth.currentUser?.let {
            firestore.collection("users").document(it.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        updateUserProfile(document)
                    }
                }
                .addOnFailureListener {
                    Log.e("ProfileScreen", "Error fetching user data", it)
                    onNavigateToLogin()
                }
        } ?: onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE1BEE7), Color(0xFF90CAF9)),
                    start = Offset(0f, 0f),
                    end = Offset(0f, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Icon(
                    painter = painterResource(id = R.drawable.edit), // Add a pen icon in your drawable
                    contentDescription = "Edit",
                    tint = Color(0xFFFF4081),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { isEditing = true }
                )
            }

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF4081)),
                contentAlignment = Alignment.Center
            ) {
                if (!profileImageUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUrl),
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.prfl),
                        contentDescription = "Default Profile Image",
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Text(
                text = "Hello!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PoppinsMedium, // Apply the Poppins Medium font
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(top = 12.dp)
            )

            Text(
                text = username,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = MontserratBold, // Apply the Montserrat Bold font
                color = Color(0xFFFF4081),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .background(
                        color = Color(0xFFFF4081).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
// Removed the social media icons and added a subtle health-themed divider
            Divider(
                color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                thickness = 2.dp,
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 24.dp)
                    .fillMaxWidth(0.5f) // Short divider for a modern look
            )
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileDetailField("NAME", username, isEditing) { username = it }

                    ProfileDetailField("EMAIL", email, isEditing) { email = it }
                    ProfileDetailField("MOBILE", phoneNumber, isEditing) { phoneNumber = it }

                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier
                        .height(48.dp)
                        .width(120.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Sign Out Button at bottom right
        Button(
            onClick = {
                auth.signOut()
                onNavigateToLogin()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Sign Out", color = Color.White)
        }
    }

    // Save Confirmation Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Update Profile") },
            text = { Text("Are you sure you want to update your details?") },
            confirmButton = {
                Button(
                    onClick = {
                        saveProfileChanges(
                            auth.currentUser?.uid,
                            username,
                            email,
                            phoneNumber

                        )
                        isEditing = false
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Yes", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // Revert to initial values
                        username = initialUsername

                        email = initialEmail
                        phoneNumber = initialPhoneNumber

                        isEditing = false
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("No", color = Color.White)
                }
            },
            shape = RoundedCornerShape(12.dp),
            backgroundColor = Color.White
        )
    }
}

@Composable
fun ProfileDetailField(label: String, value: String, isEditing: Boolean, onValueChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        if (isEditing) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(top = 4.dp)
            )
        } else {
            Text(
                text = value,
                color = Color.Black,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp, modifier = Modifier.padding(top = 8.dp))
    }
}

@OptIn(UnstableApi::class)
fun saveProfileChanges(
    userId: String?,
    username: String,

    email: String,
    phoneNumber: String
) {
    if (userId == null) return

    val firestore = FirebaseFirestore.getInstance()
    val userMap = mapOf(
        "username" to username,
        "email" to email,
        "phoneNumber" to phoneNumber

    )

    firestore.collection("users").document(userId).set(userMap)
        .addOnSuccessListener { Log.d("ProfileScreen", "Profile updated successfully") }
        .addOnFailureListener { e -> Log.e("ProfileScreen", "Error updating profile", e) }
}
@Composable
fun AIAssistantScreen(navController: NavController) {
    val context = LocalContext.current

    // Launcher for starting HelpActivity and handling the result
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // When HelpActivity finishes, navigate to the Home screen
        navController.navigate("home") {
            // Pop up to the home screen to avoid stacking AIAssistantScreen
            popUpTo(navController.graph.startDestinationId) {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    // Launch HelpActivity only once when the composable is first composed
    LaunchedEffect(Unit) {
        val intent = Intent(context, HelpActivity::class.java)
        launcher.launch(intent)
    }
}