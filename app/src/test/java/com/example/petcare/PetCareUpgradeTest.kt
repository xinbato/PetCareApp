package com.example.petcare

import com.example.petcare.data.model.AppSettings
import com.example.petcare.data.model.PetModel
import com.example.petcare.data.model.ThemeMode
import com.example.petcare.data.model.VaccinationModel
import com.example.petcare.data.repository.PlacesRepository
import com.example.petcare.ui.screens.buildPetSummaryText
import com.example.petcare.utils.DateUtils
import com.example.petcare.utils.GeminiPetScanner
import com.example.petcare.utils.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PetCareUpgradeTest {

    @Test
    fun `appSettings weight conversion between kg and lb`() {
        val kgSettings = AppSettings(usePounds = false, themeMode = ThemeMode.DARK)
        assertEquals("kg", kgSettings.weightUnitLabel)
        assertEquals("10.0 kg", kgSettings.formatWeight(10.0))

        val lbSettings = kgSettings.copy(usePounds = true)
        assertEquals("lb", lbSettings.weightUnitLabel)
        val lbs = lbSettings.toDisplayWeight(10.0)
        assertEquals(22.0462, lbs, 0.01)
        assertTrue(lbSettings.formatWeight(10.0).endsWith("lb"))
    }

    @Test
    fun `dateUtils parsing and formatting helpers handle valid and edge inputs`() {
        assertEquals(7.5, DateUtils.parseWeightKg("7,5 kg"), 0.001)
        assertEquals(0.0, DateUtils.parseWeightKg("chưa có"), 0.001)
        assertEquals("5.5 kg", DateUtils.formatWeight(5.5))

        val parsed = DateUtils.parseDateMillis("25/12/2026")
        assertNotNull(parsed)
        assertEquals("25/12/2026", DateUtils.formatDate(parsed!!))
        assertNull(DateUtils.parseDateMillis("invalid"))

        val key = DateUtils.todayKey()
        assertTrue(key.matches(Regex("""\d{4}-\d{2}-\d{2}""")))
    }

    @Test
    fun `placesRepository parses overpass nodes and ways and computes distance`() {
        val overpassJson = """
            {
              "elements": [
                {
                  "type": "node",
                  "id": 1001,
                  "lat": 10.7780,
                  "lon": 106.7010,
                  "tags": {
                    "name": "Phòng Khám Thú Y PetCare",
                    "amenity": "veterinary",
                    "addr:street": "Nguyễn Thị Minh Khai",
                    "addr:city": "TP.HCM",
                    "phone": "0909123456",
                    "opening_hours": "08:00-20:00"
                  }
                },
                {
                  "type": "way",
                  "id": 1002,
                  "center": { "lat": 10.7850, "lon": 106.6950 },
                  "tags": {
                    "name": "Cửa Hàng Pet Shop",
                    "shop": "pet"
                  }
                }
              ]
            }
        """.trimIndent()

        val places = PlacesRepository.parseOverpassPlaces(overpassJson, 10.7769, 106.7009)
        assertEquals(2, places.size)
        assertEquals("Thú y", places[0].category)
        assertEquals("Phòng Khám Thú Y PetCare", places[0].name)
        assertEquals("08:00-20:00", places[0].openingHours)
        assertTrue(places[0].distanceKm >= 0.0)
        assertEquals("Cửa hàng", places[1].category)
        assertTrue(places[0].distanceKm <= places[1].distanceKm)
    }

    @Test
    fun `placesRepository parses nominatim coordinates cleanly`() {
        val geoJson = """[{"lat":"10.7769","lon":"106.7009","display_name":"Ho Chi Minh City"}]"""
        val coords = PlacesRepository.parseNominatimCoordinates(geoJson, "Ho Chi Minh City").getOrNull()
        assertNotNull(coords)
        assertEquals(10.7769, coords!!.first, 0.0001)
        assertEquals(106.7009, coords.second, 0.0001)
        assertTrue(PlacesRepository.parseNominatimCoordinates("[]", "Empty").isFailure)

        val nomPlacesJson = """
            [
              {
                "osm_id": 5501,
                "lat": "10.7800",
                "lon": "106.7020",
                "type": "veterinary",
                "name": "Phòng khám Thú y Sài Gòn",
                "display_name": "Phòng khám Thú y Sài Gòn, Quận 1, TP. Hồ Chí Minh"
              }
            ]
        """.trimIndent()
        val nomPlaces = PlacesRepository.parseNominatimPlaces(nomPlacesJson, 10.7769, 106.7009)
        assertEquals(1, nomPlaces.size)
        assertEquals("Thú y", nomPlaces[0].category)
        assertEquals("Phòng khám Thú y Sài Gòn", nomPlaces[0].name)
        assertTrue(nomPlaces[0].distanceKm > 0.0)
    }

    @Test
    fun `geminiPetScanner parses markdown fenced JSON with suggested vaccines`() {
        val raw = """
            ```json
            {
              "name": "Milo",
              "species": "Chó",
              "breed": "Corgi",
              "gender": "Đực",
              "ageText": "6 tháng",
              "weightKg": 7.2,
              "color": "Vàng trắng",
              "notes": "Thích chạy nhảy",
              "careSummary": "Chế độ vận động vừa phải, giữ ấm khi trời lạnh.",
              "suggestedVaccines": [
                {
                  "vaccineName": "Vaccine 5 trong 1",
                  "daysFromNow": 14,
                  "notes": "Mũi nhắc định kỳ"
                },
                {
                  "vaccineName": "Tiêm phòng Dại",
                  "daysFromNow": 30,
                  "notes": "Bắt buộc hàng năm"
                }
              ]
            }
            ```
        """.trimIndent()

        val parsed = GeminiPetScanner.parseScanResultJson(raw)
        assertEquals("Milo", parsed.name)
        assertEquals("Chó", parsed.species)
        assertEquals("Corgi", parsed.breed)
        assertEquals("6 tháng", parsed.ageText)
        assertEquals(7.2, parsed.weightKg, 0.01)
        assertEquals(2, parsed.suggestedVaccines.size)
        assertEquals("Vaccine 5 trong 1", parsed.suggestedVaccines[0].vaccineName)
        assertEquals(14, parsed.suggestedVaccines[0].daysFromNow)

        val fallback = GeminiPetScanner.parseScanResultJson("not valid json")
        assertEquals("Chó", fallback.species)

        assertTrue(GeminiPetScanner.isTransientOverloadError("This model is currently experiencing high demand."))
        assertFalse(GeminiPetScanner.isTransientOverloadError("Firebase App Check token is invalid."))
        assertTrue(GeminiPetScanner.mapAiErrorMessage("This model is currently experiencing high demand.").contains("quá tải"))
    }

    @Test
    fun `notificationHelper parses schedule hours and avoids past trigger timestamps`() {
        assertEquals(8 to 30, NotificationHelper.parseHourMinute("08:30 AM"))
        assertEquals(9 to 15, NotificationHelper.parseHourMinute("09:15"))
        assertEquals(14 to 5, NotificationHelper.parseHourMinute("14:05"))
        assertNull(NotificationHelper.parseHourMinute("invalid"))

        val now = 1_700_000_000_000L
        val pastVacc = NotificationHelper.computeVaccinationTriggerMillis(now - 100_000_000L, reminderDaysBefore = 3L, nowMillis = now)
        assertNull(pastVacc)

        val futureVacc = NotificationHelper.computeVaccinationTriggerMillis(now + 10L * 86_400_000L, reminderDaysBefore = 3L, nowMillis = now)
        assertNotNull(futureVacc)
        assertTrue(futureVacc!! > now)

        val nextDaily = NotificationHelper.computeNextDailyTriggerMillis(hour = 8, minute = 0, endDateMillis = 0L, nowMillis = now)
        assertNotNull(nextDaily)
        assertTrue(nextDaily!! > now)

        val parsedSchedMillis = NotificationHelper.parseScheduleMillis("15/12/2026", "09:00")
        assertNotNull(parsedSchedMillis)
        assertTrue(parsedSchedMillis!! > 0L)
    }

    @Test
    fun `buildPetSummaryText formats pet summary without emojis`() {
        val pets = listOf(
            PetModel(
                id = "p1",
                name = "Bông",
                species = "Mèo",
                breed = "ALN",
                age = "1 tuổi",
                weight = "4.2 kg",
                weightKg = 4.2,
                gender = "Cái",
                notes = "Ăn hạt Royal Canin",
            )
        )
        val vaccinations = listOf(
            VaccinationModel(
                id = "v1",
                petId = "p1",
                title = "4 bệnh mèo",
                dateTime = "10/12/2026",
                completed = false,
            )
        )
        val text = buildPetSummaryText("Minh", pets, vaccinations, AppSettings())
        assertTrue(text.contains("Bông (Mèo - ALN)"))
        assertTrue(text.contains("4 bệnh mèo"))
        assertTrue(text.contains("4.2 kg"))
        assertFalse(text.contains(Regex("[\uD800-\uDBFF]")))
    }
}
