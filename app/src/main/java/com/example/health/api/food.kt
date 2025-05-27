package com.example.health.api

import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.io.File

class FoodRepository(private val api: FoodAnalyzerApi) {

    suspend fun analyzeFood(imageFile: File): FoodAnalysisResponse? {
        return try {
            val base64Image = encodeImageToBase64(imageFile)

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = """
                     You are an AI food analyzer. Identify the food items present in the given image and provide their nutritional values. 
                    Return a **valid JSON** object only, with the following format:
                    
                    {
                        "food_items": ["apple", "banana", "bread"],  
                        "nutrition": {
                            "calories": (int, kcal),
                            "protein": (float, grams),
                            "carbs": (float, grams),
                            "fats": (float, grams)
                        }
                    }
                    
                    Do not include any extra text or explanation—just return the JSON object.
                """.trimIndent()),
                            Part(inlineData = InlineData(data = base64Image))
                        )
                    )
                )
            )

            val response = api.analyzeFood(request)
            if (response.isSuccessful) {
                val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                Log.d("FoodRepository", "Raw API Response: $rawText")
                rawText?.let { return parseFoodAnalysis(it) }
            } else {
                Log.e("FoodRepository", "Error: ${response.errorBody()?.string()}")
            }
            null
        } catch (e: Exception) {
            Log.e("FoodRepository", "Exception: ${e.message}")
            null
        }
    }

    private fun encodeImageToBase64(file: File): String {
        val bytes = file.readBytes()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun parseFoodAnalysis(responseText: String): FoodAnalysisResponse {
        return try {
            // Trim unnecessary whitespace and newlines
            val cleanJson = responseText.trim()

            // Check if the response is enclosed in triple backticks (```json ... ```)
            val jsonContent = if (cleanJson.startsWith("```json") && cleanJson.endsWith("```")) {
                cleanJson.substring(7, cleanJson.length - 3).trim()  // Remove ```json ... ```
            } else {
                cleanJson
            }

            // Convert the cleaned response to a JSON Object
            val jsonObject = JSONObject(jsonContent)

            val foodItems = jsonObject.optJSONArray("food_items")?.let { array ->
                List(array.length()) { array.getString(it) }
            } ?: emptyList()

            val nutrition = jsonObject.optJSONObject("nutrition") ?: JSONObject()

            val nutritionInfo = NutritionInfo(
                calories = nutrition.optInt("calories", 0),
                protein = nutrition.optDouble("protein", 0.0),
                carbs = nutrition.optDouble("carbs", 0.0),
                fats = nutrition.optDouble("fats", 0.0)
            )

            FoodAnalysisResponse(foodItems, nutritionInfo)
        } catch (e: Exception) {
            Log.e("FoodRepository", "JSON Parsing Error: ${e.message}")
            FoodAnalysisResponse(emptyList(), NutritionInfo(0, 0.0, 0.0, 0.0))
        }
    }

}

data class FoodAnalysisResponse(val foodItems: List<String>, val nutritionInfo: NutritionInfo)
data class NutritionInfo(val calories: Int, val protein: Double, val carbs: Double, val fats: Double)
