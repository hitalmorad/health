package com.example.health

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class FoodScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        setContent {
            FoodScreen()
        }
    }
}

@Composable
fun FoodScreen() {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val nutritionData = remember { mutableStateOf<Map<String, String>?>(null) }
    val isLoading = remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap == null) {
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        val uri = saveImageToCache(context, bitmap)
        imageUri.value = uri

        isLoading.value = true
        val base64Image = encodeImageToBase64(bitmap)
        fetchNutritionDetails(base64Image, nutritionData, context) {
            isLoading.value = false
        }
    }


    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📸 Capture Food Image", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { cameraLauncher.launch() }, colors = ButtonDefaults.buttonColors(Color.Green)) {
            Text("Open Camera", color = Color.White)
        }

        imageUri.value?.let { uri ->
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }

        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            NutritionCard(nutritionData.value)
        }
    }
}

@Composable
fun NutritionCard(nutritionInfo: Map<String, String>?) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🍽 Nutrition Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            nutritionInfo?.let { data ->
                data.forEach { (key, value) ->
                    Text("$key: $value", fontSize = 14.sp, modifier = Modifier.padding(2.dp))
                }
            } ?: Text("📊 Nutrition details will appear here after analysis.")
        }
    }
}

fun saveImageToCache(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "captured_image.jpg")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
    return file.toUri()
}

fun encodeImageToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
    return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)
}

fun fetchNutritionDetails(
    base64Image: String,
    nutritionData: MutableState<Map<String, String>?>,
    context: Context,
    onComplete: () -> Unit
) {
    val py = Python.getInstance()
    val pyObject = py.getModule("v2")

    val result = pyObject.callAttr("process_food_image", base64Image).toString()

    try {
        val parsedData = result.split(",").associate {
            val parts = it.split(":")
            parts[0].trim() to parts[1].trim()
        }
        nutritionData.value = parsedData
    } catch (e: Exception) {
        Toast.makeText(context, "Error parsing data", Toast.LENGTH_SHORT).show()
    } finally {
        onComplete()
    }
}