package com.example.health

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HealthActivity : ComponentActivity() {

    private var reportText by mutableStateOf("")
    private var resultText by mutableStateOf("")
    private var isAnalyzing by mutableStateOf(false)
    private var isUploading by mutableStateOf(false)
    private var uploadProgress by mutableStateOf(0f) // Progress from 0 to 1
    private var selectedFileName by mutableStateOf("Document.pdf")

    // Parsed sections
    private var keyFindings by mutableStateOf<List<String>>(emptyList())
    private var attentions by mutableStateOf<List<String>>(emptyList())
    private var suggestions by mutableStateOf<List<String>>(emptyList())

    private val filePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                lifecycleScope.launch {
                    isUploading = true
                    uploadProgress = 0f
                    selectedFileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Document.pdf"

                    // Simulate upload progress
                    while (uploadProgress < 1f) {
                        delay(100L)
                        uploadProgress = (uploadProgress + 0.1f).coerceAtMost(1f)
                    }

                    reportText = extractTextBasedOnType(uri)
                    resultText = ""
                    keyFindings = emptyList()
                    attentions = emptyList()
                    suggestions = emptyList()
                    isUploading = false
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext)

        setContent {
            MaterialTheme {
                HealthAppUI(
                    onPickFile = { pickFile() },
                    onAnalyze = {
                        lifecycleScope.launch {
                            isAnalyzing = true
                            resultText = withContext(Dispatchers.IO) {
                                GeminiAnalyzer.analyzeReport(reportText)
                            }
                            parseAndSetResults(resultText)
                            isAnalyzing = false
                        }
                    },
                    reportText = reportText,
                    resultText = resultText,
                    keyFindings = keyFindings,
                    attentions = attentions,
                    suggestions = suggestions,
                    isAnalyzing = isAnalyzing,
                    isUploading = isUploading,
                    uploadProgress = uploadProgress,
                    selectedFileName = selectedFileName,
                    onCancelUpload = {
                        isUploading = false
                        uploadProgress = 0f
                    }
                )
            }
        }
    }

    private fun parseAndSetResults(raw: String) {
        val cleaned = raw.replace("*", "").trim()
        val keyTitle = "Key Findings"
        val attentionTitle = "Attention"
        val suggestionTitle = "Suggestions for Improvement"

        var findingsText = ""
        var attentionText = ""
        var suggestionText = ""

        val regex = Regex("""\d+\.\s*(.+?)\n(.*?)(?=\n\d+\.|$)""", RegexOption.DOT_MATCHES_ALL)
        val matches = regex.findAll(cleaned)

        for (match in matches) {
            val sectionTitle = match.groupValues[1].trim()
            val sectionBody = match.groupValues[2].trim()

            when {
                sectionTitle.contains(keyTitle, ignoreCase = true) -> findingsText = sectionBody
                sectionTitle.contains(attentionTitle, ignoreCase = true) -> attentionText = sectionBody
                sectionTitle.contains(suggestionTitle, ignoreCase = true) -> suggestionText = sectionBody
            }
        }

        keyFindings = findingsText.lines()
            .map { it.trim().removePrefix("-").trim() }
            .filter { it.isNotEmpty() }

        attentions = attentionText.lines()
            .map { it.trim().removePrefix("-").trim() }
            .filter { it.isNotEmpty() }

        suggestions = suggestionText.lines()
            .map { it.trim().removePrefix("-").trim() }
            .filter { it.isNotEmpty() }
    }

    @Composable
    fun HealthAppUI(
        onPickFile: () -> Unit,
        onAnalyze: () -> Unit,
        reportText: String,
        resultText: String,
        keyFindings: List<String>,
        attentions: List<String>,
        suggestions: List<String>,
        isAnalyzing: Boolean,
        isUploading: Boolean,
        uploadProgress: Float,
        selectedFileName: String,
        onCancelUpload: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            if (resultText.isBlank() && reportText.isBlank() && !isUploading) {
                // Show Upload UI when no response is generated and no file is being uploaded
                UploadFileUI(onPickFile = onPickFile)
            } else if (isUploading || isAnalyzing) {
                // Show Loader UI during upload or analysis
                FileUploadLoader(
                    progress = uploadProgress,
                    fileName = selectedFileName,
                    onCancel = onCancelUpload
                )
            } else {
                // Full UI when a file is selected or analysis is complete
                Column(
                    modifier = Modifier.fillMaxSize().padding(0.dp, 20.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Health Report Analyzer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color(0xFF1A3C6D)
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = onPickFile,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3C6D))
                        ) {
                            Text("Choose File", color = Color.White, fontSize = 16.sp)
                        }

                        if (reportText.isNotBlank()) {
                            Button(
                                onClick = onAnalyze,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A3C6D)),
                                enabled = !isAnalyzing
                            ) {
                                Text("Analyze Report", color = Color.White, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (resultText.isNotBlank()) {
                        Text(
                            text = "Analysis Results",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A3C6D)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            if (keyFindings.isNotEmpty()) {
                                item {
                                    AnalysisCard(title = "Key Findings", items = keyFindings)
                                }
                            }
                            if (attentions.isNotEmpty()) {
                                item {
                                    AnalysisCard(title = "Attention", items = attentions)
                                }
                            }
                            if (suggestions.isNotEmpty()) {
                                item {
                                    AnalysisCard(title = "Suggestions for Improvement", items = suggestions)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun UploadFileUI(onPickFile: () -> Unit) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE3D7FF),
                                    Color(0xFFDDEEFF),
                                    Color(0xFFD7F4FF)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "UPLOAD YOUR FILES",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF333333)
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "FILE SHOULD BE JPG/PNG/PDF",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFF999999),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Upload Icon",
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Drag & Drop your files or ",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            color = Color(0xFF666666)
                                        )
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onPickFile,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Browse File",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun FileUploadLoader(
        progress: Float,
        fileName: String,
        onCancel: () -> Unit
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE3D7FF),
                                    Color(0xFFDDEEFF),
                                    Color(0xFFD7F4FF)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "UPLOADING FILE",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF333333)
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "File Icon",
                                    tint = Color(0xFF666666),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = fileName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp,
                                            color = Color(0xFF333333)
                                        ),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                            color = Color(0xFF666666)
                                        )
                                    )
                                }
                            }
                            IconButton(
                                onClick = onCancel,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFFF4444))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel Upload",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF3B82F6),
                            trackColor = Color(0xFFD1D5DB)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun AnalysisCard(title: String, items: List<String>) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A3C6D)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                items.forEach { item ->
                    Text(
                        text = "• $item",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF333333)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/jpeg", "image/png"))
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePicker.launch(intent)
    }

    private suspend fun extractTextBasedOnType(uri: Uri): String {
        val mimeType = contentResolver.getType(uri)
        return when {
            mimeType == "application/pdf" -> extractTextFromPdf(uri)
            mimeType?.startsWith("image/") == true -> extractTextFromImage(uri)
            else -> withContext(Dispatchers.IO) {
                contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: "Unable to read file"
            }
        }
    }

    private suspend fun extractTextFromPdf(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            contentResolver.openInputStream(uri).use { inputStream ->
                val document = PDDocument.load(inputStream)
                val text = PDFTextStripper().getText(document)
                document.close()
                text
            } ?: "Failed to open PDF file"
        } catch (e: Exception) {
            "Failed to read PDF: ${e.message}"
        }
    }

    private suspend fun extractTextFromImage(uri: Uri): String = suspendCoroutine { cont ->
        val image = InputImage.fromFilePath(this, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                cont.resume(visionText.text)
            }
            .addOnFailureListener { e ->
                cont.resume("Failed to read image: ${e.message}")
            }
    }

    object GeminiAnalyzer {
        private const val API_KEY = "API_KEY" // Replace with your actual Gemini API key
        private const val GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$API_KEY"

        fun analyzeReport(report: String): String {
            return try {
                val prompt = """
                    Analyze this blood report and return the result in this format:

                    1. Key Findings (bullet points)
                    2. Attention (only if needed, like low sugar)
                    3. Suggestions for improvement

                    Report:
                    $report
                """.trimIndent()

                val json = JSONObject()
                val content = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }
                json.put("contents", JSONArray().put(content))

                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()

                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(GEMINI_URL).post(body).build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return "Gemini API Error: ${response.code} - ${response.message}\n${response.body?.string()}"
                    }

                    val responseBody = response.body?.string() ?: return "Empty response"
                    val jsonResponse = JSONObject(responseBody)

                    jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                }

            } catch (e: Exception) {
                "Failed to analyze report: ${e.message}"
            }
        }
    }
}



