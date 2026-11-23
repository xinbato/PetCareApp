package com.example.petcare.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.components.PetCareIcons
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.RedError
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary
import com.example.petcare.utils.GeminiPetScanner
import com.example.petcare.utils.PetScanResult
import kotlinx.coroutines.launch

@Composable
fun AddPetCameraScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onScannedProceed: (species: String, breed: String, weight: String) -> Unit = { _, _, _ -> },
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<PetScanResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun runScan(bitmap: Bitmap) {
        selectedBitmap = bitmap
        isLoading = true
        errorMessage = null
        scanResult = null
        coroutineScope.launch {
            val result = GeminiPetScanner.analyzePetImage(bitmap)
            result.onSuccess { data ->
                scanResult = data
                isLoading = false
            }.onFailure { ex ->
                errorMessage = ex.localizedMessage ?: "Không thể nhận diện hình ảnh. Vui lòng thử lại."
                isLoading = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            runScan(bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bitmap = decodeUriToBitmap(context, uri)
            if (bitmap != null) {
                runScan(bitmap)
            } else {
                errorMessage = "Không thể tải ảnh từ thư viện thiết bị."
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = KeyboardArrowLeftIcon,
                        contentDescription = "Quay lại",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Quét thú cưng bằng AI",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Chụp hoặc chọn ảnh để nhận diện giống loài",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Viewfinder / Image Preview Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .border(2.dp, GreenPrimary, RoundedCornerShape(24.dp))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val currentBmp = selectedBitmap
                        if (currentBmp != null) {
                            Image(
                                bitmap = currentBmp.asImageBitmap(),
                                contentDescription = "Ảnh thú cưng",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .background(GreenLight, RoundedCornerShape(20.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = PetCareIcons.Camera,
                                        contentDescription = "Camera",
                                        tint = GreenPrimary,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Chưa có ảnh thú cưng",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Chụp ảnh trực tiếp hoặc chọn ảnh có sẵn",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Loading Overlay
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 3.dp,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "AI đang nhận diện...",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select / Capture Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Chụp ảnh", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = GreenPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Chọn từ máy", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = GreenPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error Message Card
                if (errorMessage != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, RedError.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Thông báo nhận diện",
                                fontWeight = FontWeight.Bold,
                                color = RedError,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorMessage.orEmpty(),
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // AI Scan Result Card
                val res = scanResult
                if (res != null) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, GreenPrimary, RoundedCornerShape(18.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "Kết quả nhận diện",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Loài động vật", fontSize = 12.sp, color = TextSecondary)
                                    Text(text = res.species, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Column {
                                    Text(text = "Cân nặng ước tính", fontSize = 12.sp, color = TextSecondary)
                                    Text(text = res.estimatedWeight, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GreenPrimary)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = "Giống loài (Breed)", fontSize = 12.sp, color = TextSecondary)
                            Text(text = res.breed, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                            if (res.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(text = "Thông tin & Chăm sóc", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    text = res.description,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Bottom Action Button
            val currentResult = scanResult
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        if (currentResult != null) {
                            onScannedProceed(
                                currentResult.species,
                                currentResult.breed,
                                currentResult.estimatedWeight
                            )
                        } else {
                            onScannedProceed("Chó", "Golden Retriever", "12.0 kg")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = !isLoading
                ) {
                    Text(
                        text = if (currentResult != null) "Dùng thông tin này để thêm thú cưng" else "Bỏ qua & Nhập thủ công",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun decodeUriToBitmap(context: Context, uri: Uri): Bitmap? {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }.getOrNull()
}

@Preview(showBackground = true)
@Composable
fun AddPetCameraScreenPreview() {
    PetCareTheme {
        AddPetCameraScreen()
    }
}
