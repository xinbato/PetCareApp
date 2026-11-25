package com.example.petcare.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import java.util.UUID

/**
 * Manages app-owned pet photos under filesDir/pet_photos so picked or captured images persist.
 */
object PetPhotoStorage {
    private const val PET_PHOTOS_DIR = "pet_photos"

    suspend fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(context.filesDir, PET_PHOTOS_DIR).apply { mkdirs() }
                val dest = File(dir, "pet_${UUID.randomUUID()}.jpg")
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                } ?: return@withContext null
                Uri.fromFile(dest).toString()
            }.getOrNull()
        }

    suspend fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(context.filesDir, PET_PHOTOS_DIR).apply { mkdirs() }
                val dest = File(dir, "pet_${UUID.randomUUID()}.jpg")
                dest.outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
                }
                Uri.fromFile(dest).toString()
            }.getOrNull()
        }

    suspend fun decodeBitmapSafely(context: Context, uri: Uri): Bitmap? =
        withContext(Dispatchers.IO) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = false
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            }.getOrNull()
        }

    fun deleteOwnedPetPhoto(context: Context, photoUri: String) {
        if (!photoUri.startsWith("file://")) return
        runCatching {
            val photosDir = File(context.filesDir, PET_PHOTOS_DIR).canonicalFile
            val target = File(URI(photoUri)).canonicalFile
            if (target.parentFile == photosDir && target.exists()) {
                target.delete()
            }
        }
    }
}
