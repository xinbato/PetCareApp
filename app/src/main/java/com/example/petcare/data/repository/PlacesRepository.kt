package com.example.petcare.data.repository

import com.example.petcare.R
import com.example.petcare.data.model.PetPlaceModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Fetches real veterinary clinics, pet spas, and pet shops via OpenStreetMap Overpass & Nominatim.
 */
class PlacesRepository {

    suspend fun searchNearbyPetFacilities(
        lat: Double,
        lon: Double,
        radiusMeters: Int = 5000,
    ): Result<List<PetPlaceModel>> = withContext(Dispatchers.IO) {
        try {
            val clampedRadius = radiusMeters.coerceIn(1000, 20000)
            val query = """
                [out:json][timeout:15];
                (
                  nwr["amenity"="veterinary"](around:$clampedRadius,$lat,$lon);
                  nwr["shop"="pet"](around:$clampedRadius,$lat,$lon);
                  nwr["craft"="pet_grooming"](around:$clampedRadius,$lat,$lon);
                  nwr["amenity"="animal_boarding"](around:$clampedRadius,$lat,$lon);
                  nwr["amenity"="animal_shelter"](around:$clampedRadius,$lat,$lon);
                );
                out center 50;
            """.trimIndent()

            val overpassPlaces = runCatching {
                val responseJson = try {
                    postOverpass(OVERPASS_PRIMARY, query)
                } catch (primaryErr: Exception) {
                    if (primaryErr is CancellationException) throw primaryErr
                    postOverpass(OVERPASS_MIRROR, query)
                }
                parseOverpassPlaces(responseJson, lat, lon)
            }.getOrElse { err ->
                if (err is CancellationException) throw err
                null
            }

            if (overpassPlaces != null) {
                return@withContext Result.success(overpassPlaces)
            }

            // Fallback to Nominatim bounded search when Overpass mirrors rate-limit or time out
            val nominatimPlaces = searchNominatimNearby(lat, lon, clampedRadius)
            if (nominatimPlaces != null) {
                return@withContext Result.success(nominatimPlaces)
            }

            Result.failure(
                IllegalStateException("Không thể tải danh sách cơ sở thú cưng từ OpenStreetMap. Vui lòng kiểm tra mạng và thử lại.")
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.failure(
                IllegalStateException("Không thể tải danh sách cơ sở thú cưng từ OpenStreetMap. Vui lòng kiểm tra mạng và thử lại.")
            )
        }
    }

    private fun searchNominatimNearby(
        lat: Double,
        lon: Double,
        radiusMeters: Int,
    ): List<PetPlaceModel>? {
        val latDelta = radiusMeters / 111_000.0
        val lonDelta = radiusMeters / (111_000.0 * cos(Math.toRadians(lat)).coerceAtLeast(0.2))
        val minLon = lon - lonDelta
        val maxLat = lat + latDelta
        val maxLon = lon + lonDelta
        val minLat = lat - latDelta
        val collected = mutableListOf<PetPlaceModel>()
        var anySuccess = false

        for (keyword in listOf("veterinary", "thú y", "pet")) {
            runCatching {
                val q = URLEncoder.encode(keyword, "UTF-8")
                val url = URL(
                    "https://nominatim.openstreetmap.org/search?q=$q&format=json&limit=15&addressdetails=1" +
                        "&viewbox=$minLon,$maxLat,$maxLon,$minLat&bounded=1"
                )
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", USER_AGENT)
                    setRequestProperty("Accept", "application/json")
                    connectTimeout = 15_000
                    readTimeout = 20_000
                }
                if (conn.responseCode in 200..299) {
                    anySuccess = true
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    collected += parseNominatimPlaces(body, lat, lon, defaultKeyword = keyword)
                }
            }
        }
        return if (anySuccess) {
            collected.distinctBy { it.id }.sortedBy { it.distanceKm }
        } else {
            null
        }
    }

    suspend fun geocodeArea(areaName: String): Result<Pair<Double, Double>> = withContext(Dispatchers.IO) {
        try {
            val clean = areaName.trim()
            require(clean.isNotEmpty()) { "Vui lòng nhập tên khu vực hoặc quận/thành phố." }
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val url = URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=json&limit=1")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", USER_AGENT)
                connectTimeout = 15_000
                readTimeout = 20_000
            }
            val code = conn.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("Nominatim HTTP $code")
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            parseNominatimCoordinates(body, clean)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (_: Exception) {
            Result.failure(IllegalStateException("Không thể tìm kiếm khu vực lúc này. Vui lòng thử lại."))
        }
    }

    private fun postOverpass(endpoint: String, overpassQuery: String): String {
        val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", USER_AGENT)
            connectTimeout = 15_000
            readTimeout = 20_000
        }
        val payload = "data=${URLEncoder.encode(overpassQuery, "UTF-8")}"
        conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
        val code = conn.responseCode
        if (code !in 200..299) {
            throw IllegalStateException("Overpass HTTP $code")
        }
        return conn.inputStream.bufferedReader().use { it.readText() }
    }

    internal companion object {
        private const val OVERPASS_PRIMARY = "https://overpass-api.de/api/interpreter"
        private const val OVERPASS_MIRROR = "https://overpass.kumi.systems/api/interpreter"
        private const val USER_AGENT = "PetCareApp-Android/1.0"

        internal fun parseOverpassPlaces(
            rawJson: String,
            originLat: Double,
            originLon: Double,
        ): List<PetPlaceModel> {
            val root = JSONObject(rawJson)
            val elements = root.optJSONArray("elements") ?: return emptyList()
            val results = mutableListOf<PetPlaceModel>()

            for (i in 0 until elements.length()) {
                val el = elements.optJSONObject(i) ?: continue
                val center = el.optJSONObject("center")
                val elLat = if (el.has("lat") && !el.isNull("lat")) el.optDouble("lat", Double.NaN)
                else center?.optDouble("lat", Double.NaN) ?: Double.NaN
                val elLon = if (el.has("lon") && !el.isNull("lon")) el.optDouble("lon", Double.NaN)
                else center?.optDouble("lon", Double.NaN) ?: Double.NaN
                if (elLat.isNaN() || elLon.isNaN()) continue

                val tags = el.optJSONObject("tags") ?: JSONObject()
                val amenity = tags.optString("amenity")
                val shop = tags.optString("shop")

                val category = when {
                    amenity == "veterinary" -> "Thú y"
                    shop == "pet" -> "Cửa hàng"
                    else -> "Spa"
                }
                val defaultName = when (category) {
                    "Thú y" -> "Phòng khám thú y"
                    "Cửa hàng" -> "Cửa hàng thú cưng"
                    else -> "Spa & Chăm sóc thú cưng"
                }
                val name = tags.optString("name").takeIf { it.isNotBlank() }
                    ?: tags.optString("name:vi").takeIf { it.isNotBlank() }
                    ?: defaultName

                val addressParts = listOf(
                    tags.optString("addr:housenumber"),
                    tags.optString("addr:street"),
                    tags.optString("addr:suburb").ifBlank { tags.optString("addr:quarter") },
                    tags.optString("addr:district"),
                    tags.optString("addr:city"),
                ).filter { it.isNotBlank() }

                val address = if (addressParts.isNotEmpty()) {
                    addressParts.joinToString(", ")
                } else {
                    tags.optString("addr:full").takeIf { it.isNotBlank() }
                        ?: String.format(Locale.US, "Tọa độ: %.4f, %.4f", elLat, elLon)
                }

                val phone = tags.optString("phone").ifBlank { tags.optString("contact:phone") }
                val openingHours = tags.optString("opening_hours").takeIf { it.isNotBlank() }
                val distKm = haversineKm(originLat, originLon, elLat, elLon)
                val distLabel = if (distKm < 1.0) {
                    "${(distKm * 1000).toInt().coerceAtLeast(10)} m"
                } else {
                    String.format(Locale.US, "%.1f km", distKm)
                }

                val imageRes = when (category) {
                    "Thú y" -> R.drawable.dog
                    "Cửa hàng" -> R.drawable.cat
                    else -> R.drawable.rabbit
                }

                val subtitle = when (category) {
                    "Thú y" -> "Khám thú y • Tiêm phòng • Điều trị"
                    "Cửa hàng" -> "Thức ăn • Phụ kiện • Đồ dùng thú cưng"
                    else -> "Tắm sấy • Cắt tỉa • Chăm sóc thú cưng"
                }

                results.add(
                    PetPlaceModel(
                        id = "${el.optString("type", "node")}_${el.optLong("id", i.toLong())}",
                        name = name,
                        category = category,
                        subtitle = subtitle,
                        distance = distLabel,
                        distanceKm = distKm,
                        address = address,
                        phone = phone,
                        openingHours = openingHours,
                        imageRes = imageRes,
                        lat = elLat,
                        lng = elLon,
                        statusText = openingHours?.let { "$distLabel • $it" } ?: "$distLabel • OpenStreetMap",
                    )
                )
            }

            return results.sortedBy { it.distanceKm }
        }

        internal fun parseNominatimPlaces(
            rawJson: String,
            originLat: Double,
            originLon: Double,
            defaultKeyword: String = "veterinary",
        ): List<PetPlaceModel> {
            val arr = JSONArray(rawJson)
            val results = mutableListOf<PetPlaceModel>()
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val elLat = item.optString("lat").toDoubleOrNull() ?: continue
                val elLon = item.optString("lon").toDoubleOrNull() ?: continue
                val type = item.optString("type")
                val category = when {
                    type.contains("vet", ignoreCase = true) || defaultKeyword.contains("vet", ignoreCase = true) ||
                        defaultKeyword.contains("thú y", ignoreCase = true) -> "Thú y"
                    type.contains("pet", ignoreCase = true) || defaultKeyword == "pet" -> "Cửa hàng"
                    else -> "Spa"
                }
                val displayName = item.optString("display_name").trim()
                val shortName = item.optString("name").trim().ifEmpty {
                    displayName.substringBefore(",").trim().ifEmpty { "Cơ sở thú cưng" }
                }
                val distKm = haversineKm(originLat, originLon, elLat, elLon)
                val distLabel = if (distKm < 1.0) {
                    "${(distKm * 1000).toInt().coerceAtLeast(10)} m"
                } else {
                    String.format(Locale.US, "%.1f km", distKm)
                }
                val imageRes = when (category) {
                    "Thú y" -> R.drawable.dog
                    "Cửa hàng" -> R.drawable.cat
                    else -> R.drawable.rabbit
                }
                val subtitle = when (category) {
                    "Thú y" -> "Khám thú y • Tiêm phòng • Điều trị"
                    "Cửa hàng" -> "Thức ăn • Phụ kiện • Đồ dùng thú cưng"
                    else -> "Tắm sấy • Cắt tỉa • Chăm sóc thú cưng"
                }
                results.add(
                    PetPlaceModel(
                        id = "nom_${item.optLong("osm_id", i.toLong())}",
                        name = shortName,
                        category = category,
                        subtitle = subtitle,
                        distance = distLabel,
                        distanceKm = distKm,
                        address = displayName.ifEmpty { String.format(Locale.US, "Tọa độ: %.4f, %.4f", elLat, elLon) },
                        imageRes = imageRes,
                        lat = elLat,
                        lng = elLon,
                        statusText = "$distLabel • OpenStreetMap",
                    )
                )
            }
            return results.sortedBy { it.distanceKm }
        }

        internal fun parseNominatimCoordinates(rawJson: String, areaLabel: String): Result<Pair<Double, Double>> {
            val arr = JSONArray(rawJson)
            if (arr.length() == 0) {
                return Result.failure(IllegalStateException("Không tìm thấy địa điểm \"$areaLabel\"."))
            }
            val first = arr.optJSONObject(0)
                ?: return Result.failure(IllegalStateException("Không tìm thấy địa điểm \"$areaLabel\"."))
            val lat = first.optString("lat").toDoubleOrNull()
            val lon = first.optString("lon").toDoubleOrNull()
            if (lat == null || lon == null) {
                return Result.failure(IllegalStateException("Tọa độ địa điểm không hợp lệ."))
            }
            return Result.success(lat to lon)
        }

        internal fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val earthRadiusKm = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return earthRadiusKm * c
        }
    }
}
