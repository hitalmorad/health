package com.example.health

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (!granted) {
                    Log.w("ForgotPassword", "Notification permission denied")
                }
            }.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            ForgotPasswordNavigation()
        }
    }
}

@Composable
fun ForgotPasswordNavigation() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "enter_phone") {
        composable("enter_phone") {
            EnterPhoneScreen(
                onPhoneVerified = { phone, email ->
                    navController.navigate("otp/$phone/$email")
                },
                onCancel = {
                    navController.context.startActivity(
                        Intent(navController.context, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                    (navController.context as? ComponentActivity)?.finishAffinity()
                }
            )
        }
        composable("otp/{phone}/{email}") { backStackEntry ->
            val phone = backStackEntry.arguments?.getString("phone") ?: ""
            val email = backStackEntry.arguments?.getString("email") ?: ""
            OTPScreen(
                phoneNumber = phone,
                email = email,
                onOtpVerified = {
                    navController.navigate("success")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("success") {
            SuccessScreen(
                onDone = {
                    navController.context.startActivity(
                        Intent(navController.context, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                    (navController.context as? ComponentActivity)?.finishAffinity()
                }
            )
        }
    }
}

@Composable
fun EnterPhoneScreen(onPhoneVerified: (String, String) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Reset Password",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Mobile Number (e.g., 9725263985 or +919725263985)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    coroutineScope.launch {
                        delay(100) // Mitigate IME issues
                        if (phoneNumber.isEmpty()) {
                            errorMessage = "Please enter a mobile number"
                            return@launch
                        }
                        val cleanedNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")
                        if (cleanedNumber.length < 10) {
                            errorMessage = "Please enter a valid mobile number (at least 10 digits)"
                            return@launch
                        }

                        val queryNumber = cleanedNumber.takeLast(10)
                        Log.d("EnterPhone", "Querying Firestore with: $queryNumber")

                        isLoading = true
                        errorMessage = ""

                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .whereEqualTo("phoneNumber", queryNumber)
                            .get()
                            .addOnSuccessListener { documents ->
                                isLoading = false
                                if (documents.isEmpty) {
                                    errorMessage = "This phone number is not registered"
                                } else {
                                    val email = documents.documents.first().getString("email") ?: ""
                                    if (email.isEmpty()) {
                                        errorMessage = "No email associated with this account"
                                    } else {
                                        onPhoneVerified(phoneNumber, email)
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                errorMessage = "Error checking phone number: ${e.message}"
                                Log.e("EnterPhone", "Firestore query failed", e)
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Verify Phone Number", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Cancel",
            color = Color(0xFF86D2F5),
            modifier = Modifier.clickable { onCancel() }
        )
    }
}

@Composable
fun OTPScreen(phoneNumber: String, email: String, onOtpVerified: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var otp by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var verificationId by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        val cleanedNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")
        val formattedPhoneNumber = if (cleanedNumber.startsWith("+")) {
            cleanedNumber
        } else {
            "+91$cleanedNumber"
        }
        Log.d("OTPScreen", "Initiating OTP for: $formattedPhoneNumber")

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                isLoading = false
                Log.d("OTPScreen", "Auto-verification completed")
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            coroutineScope.launch {
                                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                    .addOnCompleteListener { emailTask ->
                                        if (emailTask.isSuccessful) {
                                            FirebaseAuth.getInstance().signOut()
                                            onOtpVerified()
                                        } else {
                                            errorMessage = "Failed to send reset email: ${emailTask.exception?.message}"
                                            Log.e("OTPScreen", "Email send failed", emailTask.exception)
                                        }
                                    }
                            }
                        } else {
                            errorMessage = "Auto-verification failed: ${task.exception?.message}"
                            Log.e("OTPScreen", "Auto-verification failed", task.exception)
                        }
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                isLoading = false
                errorMessage = if (formattedPhoneNumber == "+16505551234") {
                    "Test number detected. Use OTP: 123456"
                } else {
                    "Failed to send OTP: ${e.message}. For test numbers, use +16505551234 with OTP 123456."
                }
                Log.e("OTPScreen", "OTP verification failed", e)
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                isLoading = false
                verificationId = id
                errorMessage = if (formattedPhoneNumber == "+16505551234") {
                    "Test number detected. Use OTP: 123456"
                } else {
                    "OTP sent. Check your SMS."
                }
                Log.d("OTPScreen", "OTP sent, verificationId: $id")
            }
        }

        try {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                formattedPhoneNumber,
                60,
                TimeUnit.SECONDS,
                context as ComponentActivity,
                callbacks
            )
        } catch (e: Exception) {
            isLoading = false
            errorMessage = "Error initiating OTP: ${e.message}"
            Log.e("OTPScreen", "OTP initiation failed", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter OTP",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sent to $phoneNumber",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { otp = it },
            label = { Text("OTP") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    coroutineScope.launch {
                        delay(100) // Mitigate IME issues
                        if (otp.isEmpty()) {
                            errorMessage = "Please enter the OTP"
                            return@launch
                        }
                        if (verificationId.isEmpty()) {
                            errorMessage = "OTP not sent yet. For test numbers, use 123456."
                            return@launch
                        }

                        isLoading = true
                        errorMessage = ""

                        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                        .addOnCompleteListener { emailTask ->
                                            isLoading = false
                                            if (emailTask.isSuccessful) {
                                                FirebaseAuth.getInstance().signOut()
                                                onOtpVerified()
                                            } else {
                                                errorMessage = "Failed to send reset email: ${emailTask.exception?.message}"
                                                Log.e("OTPScreen", "Email send failed", emailTask.exception)
                                            }
                                        }
                                } else {
                                    isLoading = false
                                    errorMessage = "Invalid OTP: ${task.exception?.message}"
                                    Log.e("OTPScreen", "OTP verification failed", task.exception)
                                }
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Verify OTP", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Back",
            color = Color(0xFF86D2F5),
            modifier = Modifier.clickable { onBack() }
        )
    }
}

@Composable
fun SuccessScreen(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Password Reset Initiated!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "A password reset email has been sent. Please check your inbox.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onDone() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Back to Login", color = Color.White)
        }
    }
}