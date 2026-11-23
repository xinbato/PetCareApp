package com.example.petcare.utils

import android.graphics.Bitmap
import android.util.Base64
import com.example.petcare.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class PetScanResult(
    val species: String = "Chó",
    val breed: String = "",
    val estimatedWeight: String = "5.0 kg",
    val description: String = "",
)

object GeminiPetScanner {

    // API Key duoc inject tu dong qua BuildConfig (doc tu local.properties hoac bien moi truong).
    // File local.properties da duoc them vao .gitignore nen tuyet doi an toan khi push len repository public.
    fun getApiKey(): String = BuildConfig.GEMINI_API_KEY

    // Su dung gemini-3.5-flash-lite: sieu nhanh, tiet kiem token nhat, ho tro nhan dien anh
    private const val MODEL_NAME = "gemini-3.5-flash-lite"
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    suspend fun analyzePetImage(
        bitmap: Bitmap,
        apiKey: String = getApiKey(),
    ): Result<PetScanResult> = withContext(Dispatchers.IO) {
        runCatching {
            if (apiKey.isBlank() || apiKey == "YOUR_GEMINI_API_KEY") {
                throw IllegalStateException("Chưa cấu hình GEMINI_API_KEY. Vui lòng thêm 'GEMINI_API_KEY=...' vào file local.properties.")
            }

            val scaledBitmap = scaleDownBitmap(bitmap, maxDimension = 1024)
            val base64Image = bitmapToBase64(scaledBitmap)

            val endpoint = "$API_URL/$MODEL_NAME:generateContent?key=$apiKey"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            val promptText = "Ban la chuyen gia giam dinh thu cung. Hay nhan dien con vat trong anh. " +
                "Tra ve duy nhat mot chuoi JSON hop le (tuyet doi khong co markdown hoac backticks) voi cac khoa sau: " +
                "\"species\" (loai con vat: Chó, Mèo, Thỏ hoac ten loai khac), " +
                "\"breed\" (ten giong cu the, vi du: Golden Retriever, Meo Anh long ngan, Poodle...), " +
                "\"estimatedWeight\" (can nang tieu chuan kem chu kg, vi du: 12.0 kg), " +
                "\"description\" (dac tinh tinh cach va luu y cham soc trong 1 den 2 cau)."

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", promptText) })
                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mime_type", "image/jpeg")
                                    put("data", base64Image)
                                }
                                put("inline_data", inlineData)
                            })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)
            }

            connection.outputStream.use { os ->
                val inputBytes = requestJson.toString().toByteArray(Charsets.UTF_8)
                os.write(inputBytes, 0, inputBytes.size)
            }

            val responseCode = connection.responseCode
            val inputStream: InputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                val errText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                val friendlyMessage = parseErrorMessage(responseCode, errText)
                throw IllegalStateException(friendlyMessage)
            }

            val responseBody = inputStream.bufferedReader().use { it.readText() }
            parseGeminiResponse(responseBody)
        }
    }

    private fun parseGeminiResponse(jsonString: String): PetScanResult {
        val root = JSONObject(jsonString)
        val candidates = root.optJSONArray("candidates")
            ?: throw IllegalStateException("Không nhận được phản hồi từ AI.")

        if (candidates.length() == 0) {
            throw IllegalStateException("AI không tìm thấy nội dung hợp lệ.")
        }

        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content")
            ?: throw IllegalStateException("Cấu trúc kết quả không hợp lệ.")

        val parts = content.optJSONArray("parts")
            ?: throw IllegalStateException("Không có dữ liệu văn bản trả về.")

        val rawText = parts.getJSONObject(0).optString("text", "").trim()
        val cleanedJson = cleanJsonString(rawText)

        val resultJson = JSONObject(cleanedJson)
        return PetScanResult(
            species = resultJson.optString("species", "Chó"),
            breed = resultJson.optString("breed", "Không xác định"),
            estimatedWeight = resultJson.optString("estimatedWeight", "5.0 kg"),
            description = resultJson.optString("description", "Không có thông tin chi tiết."),
        )
    }

    private fun parseErrorMessage(code: Int, rawJson: String): String {
        val jsonMsg = runCatching {
            JSONObject(rawJson).optJSONObject("error")?.optString("message", "")
        }.getOrNull().orEmpty()

        return when {
            rawJson.contains("API_KEY_INVALID", ignoreCase = true) ||
                jsonMsg.contains("API key not valid", ignoreCase = true) -> {
                "API Key không hợp lệ. Vui lòng kiểm tra lại GEMINI_API_KEY trong file local.properties (Lưu ý: API Key của Google AI Studio luôn bắt đầu bằng 'AIzaSy...')."
            }
            code == 429 || rawJson.contains("RESOURCE_EXHAUSTED", ignoreCase = true) -> {
                "Đã vượt quá giới hạn lượt dùng (Quota) của API Key. Vui lòng thử lại sau giây lát."
            }
            jsonMsg.isNotBlank() -> "Lỗi từ Google AI (Mã $code): $jsonMsg"
            else -> "Lỗi từ Google AI (Mã $code): $rawJson"
        }
    }

    private fun cleanJsonString(raw: String): String {
        var text = raw
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json")
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```")
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```")
        }
        return text.trim()
    }

    private fun scaleDownBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
