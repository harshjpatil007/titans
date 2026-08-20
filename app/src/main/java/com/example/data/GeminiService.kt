package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.example.model.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = 0.4f,
    @Json(name = "topP") val topP: Float? = 0.95f,
    @Json(name = "topK") val topK: Int? = 40,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 1024
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
    @Json(name = "error") val error: GeminiError? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiError(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "status") val status: String? = null
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    fun isApiKeyConfigured(): Boolean {
        val key = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.startsWith("placeholder", ignoreCase = true)
    }

    suspend fun queryGemini(
        userQuery: String,
        scenario: DisasterScenario,
        userLat: Double,
        userLng: Double,
        userLocationName: String,
        personalRiskScore: Int,
        riskLevel: RiskLevel,
        nearestHospital: EmergencyFacility?,
        nearestShelter: SafePlace?,
        nearestHazard: HazardZone?,
        activeBusesCount: Int,
        language: Language
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (!isApiKeyConfigured()) {
            Log.w(TAG, "Gemini API key is not configured or is default placeholder.")
            return@withContext ""
        }

        val targetLang = when (language) {
            Language.HINDI -> "Hindi (हिंदी)"
            Language.MARATHI -> "Marathi (मराठी)"
            Language.ENGLISH -> "English"
        }

        val systemPrompt = """
            You are 'RakshAI Gemini Emergency Evacuation & Flood Resilience AI' for Nashik District Disaster Management Authority (DDMA).
            Your mission: Provide real-time, compassionate, authoritative, and actionable disaster guidance to citizens affected by heavy monsoon floods, Godavari river inundation, and dam discharges in Nashik.
            
            Current Live Telemetry:
            - Active Disaster Scenario: ${scenario.title} (Severity: ${scenario.hazardSeverity}/100)
            - Citizen Location: $userLocationName (Lat: $userLat, Lng: $userLng)
            - Citizen Risk Score: $personalRiskScore/100 ($riskLevel)
            - Precipitation: ${scenario.rainfallIntensityMmPerHour} mm/h, River Level: +${scenario.riverLevelChangeMeters} m
            - Nearest Safe Shelter: ${nearestShelter?.name ?: "KTHM College Relief Shelter"} (${nearestShelter?.distanceKm ?: 0.9} km, Elevation: ${nearestShelter?.safeZoneElevationMeters ?: 615}m)
            - Nearest Hospital: ${nearestHospital?.name ?: "Nashik Civil Hospital"} (${nearestHospital?.availableBeds ?: 45} emergency beds available)
            - Evacuation Transit: $activeBusesCount CityLink evacuation buses active in grid.
            
            Response Rules:
            1. Respond in $targetLang.
            2. Be direct, clear, highly structured (using bullet points and bold highlights).
            3. Prioritize safety: suggest moving to higher ground, avoiding submerged bridges along the Godavari, taking designated high-ground routes, and using the in-app SOS button if trapped.
            4. Keep responses concise (under 180 words) for fast emergency reading on mobile devices.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = userQuery)),
                    role = "user"
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.4f,
                maxOutputTokens = 800
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            )
        )

        try {
            val response = api.generateContent(apiKey = apiKey, request = request)
            val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!candidateText.isNullOrBlank()) {
                candidateText.trim()
            } else {
                val errorMsg = response.error?.message ?: "No candidate generated"
                Log.e(TAG, "Gemini API empty response or error: $errorMsg")
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking Gemini API: ${e.message}", e)
            ""
        }
    }
}
