package com.example.petcare.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.data.model.SuggestedVaccine
import com.example.petcare.ui.components.PetCareTopAppBar
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.viewmodel.PetManagementViewModel
import com.example.petcare.ui.viewmodel.SettingsViewModel
import com.example.petcare.utils.DateUtils
import com.example.petcare.utils.GeminiPetScanner
import com.example.petcare.utils.PetPhotoStorage
import com.example.petcare.utils.PetScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val SamplePrompts = listOf(
    "Corgi vàng trắng 6 tháng tuổi, nặng 7.2kg, năng động",
    "Mèo Anh lông ngắn xám xanh 1 tuổi, nặng 4kg",
    "Poodle nâu đỏ 4 tháng tuổi, nặng 2.8kg, chưa tiêm mũi 2",
)

@Composable
fun AddPetCameraScreen(
    modifier: Modifier = Modifier,
    viewModel: PetManagementViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onScannedProceed: (species: String, breed: String, weight: String) -> Unit = { _, _, _ -> },
    onPetSaved: () -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by settingsViewModel.settings.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var savedPhotoUri by remember { mutableStateOf("") }
    var promptText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var overloadFailureCount by remember { mutableIntStateOf(0) }
    var showManualFallbackDialog by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<PetScanResult?>(null) }

    // Editable draft fields populated after AI analysis
    var draftName by remember { mutableStateOf("") }
    var draftSpecies by remember { mutableStateOf("Chó") }
    var draftBreed by remember { mutableStateOf("") }
    var draftGender by remember { mutableStateOf("Chưa rõ") }
    var draftAge by remember { mutableStateOf("") }
    var draftWeightKgText by remember { mutableStateOf("") }
    var draftColor by remember { mutableStateOf("") }
    var draftNotes by remember { mutableStateOf("") }
    var draftCareSummary by remember { mutableStateOf("") }
    var suggestedVaccines by remember { mutableStateOf<List<SuggestedVaccine>>(emptyList()) }
    val checkedVaccines = remember { mutableStateMapOf<Int, Boolean>() }

    fun applyScanResult(result: PetScanResult) {
        scanResult = result
        draftName = result.name.ifBlank { draftName }
        draftSpecies = result.species.ifBlank { "Chó" }
        draftBreed = result.breed
        draftGender = result.gender.ifBlank { "Chưa rõ" }
        draftAge = result.ageText
        draftWeightKgText = if (result.weightKg > 0.0) {
            result.weightKg.toString()
        } else {
            result.estimatedWeight.replace("kg", "", ignoreCase = true).trim()
        }
        draftColor = result.color
        draftCareSummary = result.description.ifBlank { result.notes }
        draftNotes = result.notes.ifBlank { result.description }
        suggestedVaccines = result.suggestedVaccines
        checkedVaccines.clear()
        result.suggestedVaccines.indices.forEach { idx ->
            checkedVaccines[idx] = true
        }
    }

    fun runAiAnalysis(bitmap: Bitmap?, description: String) {
        if (bitmap == null && description.isBlank()) {
            errorMessage = "Vui lòng chọn ảnh hoặc nhập mô tả về thú cưng."
            return
        }
        coroutineScope.launch {
            isLoading = true
            errorMessage = null
            val result = GeminiPetScanner.analyzePet(
                bitmap = bitmap,
                userDescription = description,
            )
            result.onSuccess { scanned ->
                overloadFailureCount = 0
                applyScanResult(scanned)
            }.onFailure { err ->
                val raw = err.message.orEmpty()
                if (GeminiPetScanner.isTransientOverloadError(raw) ||
                    raw.contains("quá tải", ignoreCase = true) ||
                    raw.contains("Quá thời gian", ignoreCase = true)
                ) {
                    overloadFailureCount += 1
                    if (overloadFailureCount > 3) {
                        showManualFallbackDialog = true
                    }
                }
                errorMessage = raw.ifBlank { "Không thể phân tích thú cưng lúc này." }
            }
            isLoading = false
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            errorMessage = null
            coroutineScope.launch {
                savedPhotoUri = withContext(Dispatchers.IO) {
                    PetPhotoStorage.saveBitmapToInternalStorage(context, bitmap).orEmpty()
                }
            }
            runAiAnalysis(bitmap, promptText.trim())
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val (copiedPath, decodedBitmap) = withContext(Dispatchers.IO) {
                    val path = PetPhotoStorage.copyUriToInternalStorage(context, uri).orEmpty()
                    val bmp = PetPhotoStorage.decodeBitmapSafely(context, uri)
                    path to bmp
                }
                if (copiedPath.isNotBlank()) {
                    savedPhotoUri = copiedPath
                }
                if (decodedBitmap != null) {
                    selectedBitmap = decodedBitmap
                    runAiAnalysis(decodedBitmap, promptText.trim())
                } else {
                    errorMessage = "Không thể đọc ảnh đã chọn. Thử ảnh khác hoặc nhập mô tả."
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            PetCareTopAppBar(
                onBackClick = onBackClick,
                title = "Thêm thú cưng bằng AI",
                subtitle = "Chụp ảnh hoặc mô tả tự nhiên để tạo hồ sơ và gợi ý lịch tiêm",
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Photo capture / gallery preview box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colorScheme.surface)
                    .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                    .clickable(enabled = !isLoading) {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                val bmp = selectedBitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Ảnh thú cưng",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(40.dp),
                        )
                        Text(
                            text = "Chọn ảnh thú cưng hoặc mô tả bên dưới",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                        )
                        Text(
                            text = "Ảnh được lưu an toàn trong thiết bị",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorScheme.scrim.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = colorScheme.onPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "AI đang phân tích hồ sơ...",
                                color = colorScheme.onPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { cameraLauncher.launch(null) },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Chụp ảnh", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Chụp ảnh", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Thư viện", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Thư viện ảnh", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mô tả thêm cho AI (tùy chọn hoặc dùng thay ảnh)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = promptText,
                onValueChange = { promptText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("VD: Bé Milo giống Poodle 5 tháng, nặng 3.5kg, lông nâu") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SamplePrompts.forEach { sample ->
                    FilterChip(
                        selected = promptText == sample,
                        onClick = { promptText = sample },
                        label = { Text(sample, maxLines = 1, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primaryContainer,
                            selectedLabelColor = colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { runAiAnalysis(selectedBitmap, promptText.trim()) },
                enabled = !isLoading && (selectedBitmap != null || promptText.isNotBlank()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Phân tích", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Phân tích bằng Gemini AI", fontWeight = FontWeight.Bold)
            }

            errorMessage?.let { err ->
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = err,
                            color = colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                        )
                        if (overloadFailureCount > 3) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "AI đã nghẽn $overloadFailureCount lần liên tiếp. Bạn nên nhập thông tin thú cưng thủ công để tiết kiệm thời gian.",
                                color = colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val weightArg = if (draftWeightKgText.isNotBlank()) {
                                        "${draftWeightKgText.trim()} kg"
                                    } else ""
                                    onScannedProceed(draftSpecies.ifBlank { "Chó" }, draftBreed, weightArg)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text("Nhập thủ công ngay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (scanResult != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    border = BorderStroke(1.dp, colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "OK",
                                tint = colorScheme.primary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Kết quả nhận diện & Chỉnh sửa",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                            )
                        }

                        if (draftCareSummary.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colorScheme.primaryContainer.copy(alpha = 0.45f))
                                    .padding(12.dp),
                            ) {
                                Text(
                                    text = "Gợi ý chăm sóc: $draftCareSummary",
                                    fontSize = 13.sp,
                                    color = colorScheme.onSurface,
                                )
                            }
                        }

                        OutlinedTextField(
                            value = draftName,
                            onValueChange = { draftName = it },
                            label = { Text("Tên thú cưng") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            OutlinedTextField(
                                value = draftSpecies,
                                onValueChange = { draftSpecies = it },
                                label = { Text("Loài") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = draftBreed,
                                onValueChange = { draftBreed = it },
                                label = { Text("Giống") },
                                modifier = Modifier.weight(1.3f),
                                singleLine = true,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            OutlinedTextField(
                                value = draftAge,
                                onValueChange = { draftAge = it },
                                label = { Text("Tuổi ước tính") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = draftWeightKgText,
                                onValueChange = { draftWeightKgText = it },
                                label = { Text("Cân nặng (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            OutlinedTextField(
                                value = draftGender,
                                onValueChange = { draftGender = it },
                                label = { Text("Giới tính") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = draftColor,
                                onValueChange = { draftColor = it },
                                label = { Text("Màu lông") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                            )
                        }

                        OutlinedTextField(
                            value = draftNotes,
                            onValueChange = { draftNotes = it },
                            label = { Text("Ghi chú sức khỏe") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                        )

                        if (suggestedVaccines.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Gợi ý lịch tiêm & tự động thêm vào Lịch (${suggestedVaccines.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                            )
                            suggestedVaccines.forEachIndexed { index, vaccine ->
                                val isChecked = checkedVaccines[index] ?: true
                                val targetMillis = DateUtils.daysFromNowMillis(vaccine.daysFromNow)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable { checkedVaccines[index] = !isChecked }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checkedVaccines[index] = it },
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = vaccine.vaccineName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colorScheme.onSurface,
                                        )
                                        Text(
                                            text = "Dự kiến: ${DateUtils.formatDate(targetMillis)}" +
                                                if (vaccine.notes.isNotBlank()) " • ${vaccine.notes}" else "",
                                            fontSize = 12.sp,
                                            color = colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                if (draftName.isBlank()) {
                                    errorMessage = "Vui lòng đặt tên cho thú cưng trước khi lưu."
                                    return@Button
                                }
                                val parsedKg = DateUtils.parseWeightKg(draftWeightKgText)
                                    .let { if (it > 0.0) it else 3.0 }
                                val selectedVaccines = suggestedVaccines.filterIndexed { index, _ ->
                                    checkedVaccines[index] ?: true
                                }
                                viewModel.savePetFromAi(
                                    name = draftName,
                                    species = draftSpecies,
                                    breed = draftBreed,
                                    gender = draftGender,
                                    ageText = draftAge,
                                    weightKg = parsedKg,
                                    color = draftColor,
                                    notes = draftNotes,
                                    photoUri = savedPhotoUri,
                                    suggestedVaccines = selectedVaccines,
                                    defaultReminderDays = settings.defaultVaccineReminderDays,
                                )
                                onPetSaved()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                        ) {
                            Text(
                                text = "Lưu hồ sơ & Lịch tiêm",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = {
                    val weightArg = if (draftWeightKgText.isNotBlank()) {
                        "${draftWeightKgText.trim()} kg"
                    } else {
                        ""
                    }
                    onScannedProceed(
                        draftSpecies.ifBlank { "Chó" },
                        draftBreed,
                        weightArg,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Chuyển sang nhập thủ công",
                    color = colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showManualFallbackDialog) {
        AlertDialog(
            onDismissRequest = { showManualFallbackDialog = false },
            title = { Text("AI đang quá tải", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Máy chủ Gemini đang nghẽn quá 3 lần liên tiếp. Bạn có muốn chuyển sang nhập thông tin thú cưng thủ công ngay không?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showManualFallbackDialog = false
                        val weightArg = if (draftWeightKgText.isNotBlank()) {
                            "${draftWeightKgText.trim()} kg"
                        } else ""
                        onScannedProceed(draftSpecies.ifBlank { "Chó" }, draftBreed, weightArg)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                ) {
                    Text("Nhập thủ công")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showManualFallbackDialog = false }) {
                    Text("Ở lại")
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddPetCameraScreenPreview() {
    PetCareTheme {
        AddPetCameraScreen()
    }
}
