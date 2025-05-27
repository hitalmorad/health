package com.example.health

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import kotlin.coroutines.resume

class HealthActivity : ComponentActivity() {

    private var reportText by mutableStateOf("")
    private var resultText by mutableStateOf("")

    private val filePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    lifecycleScope.launch {
                        reportText = extractTextBasedOnType(uri)
                        resultText = ""  // Clear previous result when new report loaded
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext) // Initialize PDFBox

        setContent {
            MaterialTheme {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(onClick = { pickFile() }) {
                        Text("Choose Blood Report File")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (reportText.isNotBlank()) {
                        Button(onClick = {
                            lifecycleScope.launch {
                                // Show a loading placeholder while waiting
                                resultText = "Analyzing report..."
                                resultText = GeminiAnalyzer.analyzeReport(reportText)
                            }
                        }) {
                            Text("Analyze Report")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (resultText.isNotBlank()) {
                        Text("Analysis:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(resultText)
                    }
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
                contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
                    ?: "Unable to read file"
            }
        }
    }

    private suspend fun extractTextFromPdf(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            contentResolver.openInputStream(uri).use { inputStream ->
                val document = PDDocument.load(inputStream)
                val stripper = PDFTextStripper()
                val text = stripper.getText(document)
                document.close()
                text
            } ?: "Failed to open PDF file"
        } catch (e: Exception) {
            "Failed to read PDF: ${e.message}"
        }
    }

    private suspend fun extractTextFromImage(uri: Uri): String =
        suspendCancellableCoroutine { cont ->
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
        private const val API_KEY = "AIzaSyBDSnu4frUnCfcKpZeVzn0X9ht6kDpD8dw"
        private const val GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$API_KEY"

        fun analyzeReport(report: String): String {
            val prompt = """
                Analyze this blood report and return the result in this format:

                1. Key Findings (bullet points)
                2. Attention (only if needed, like low sugar)
                3. Suggestions for improvement

                Report:
                $report
            """.trimIndent()

            val json = JSONObject()
            val content = JSONObject().put("parts", listOf(JSONObject().put("text", prompt)))
            json.put("contents", listOf(content))

            val client = OkHttpClient()
            val body = RequestBody.create("application/json".toMediaType(), json.toString())
            val request = Request.Builder().url(GEMINI_URL).post(body).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return "Error: ${response.message}"
                val responseBody = response.body?.string() ?: return "Empty response"
                val jsonResponse = JSONObject(responseBody)
                return jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            }
        }
    }
}
