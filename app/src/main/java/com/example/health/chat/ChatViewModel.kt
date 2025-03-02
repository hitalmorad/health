package com.example.health.chat

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import java.util.Locale

class ChatViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    val messageList = mutableStateListOf<MessageModel>()
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var speechResultCallback: ((String) -> Unit)? = null

    init {
        // Initialize Text-To-Speech
        tts = TextToSpeech(application.applicationContext, this)

        // Initialize Speech Recognizer
        if (SpeechRecognizer.isRecognitionAvailable(application.applicationContext)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(application.applicationContext)
        } else {
            Log.e("SpeechRecognizer", "Speech recognition not available on this device.")
        }

        setupSpeechRecognizer()
    }

    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-pro",
        apiKey = Constants.apiKey
    )

    @RequiresApi(35)
    fun sendMessage(question: String) {
        viewModelScope.launch {
            try {
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role) { text(it.message) }
                    }.toList()
                )

                messageList.add(MessageModel(question, "user"))
                messageList.add(MessageModel("Typing...", "model"))

                val response = chat.sendMessage(question+"Answer concisely in 50 words or less.")
                var botReply = response.text ?: "I'm sorry, I didn't understand that."

                // Remove special characters
                botReply = botReply.replace(Regex("[^a-zA-Z0-9.,!?\\s]"), "")
                val maxLines = botReply.length
                botReply = botReply.lines().take(maxLines).joinToString("\n")

                // Humanize the response (basic paraphrasing)
                botReply = botReply.replace("I am", "I'm")
                    .replace("do not", "don't")
                    .replace("cannot", "can't")

                messageList.removeLast()
                messageList.add(MessageModel(botReply, "model"))

                speakText(botReply) // Convert bot response to speech

            } catch (e: Exception) {
                messageList.removeLast()
                messageList.add(MessageModel("Something went wrong. Please try again.", "model"))
                Log.e("ChatViewModel", "Error: ${e.message}")
            }
        }
    }

    private fun speakText(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun startListening(onResult: (String) -> Unit) {
        val context = getApplication<Application>().applicationContext

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("SpeechRecognizer", "Microphone permission not granted.")
            return
        }

        speechResultCallback = onResult
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer?.startListening(intent)
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SpeechRecognizer", "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("SpeechRecognizer", "Speech started")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d("SpeechRecognizer", "Speech ended")
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission error"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error"
                }
                Log.e("SpeechRecognizer", "Error: $errorMessage")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull().orEmpty()
                Log.d("SpeechRecognizer", "Recognized: $recognizedText")
                speechResultCallback?.invoke(recognizedText)
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            Log.d("TTS", "TTS Initialized successfully")
        } else {
            Log.e("TTS", "TTS Initialization failed")
        }
    }
    override fun onCleared() {
        super.onCleared()
        stopServices()
    }

    fun stopServices() {
        tts?.stop()
        speechRecognizer?.stopListening()
    }


}

