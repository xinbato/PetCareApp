package com.example.petcare.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.R
import com.example.petcare.data.model.PetModel
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.components.PetCareTextField
import com.example.petcare.ui.theme.PetCareTheme
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.petcare.ui.components.PetAvatar
import com.example.petcare.ui.viewmodel.PetManagementViewModel
import com.example.petcare.utils.DateUtils
import com.example.petcare.utils.PetPhotoStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun AddPetHandmadeScreen(
    modifier: Modifier = Modifier,
    initialSpecies: String = "Chó",
    initialBreed: String = "",
    initialWeight: String = "",
    viewModel: PetManagementViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onPetAdded: () -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    var name by remember { mutableStateOf("") }
    var species by remember(initialSpecies) { mutableStateOf(initialSpecies.ifBlank { "Chó" }) }
    var breed by remember(initialBreed) { mutableStateOf(initialBreed) }
    var dateOfBirth by remember { mutableStateOf("") }
    var weight by remember(initialWeight) { mutableStateOf(initialWeight) }
    var gender by remember { mutableStateOf("Đực") }
    var photoUri by remember { mutableStateOf("") }
    var iconRes by remember(species) {
        val res = when {
            species.contains("Mèo", ignoreCase = true) -> R.drawable.cat
            species.contains("Thỏ", ignoreCase = true) -> R.drawable.rabbit
            else -> R.drawable.dog
        }
        mutableIntStateOf(res)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val savedPath = withContext(Dispatchers.IO) {
                    PetPhotoStorage.copyUriToInternalStorage(context, uri).orEmpty()
                }
                if (savedPath.isNotBlank()) {
                    photoUri = savedPath
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
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Thêm thú cưng",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Text(
                        text = "Nhập thông tin thú cưng mới của bạn",
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            // Scrollable Form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Real Photo & Species Avatar Selector
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (photoUri.isNotBlank()) {
                        PetAvatar(
                            photoUri = photoUri,
                            fallbackIconRes = iconRes,
                            contentDescription = "Ảnh thú cưng",
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .border(2.dp, colorScheme.primary, CircleShape),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        listOf(
                            R.drawable.dog to "Chó",
                            R.drawable.cat to "Mèo",
                            R.drawable.rabbit to "Thỏ"
                        ).forEach { (res, sp) ->
                            val isSelected = iconRes == res
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) colorScheme.primaryContainer else colorScheme.surface)
                                    .border(
                                        2.dp,
                                        if (isSelected) colorScheme.primary else colorScheme.outlineVariant,
                                        CircleShape
                                    )
                                    .clickable {
                                        iconRes = res
                                        species = sp
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = res),
                                    contentDescription = sp,
                                    modifier = Modifier.size(46.dp)
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text(
                            text = if (photoUri.isBlank()) "Chọn ảnh thật từ thư viện" else "Đổi ảnh thú cưng",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                PetCareTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Tên thú cưng",
                    placeholder = "Ví dụ: Milo, Bông, Bắp..."
                )

                PetCareTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = "Loài",
                    placeholder = "Chó, Mèo, Thỏ..."
                )

                PetCareTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = "Giống",
                    placeholder = "Ví dụ: Corgi, Poodle, Mèo Ta..."
                )

                PetCareTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = "Ngày sinh",
                    placeholder = "DD/MM/YYYY"
                )

                PetCareTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = "Cân nặng",
                    placeholder = "Ví dụ: 6.5 kg"
                )

                // Gender selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Giới tính",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("Đực", "Cái").forEach { g ->
                            val isSelected = gender == g
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) colorScheme.primaryContainer else colorScheme.surface)
                                    .border(
                                        1.dp,
                                        if (isSelected) colorScheme.primary else colorScheme.outlineVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { gender = g },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = g,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) colorScheme.primary else colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Save Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val newId = UUID.randomUUID().toString()
                            val calculatedAge = calculatePetAge(dateOfBirth.trim())
                            val parsedKg = DateUtils.parseWeightKg(weight)
                            val displayWeight = if (parsedKg > 0.0) {
                                DateUtils.formatWeight(parsedKg)
                            } else {
                                weight.trim().ifEmpty { "Chưa có" }
                            }
                            val newPet = PetModel(
                                id = newId,
                                name = name.trim(),
                                species = species.trim(),
                                breed = breed.trim().ifEmpty { "Chưa rõ giống" },
                                dateOfBirth = dateOfBirth.trim(),
                                age = calculatedAge,
                                weight = displayWeight,
                                weightKg = parsedKg,
                                gender = gender,
                                iconRes = iconRes,
                                photoUri = photoUri,
                                statusText = "Khỏe mạnh",
                                statusDetail = if (weight.isNotBlank()) "Cân nặng $displayWeight" else "Chưa có cân nặng",
                                isFavorite = false,
                                temperature = "",
                                activity = "",
                                diet = "",
                                lastUpdated = "vừa xong"
                            )
                            viewModel.savePet(newPet)
                            viewModel.selectPet(newId)
                            onPetAdded()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    enabled = name.isNotBlank()
                ) {
                    Text(
                        text = "Thêm thú cưng",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddPetHandmadeScreenPreview() {
    PetCareTheme {
        AddPetHandmadeScreen()
    }
}

private fun calculatePetAge(dob: String): String {
    if (dob.isBlank()) return "Chưa rõ tuổi"
    return runCatching {
        val parts = dob.split("/")
        if (parts.size == 3) {
            val birthYear = parts[2].toInt()
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val years = currentYear - birthYear
            if (years > 0) "$years tuổi" else "Dưới 1 tuổi"
        } else "Chưa rõ tuổi"
    }.getOrDefault("Chưa rõ tuổi")
}
