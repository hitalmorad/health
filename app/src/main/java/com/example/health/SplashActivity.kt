package com.example.health

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreen()
        }
    }
}

@Composable
fun SplashScreen() {
    var startAnimation by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000), label = "Fade Animation"
    )
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 1000), label = "Scale Animation"
    )

    // Navigate to MainActivity after 3 seconds
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000) // 3-second delay
        context.startActivity(Intent(context, MainActivity::class.java))
        (context as? ComponentActivity)?.finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.newsp),
                contentDescription = "Health Logo",
                modifier = Modifier
                    .size(200.dp)
                    .alpha(alpha)
                    .scale(scale)
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Your Health, Our Priority",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black,
                modifier = Modifier
                    .alpha(alpha)
                    .scale(scale)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Track your wellness and stay healthy with expert guidance.",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                modifier = Modifier
                    .alpha(alpha)
                    .scale(scale)
            )
        }
    }
}