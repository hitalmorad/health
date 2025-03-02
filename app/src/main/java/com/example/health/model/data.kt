package com.example.health.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.health.api.Doctor
import com.example.health.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DoctorViewModel : ViewModel() {

    private val _doctors = MutableStateFlow<List<Doctor>>(emptyList()) // ✅ Use MutableStateFlow
    val doctors: StateFlow<List<Doctor>> = _doctors.asStateFlow() // ✅ Expose as StateFlow

    var isLoading = MutableStateFlow(true) // ✅ Changed to MutableStateFlow
        private set
    var errorMessage = MutableStateFlow<String?>(null)
        private set

    private val specialties = listOf("orthopedic", "dentist", "neurologist", "cardiologist")

    fun fetchAllDoctors() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                isLoading.value = true
                val allDoctors = mutableListOf<Doctor>()

                specialties.map { specialty ->
                    async {
                        val url = "https://api.apify.com/v2/datasets/TIOYBdV6r78zHFq00/items"
                        val response = RetrofitClient.apiService.getDoctors(url) // ✅ Corrected API call
                        allDoctors.addAll(response)
                    }
                }.forEach { it.await() }

                _doctors.value = allDoctors // ✅ Update StateFlow after API calls
                isLoading.value = false
            } catch (e: Exception) {
                errorMessage.value = "Failed to load doctors: ${e.message}"
                isLoading.value = false
            }
        }
    }
}
