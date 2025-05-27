package com.example.health

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            LoginScreen(navController)
        }
    }
}
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleGoogleSignInResult(
            result = result,
            onSuccess = { user ->
                Toast.makeText(context, "Google Sign-In successful: ${user?.displayName}", Toast.LENGTH_SHORT).show()
                navController.navigate("home")
            },
            onFailure = { exception ->
                Toast.makeText(context, "Google Sign-In failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

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
                            onClick = { signInWithGoogle(context,  launcher) }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        SocialMediaLogin(
                            logo = R.drawable.facebook, text = "Facebook",
                            modifier = Modifier.weight(1f),
                            onClick = { /*signInWithFacebook(context, navController)*/ }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(UnstableApi::class)
fun handleGoogleSignInResult(
    result: ActivityResult,
    onSuccess: (FirebaseUser?) -> Unit,
    onFailure: (Exception) -> Unit
) {
    if (result.resultCode == Activity.RESULT_OK) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        onSuccess(authTask.result?.user)
                    } else {
                        onFailure(authTask.exception ?: Exception("Firebase authentication failed"))
                    }
                }
        } catch (e: ApiException) {
            onFailure(e)
        }
    } else {
        onFailure(Exception("Google Sign-In canceled or failed"))
    }
}

@OptIn(UnstableApi::class)
fun signInWithGoogle(
    context: Context,
    launcher: ActivityResultLauncher<Intent>
) {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
    if (resultCode != ConnectionResult.SUCCESS) {
        val errorMsg = when (resultCode) {
            ConnectionResult.SERVICE_MISSING -> "Google Play Services missing"
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> "Google Play Services update required"
            ConnectionResult.SERVICE_DISABLED -> "Google Play Services disabled"
            else -> "Google Play Services error: $resultCode"
        }
        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        return
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    val signInIntent = googleSignInClient.signInIntent
    launcher.launch(signInIntent)
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
            isPassword = true// Hides password input
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

// ------------------------------- sign in with Google ------------------------

    fun signInWithGoogle(
    ){
    }

    fun handleGoogleSignInResult(
        result: ActivityResult,
        onSuccess: (FirebaseUser?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            onSuccess(authTask.result?.user)
                        } else {
                            onFailure(authTask.exception ?: Exception("Google Sign-In failed"))
                        }
                    }
            } catch (e: ApiException) {
                onFailure(e)
            }
        } else {
            onFailure(Exception("Google Sign-In canceled"))
        }
    }


}
