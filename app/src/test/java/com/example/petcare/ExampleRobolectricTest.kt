package com.example.petcare

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.petcare.data.model.AppSettings
import com.example.petcare.data.model.MedicationModel
import com.example.petcare.data.model.PetModel
import com.example.petcare.data.model.ScheduleModel
import com.example.petcare.data.model.VaccinationModel
import com.example.petcare.data.model.resolvePetIconRes
import com.example.petcare.ui.components.resolveRenderableDrawableRes
import com.example.petcare.utils.BootReminderReceiver
import com.example.petcare.utils.GoogleSignInHelper
import com.example.petcare.utils.NotificationHelper
import com.example.petcare.utils.PetAlarmReceiver
import com.example.petcare.utils.PetPhotoStorage
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.io.File
import java.net.URI

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        NotificationHelper.createNotificationChannel(context)
        NotificationHelper.clearAllReminders(context)
    }

    @Test
    fun `syncReminders schedules and clears alarms via ShadowAlarmManager`() {
        val futureMillis = System.currentTimeMillis() + 5L * 86_400_000L
        val vaccinations = listOf(
            VaccinationModel(
                id = "vac-1",
                petId = "pet-1",
                petName = "Milo",
                title = "5 trong 1",
                scheduledDateMillis = futureMillis,
                completed = false,
                reminderEnabled = true,
                reminderDaysBefore = 1,
            )
        )
        val medications = listOf(
            MedicationModel(
                id = "med-1",
                petId = "pet-1",
                petName = "Milo",
                name = "Vitamin",
                dosage = "1 viên",
                reminderHour = 9,
                reminderMinute = 0,
                active = true,
                reminderEnabled = true,
            )
        )
        val schedules = listOf(
            ScheduleModel(
                id = "sch-1",
                petId = "pet-1",
                petName = "Milo",
                title = "Tắm spa",
                triggerAtMillis = futureMillis,
                reminderEnabled = true,
                isCompleted = false,
            )
        )

        NotificationHelper.syncReminders(
            context = context,
            vaccinations = vaccinations,
            medications = medications,
            schedules = schedules,
            settings = AppSettings(notificationsEnabled = true),
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = Shadows.shadowOf(alarmManager)
        assertTrue(shadowAlarmManager.scheduledAlarms.size >= 3)

        NotificationHelper.syncReminders(
            context = context,
            vaccinations = vaccinations,
            medications = medications,
            schedules = schedules,
            settings = AppSettings(notificationsEnabled = false),
        )
        assertEquals(0, shadowAlarmManager.scheduledAlarms.size)
    }

    @Test
    fun `alarm and boot receivers handle reminder intents cleanly`() {
        val receiver = PetAlarmReceiver()
        val intent = Intent(context, PetAlarmReceiver::class.java).apply {
            putExtra("ALARM_TYPE", "VACCINE")
            putExtra("PET_NAME", "Milo")
            putExtra("TITLE", "Tiêm nhắc 5 trong 1")
            putExtra("EXTRA_ID", "vac-1")
        }
        receiver.onReceive(context, intent)

        val bootReceiver = BootReminderReceiver()
        bootReceiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))
    }

    @Test
    fun `petPhotoStorage saves inside pet_photos and protects external paths`() = runTest {
        val bmp = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        val savedUri = PetPhotoStorage.saveBitmapToInternalStorage(context, bmp)
        assertNotNull(savedUri)
        assertTrue(savedUri!!.startsWith("file://"))
        val savedFile = File(URI(savedUri))
        assertTrue(savedFile.exists())
        assertEquals("pet_photos", savedFile.parentFile?.name)

        // Path traversal / external file must NOT be deleted by deleteOwnedPetPhoto
        val externalFile = File(context.cacheDir, "do_not_delete.txt").apply {
            writeText("keep")
        }
        PetPhotoStorage.deleteOwnedPetPhoto(context, Uri.fromFile(externalFile).toString())
        assertTrue(externalFile.exists())

        PetPhotoStorage.deleteOwnedPetPhoto(context, savedUri)
        assertFalse(savedFile.exists())
    }

    @Test
    fun `resolveRenderableDrawableRes and resolvePetIconRes block stale or non-vector drawables`() {
        // Known valid vector & raster assets remain intact
        assertEquals(R.drawable.dog, resolveRenderableDrawableRes(context, R.drawable.dog))
        assertEquals(R.drawable.cat, resolveRenderableDrawableRes(context, R.drawable.cat))
        assertEquals(R.drawable.img, resolveRenderableDrawableRes(context, R.drawable.img))

        // Invalid IDs, adaptive-icon XML, and system selector XMLs fall back safely
        assertEquals(R.drawable.dog, resolveRenderableDrawableRes(context, 0))
        assertEquals(R.drawable.dog, resolveRenderableDrawableRes(context, 0x7f089999))
        assertEquals(R.drawable.dog, resolveRenderableDrawableRes(context, R.mipmap.ic_launcher))
        assertEquals(
            R.drawable.dog,
            resolveRenderableDrawableRes(context, android.R.drawable.list_selector_background),
        )

        // Stale persisted Firestore iconRes integers remap by species
        val staleCat = PetModel(id = "c1", species = "Mèo", iconRes = android.R.drawable.list_selector_background)
        val staleRabbit = PetModel(id = "r1", species = "Thỏ", iconRes = 0x7f089999)
        assertEquals(R.drawable.cat, staleCat.safeIconRes)
        assertEquals(R.drawable.rabbit, staleRabbit.safeIconRes)
        assertEquals(R.drawable.cat, resolvePetIconRes(R.drawable.cat, "Chó"))
    }

    @Test
    fun `googleSignInHelper suppressAutoSignIn prevents auto sign-in after logout`() = runTest {
        GoogleSignInHelper.suppressAutoSignIn = true
        val token = GoogleSignInHelper.attemptAutoSignIn(context)
        assertEquals(null, token)
        GoogleSignInHelper.suppressAutoSignIn = false
    }
}
