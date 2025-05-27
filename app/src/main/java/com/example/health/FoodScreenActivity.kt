package com.example.health

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.health.api.FoodAnalysisResponse
import com.example.health.api.FoodAnalyzerApi
import com.example.health.api.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FoodScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val foodViewModel = viewModel { FoodViewModel(FoodRepository(FoodAnalyzerApi.create())) }
            FoodScreen(foodViewModel)
        }
    }
}

@Composable
fun FoodScreen(viewModel: FoodViewModel) {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    val analysisResult by viewModel.analysisResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap == null) {
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }

        val uri = saveImageToCache(context, bitmap)
        imageUri.value = uri

        val imageFile = File(uri.path!!)
        viewModel.analyzeFood(imageFile)
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
                painter = rememberAsyncImagePainter(
                    model = uri,
                    // Optionally force Coil to skip cache for this URI
                    // You can uncomment this if unique file names aren't sufficient
                    // placeholder = null,
                    // error = null,
                    // onSuccess = null,
                    // onError = null,
                    // builder = {
                    //     memoryCachePolicy(CachePolicy.DISABLED)
                    //     diskCachePolicy(CachePolicy.DISABLED)
                    // }
                ),
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            AnalysisCard(analysisResult)
        }
    }
}

@Composable
fun AnalysisCard(result: FoodAnalysisResponse?) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🍽 Detected Food Items", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            if (!result?.foodItems.isNullOrEmpty()) {
                result?.foodItems?.forEach { food ->
                    Text("- $food")
                }
            } else {
                Text("🔍 No food items detected.")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("📊 Nutrition Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            result?.nutritionInfo?.let { data ->
                Text("Calories: ${data.calories} kcal")
                Text("Protein: ${data.protein} g")
                Text("Carbs: ${data.carbs} g")
                Text("Fats: ${data.fats} g")
            } ?: Text("📊 Nutrition details will appear here after analysis.")
        }
    }
}

class FoodViewModel(private val repository: FoodRepository) : ViewModel() {
    private val _analysisResult = MutableStateFlow<FoodAnalysisResponse?>(null)
    val analysisResult: StateFlow<FoodAnalysisResponse?> = _analysisResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun analyzeFood(imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _analysisResult.value = repository.analyzeFood(imageFile)
            _isLoading.value = false
        }
    }
}

fun saveImageToCache(context: Context, bitmap: Bitmap): Uri {
    // Generate a unique file name using a timestamp
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(context.cacheDir, "captured_image_$timestamp.jpg")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
    return file.toUri()
}