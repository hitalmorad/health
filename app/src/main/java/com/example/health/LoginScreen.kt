package com.example.health

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            TopScreen()
            Spacer(modifier = Modifier.height(36.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp)
            ) {
                LoginSection(
                    email = email,
                    password = password,
                    errorMessage = errorMessage,

                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onErrorMessageChange = { errorMessage = it },
                    navController = navController
                )
                Spacer(modifier = Modifier.height(30.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Or continue with",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF64748B))
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SocialMediaLogin(
                            logo = R.drawable.google, text = "Google",
                            modifier = Modifier.weight(1f),
                            onClick = { /*signInWithGoogle(navController)*/ }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        SocialMediaLogin(
                            logo = R.drawable.facebook, text = "Facebook",
                            modifier = Modifier.weight(1f),
                            onClick = { /*signInWithFacebook(navController)*/ }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginSection(
    email: String,
    password: String,
    errorMessage:String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onErrorMessageChange: (String) -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginTextField(label = "Email", value = email, onValueChange = onEmailChange , isPassword = false)
        Spacer(modifier = Modifier.height(15.dp))
        LoginTextField(
            label = "Password",
            value = password,
            onValueChange = onPasswordChange,
            trailing = "Forgot?",
            isPassword = true // Hides password input
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {

                if (email.isEmpty() || password.isEmpty()) {
                    onErrorMessageChange("Please input some text in all fields")
                } else {
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onErrorMessageChange("")
                                navController.navigate("main")
                            } else {
                                onErrorMessageChange("Invalid credentials")
                            }
                        }
                }

            }, // Navigate to com.example.health.HomeScreen
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF86D2F5)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Log In", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Text(text = "Don't have an account? ", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF86D2F5)),
                modifier = Modifier.clickable { navController.navigate("signup") } // Navigate to SignUp
            )
        }
    }
}
@Composable
private fun TopScreen() {
    val uicolor: Color = if (isSystemInDarkTheme()) Color.White else Black
    val backgroundColor = MaterialTheme.colorScheme.onBackground

    Box(
        // modifier = Modifier
        // .fillMaxSize()
        // .background(backgroundColor),

        contentAlignment = Alignment.TopCenter) {
        Image(
            painter = painterResource(id = R.drawable.shape),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(fraction = 0.46f)
            ,

            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier.padding(top = 80.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Icon(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(id = R.string.app_logo),
                modifier = Modifier.size(42.dp),
                tint = uicolor
            )
            Spacer(modifier = Modifier.width(15.dp))

            Column {
                Text(
                    text = stringResource(id = R.string.the_tolet),
                    color = uicolor,
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = stringResource(id = R.string.find_house),
                    color = uicolor,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Text(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .align(Alignment.BottomCenter),
            text = stringResource(id = R.string.login),
            color = uicolor,
            style = MaterialTheme.typography.headlineLarge

        )
    }
}
