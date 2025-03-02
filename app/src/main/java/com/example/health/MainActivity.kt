package com.example.health

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
//import com.example.health.doctor.DoctorAppNav


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
    NavHost(navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }
        composable("main") { MainScreen() }
    }
}


@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(bottomBar = { BottomNavigationBar(navController) }) {

        NavHost(navController, startDestination = "home", Modifier.padding(it)) {
            composable("home") { HomeScreen() }
            composable("profile") { ProfileScreen() }
            composable("notifications") { NotificationsScreen() }
            //composable("doctors") { DoctorAppNav(navController) }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    BottomNavigation(
        backgroundColor = Color(0xFFE1BEE7), // Change to your desired color
        contentColor = Color.White // Default text/icon color
    ){
        val items = listOf(
            "home" to Icons.Default.Home,
            "profile" to Icons.Default.Person,
            "notifications" to Icons.Default.Notifications
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



@Composable
fun ProfileScreen() {
    Text("Profile Screen")
}

@Composable
fun NotificationsScreen() {
    Text("Notifications Screen")
}


