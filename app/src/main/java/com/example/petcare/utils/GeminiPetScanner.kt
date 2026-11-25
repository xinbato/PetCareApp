package com.example.petcare.utils

import android.graphics.Bitmap
import android.util.Log
import androidx.core.graphics.scale
import com.example.petcare.data.model.SuggestedVaccine
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import kotlin.math.max

data class PetScanResult(
    val name: String = "",
    val species: String = "Chó",
    val breed: String = "",
    val gender: String = "Đực",
    val ageText: String = "",
    val weightKg: Double = 0.0,
    val estimatedWeight: String = "5.0 kg",
    val color: String = "",
    val notes: String = "",
    val description: String = "",
    val suggestedVaccines: List<SuggestedVaccine> = emptyList(),
)

/**
 * Analyzes pet photos and/or natural language descriptions using Firebase AI Logic and Gemini.
 */
object GeminiPetScanner {
    private const val TAG = "GeminiPetScanner"
    private const val MODEL_NAME = "gemini-3.5-flash-lite"

    private val petDraftSchema = Schema.obj(
        properties = mapOf(
            "name" to Schema.string(description = "Suggested friendly Vietnamese pet name if not specified"),
            "species" to Schema.enumeration(
                values = listOf("Chó", "Mèo", "Chim", "Thỏ", "Khác"),
                description = "Pet species in Vietnamese"
            ),
            "breed" to Schema.string(description = "Identified or estimated breed in Vietnamese"),
            "gender" to Schema.enumeration(
                values = listOf("Đực", "Cái"),
                description = "Estimated or stated gender"
            ),
            "ageText" to Schema.string(description = "Estimated or stated age in Vietnamese (e.g., 4 tháng, 2 tuổi)"),
            "weightKg" to Schema.double(description = "Estimated or stated weight in kg"),
            "color" to Schema.string(description = "Coat/fur color in Vietnamese"),
            "notes" to Schema.string(description = "Short care notes or allergies in Vietnamese"),
            "careSummary" to Schema.string(description = "1-2 sentence friendly Vietnamese care advice"),
            "suggestedVaccines" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "vaccineName" to Schema.string(description = "Vaccine name in Vietnamese"),
                        "daysFromNow" to Schema.integer(description = "Recommended days from today (1-365)"),
                        "notes" to Schema.string(description = "Short reason for this vaccine in Vietnamese")
                    )
                ),
                description = "1 to 3 recommended starter or booster vaccinations"
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = petDraftSchema
            }
        )
    }

    suspend fun analyzePet(
        bitmap: Bitmap?,
        userDescription: String = "",
    ): Result<PetScanResult> {
        return try {
            require(bitmap != null || userDescription.isNotBlank()) {
                "Vui lòng chụp/chọn ảnh hoặc nhập mô tả về thú cưng."
            }

            val scaledBitmap = bitmap?.let { scaleDownBitmap(it, maxDimension = 768) }
            val promptText = buildString {
                appendLine("Bạn là bác sĩ thú y và trợ lý chăm sóc thú cưng tại Việt Nam.")
                appendLine("Hãy phân tích hình ảnh (nếu có) và/hoặc mô tả của người dùng để trích xuất thông tin hồ sơ thú cưng bằng tiếng Việt tự nhiên.")
                appendLine("Nếu thiếu tên, hãy gợi ý một tên dễ thương phù hợp. Nhận diện loài, giống, màu lông, ước lượng độ tuổi và cân nặng kg hợp lý.")
                appendLine("Đề xuất 1-3 mũi tiêm phòng phù hợp với loài và độ tuổi.")
                if (userDescription.isNotBlank()) {
                    appendLine("Mô tả từ người dùng: ${userDescription.trim()}")
                }
            }

            val requestContent = content {
                if (scaledBitmap != null) {
                    image(scaledBitmap)
                }
                text(promptText)
            }

            val response = withTimeout(60_000L) {
                model.generateContent(requestContent)
            }

            val jsonText = response.text
                ?: return Result.failure(IllegalStateException("AI không trả về kết quả."))
            Result.success(parseScanResultJson(jsonText))
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "[ERROR] Firebase AI request timed out", e)
            Result.failure(IllegalStateException("Quá thời gian phản hồi từ AI (60s). Vui lòng thử lại."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "[ERROR] Firebase AI pet analysis failed", e)
            Result.failure(
                IllegalStateException(
                    mapAiErrorMessage(e.localizedMessage ?: e.message)
                )
            )
        }
    }

    suspend fun analyzePetImage(bitmap: Bitmap): Result<PetScanResult> =
        analyzePet(bitmap = bitmap, userDescription = "")

    internal fun isTransientOverloadError(raw: String?): Boolean {
        val msg = raw.orEmpty()
        return msg.contains("high demand", ignoreCase = true) ||
            msg.contains("overloaded", ignoreCase = true) ||
            msg.contains("UNAVAILABLE", ignoreCase = true) ||
            msg.contains("503") ||
            msg.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
            msg.contains("429")
    }

    internal fun mapAiErrorMessage(raw: String?): String {
        val msg = raw.orEmpty()
        return when {
            msg.contains("App Check", ignoreCase = true) ||
                msg.contains("attestation", ignoreCase = true) ->
                "Xác thực Firebase App Check chưa sẵn sàng. Vui lòng kiểm tra cấu hình App Check trên Firebase Console."
            msg.contains("PERMISSION_DENIED", ignoreCase = true) ||
                msg.contains("API has not been used", ignoreCase = true) ->
                "Firebase AI Logic chưa được bật trên Firebase Console cho dự án này."
            msg.contains("high demand", ignoreCase = true) ||
                msg.contains("overloaded", ignoreCase = true) ||
                msg.contains("UNAVAILABLE", ignoreCase = true) ||
                msg.contains("503") ->
                "Máy chủ Gemini đang quá tải tạm thời. Vui lòng thử lại sau vài giây."
            msg.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
                msg.contains("429") ->
                "Đã vượt giới hạn lượt dùng AI tạm thời. Vui lòng thử lại sau."
            msg.isNotBlank() -> msg
            else -> "Không thể phân tích thú cưng bằng AI lúc này."
        }
    }

    private fun scaleDownBitmap(source: Bitmap, maxDimension: Int): Bitmap {
        val largest = max(source.width, source.height)
        if (largest <= maxDimension) return source
        val scale = maxDimension.toFloat() / largest.toFloat()
        val width = (source.width * scale).toInt().coerceAtLeast(1)
        val height = (source.height * scale).toInt().coerceAtLeast(1)
        return source.scale(width, height)
    }

    internal fun parseScanResultJson(jsonText: String): PetScanResult {
        val cleaned = jsonText.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        val candidate = if (start >= 0 && end > start) cleaned.substring(start, end + 1) else cleaned
        val json = runCatching { JSONObject(candidate) }.getOrElse { JSONObject() }

        val vaccinesJson = json.optJSONArray("suggestedVaccines")
        val vaccines = buildList {
            if (vaccinesJson != null) {
                for (i in 0 until vaccinesJson.length()) {
                    val item = vaccinesJson.optJSONObject(i) ?: continue
                    val vName = item.optString("vaccineName").trim()
                    if (vName.isNotEmpty()) {
                        add(
                            SuggestedVaccine(
                                vaccineName = vName,
                                daysFromNow = item.optInt("daysFromNow", 14).coerceIn(1, 365),
                                notes = item.optString("notes", "").trim(),
                                selected = true,
                            )
                        )
                    }
                }
            }
        }

        val rawSpecies = json.optString("species", "Chó").trim()
        val normalizedSpecies = when {
            rawSpecies.contains("Mèo", ignoreCase = true) || rawSpecies.equals("CAT", ignoreCase = true) -> "Mèo"
            rawSpecies.contains("Thỏ", ignoreCase = true) || rawSpecies.equals("RABBIT", ignoreCase = true) -> "Thỏ"
            rawSpecies.contains("Chim", ignoreCase = true) || rawSpecies.equals("BIRD", ignoreCase = true) -> "Chim"
            rawSpecies.contains("Khác", ignoreCase = true) || rawSpecies.equals("OTHER", ignoreCase = true) -> "Khác"
            else -> "Chó"
        }

        val rawGender = json.optString("gender", "Đực").trim()
        val normalizedGender = if (rawGender.contains("Cái", ignoreCase = true) ||
            rawGender.equals("FEMALE", ignoreCase = true)
        ) "Cái" else "Đực"

        val weightKg = json.optDouble("weightKg", 0.0).let {
            if (it.isNaN()) 0.0 else it.coerceIn(0.0, 250.0)
        }
        val weightLabel = if (weightKg > 0.0) String.format(java.util.Locale.US, "%.1f kg", weightKg) else ""

        return PetScanResult(
            name = json.optString("name", "Bé Cưng").trim().ifEmpty { "Bé Cưng" },
            species = normalizedSpecies,
            breed = json.optString("breed", "").trim(),
            gender = normalizedGender,
            ageText = json.optString("ageText", "").trim(),
            weightKg = weightKg,
            estimatedWeight = weightLabel.ifEmpty { "5.0 kg" },
            color = json.optString("color", "").trim(),
            notes = json.optString("notes", "").trim(),
            description = json.optString("careSummary", "").trim()
                .ifEmpty { json.optString("description", "").trim() },
            suggestedVaccines = vaccines,
        )
    }
}
