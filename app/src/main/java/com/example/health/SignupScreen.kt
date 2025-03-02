package com.example.health

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun SignUpScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val cloudinary = CloudinaryConfig.cloudinary
    val context = LocalContext.current


    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }


    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf("") }
    val backgroundPainter: Painter = painterResource(id = R.drawable.bk1)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Box(

        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = backgroundPainter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(-1f),
            contentScale = ContentScale.FillBounds
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Picker
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    //.background(Color.Gray)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                selectedImageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Selected Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.size(100.dp),
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Input Fields
            SignUpTextField(value = name, onValueChange = { name = it }, label = "Full Name")
            SignUpTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number")
            SignUpTextField(value = age, onValueChange = { age = it }, label = "Age")
            SignUpTextField(value = email, onValueChange = { email = it }, label = "Email Address")
            SignUpTextField(value = password, onValueChange = { password = it }, label = "Password", isPassword = true)
            SignUpTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirm Password", isPassword = true)

            Spacer(modifier = Modifier.height(10.dp))

            // Error Message (if any)


            Spacer(modifier = Modifier.height(20.dp))

            // Sign Up Button with Validations
            val coroutineScope = rememberCoroutineScope()

            Button(
                onClick = {
                    if (validateInput(name, phone, age, email, password, confirmPassword)) {
                        coroutineScope.launch {
                            isUploading = true
                            uploadMessage = ""

                            signUpWithImage(
                                context = context,
                                cloudinary = cloudinary,
                                firestore = firestore,
                                auth = auth,
                                imageUri = selectedImageUri,
                                username = name.trim(),
                                email = email.trim(),
                                password = password.trim(),
                                phoneNumber = phone.trim(),
                                age = age.toInt(),
                                onSignupSuccess = {
                                    isUploading = false
                                    uploadMessage = "Signup successful!"
                                    navController.navigate("main")  // Ensure navigation happens on main thread
                                },
                                onSignupError = { error: String? ->
                                    isUploading = false
                                    uploadMessage = error ?: "Error during signup."
                                }
                            )
                        }
                    } else {
                        uploadMessage = "Please fill all fields, ensure passwords match, and select an image."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF86D2F5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Sign Up", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Redirect to Login
            Row {
                Text(text = "Already have an account? ", color = Color.Gray)
                Text(
                    text = "Sign In",
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate("login") }
                )
            }
        }
    }
}
fun signUpWithImage(
    context: Context,
    cloudinary: Cloudinary,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth,
    imageUri: Uri?,
    username: String,
    email: String,
    password: String,
    phoneNumber: String,
    age: Int,
    onSignupSuccess: () -> Unit,
    onSignupError: (String?) -> Unit
) {
    val inputStream = imageUri?.let { getFilePathFromUri(context, it) }

    if (inputStream != null) {
        val options = ObjectUtils.asMap(
            "folder", "health",
            "public_id", "users/${System.currentTimeMillis()}"
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uploadResult = cloudinary.uploader().upload(inputStream, options)
                val imageUrl = uploadResult["secure_url"] as String

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val userId = result.user?.uid ?: return@addOnSuccessListener
                        val userData = hashMapOf(
                            "username" to username,
                            "email" to email,
                            "profileImage" to imageUrl,
                            "phoneNumber" to phoneNumber,
                            "age" to age
                        )
                        firestore.collection("users").document(userId).set(userData)
                            .addOnSuccessListener {
                                CoroutineScope(Dispatchers.Main).launch {
                                    onSignupSuccess()  // Runs on Main Thread
                                }
                            }
                            .addOnFailureListener { exception ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    onSignupError(exception.message)
                                }
                            }
                    }
                    .addOnFailureListener { exception ->
                        CoroutineScope(Dispatchers.Main).launch {
                            onSignupError(exception.message)
                        }
                    }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    onSignupError("Cloudinary upload failed: ${e.message}")
                }
            }
        }
    } else {
        onSignupError("Failed to retrieve image data.")
    }
}


fun validateInput(name: String, phone: String, age: String, email: String, password: String, confirmPassword: String): Boolean {
    if (name.isEmpty() || phone.isEmpty() || age.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
        return false
    }
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return false
    }
    if (password.length < 6) {
        return false
    }
    if (password != confirmPassword) {
        return false
    }
    return true
}



private fun getFilePathFromUri(context: Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    return try {
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        }
    } catch (e: Exception) {
        null
    }
}
