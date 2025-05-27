package com.example.health.api


import com.example.health.Dao.DoctorDao
import com.example.health.Dao.toDoctor
import com.example.health.Dao.toEntity
import com.google.gson.GsonBuilder
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST


interface DoctorApiService {
    @GET("38694916-1b33-4930-8426-0465c61ac090")
    suspend fun getAllDoctors(): List<Doctor>
}
object RetrofitClient {
    private const val BASE_URL = "https://run.mocky.io/v3/" // Replace with your API base URL

    val doctorApiService: DoctorApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
            .create(DoctorApiService::class.java)
    }
}

class DoctorRepository(private val doctorDao: DoctorDao, private val apiService: DoctorApiService) {

    // Fetch doctors from API and store in Room
    suspend fun fetchAndStoreDoctors(): Result<Unit> {
        return try {
            val doctors = apiService.getAllDoctors()
            val doctorEntities = doctors.map { it.toEntity() }
            doctorDao.clearAll() // Clear old data
            doctorDao.insertAll(doctorEntities) // Store new data
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get doctors from Room database
    suspend fun getAllDoctors(): List<Doctor> {
        return doctorDao.getAllDoctors().map { it.toDoctor() }
    }
}



interface FoodAnalyzerApi {
    @Headers("Content-Type: application/json")
    @POST("v1/models/gemini-1.5-flash:generateContent?key=AIzaSyC0lP_oHp_4jfcvVqBM_RznF8x1QBGNJZw")
    suspend fun analyzeFood(@Body request: GeminiRequest): Response<GeminiResponse>

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/"

        fun create(): FoodAnalyzerApi {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(FoodAnalyzerApi::class.java)
        }
    }
}

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val inlineData: InlineData? = null, // For Image Input
    val text: String? = null // For Prompt Input
)

data class InlineData(
    val mimeType: String = "image/jpeg",
    val data: String // Base64-encoded image
)

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: ContentResponse
)

data class ContentResponse(
    val parts: List<TextPart>
)

data class TextPart(
    val text: String // Gemini API returns structured text
)
