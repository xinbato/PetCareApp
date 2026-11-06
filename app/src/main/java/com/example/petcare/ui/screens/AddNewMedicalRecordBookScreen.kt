package com.example.petcare.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.data.model.MedicalRecordEntryModel
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.components.PetCareIcons
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary
import java.util.UUID
@Composable
fun AddNewMedicalRecordBookScreen(
    modifier: Modifier = Modifier,
    initialPetId: String = "1",
    onBackClick: () -> Unit = {},
    onSaved: () -> Unit = {},
) {
    val allPets = PetRepository.pets
    var selectedPetId by remember { mutableStateOf(initialPetId) }
    val currentPet = allPets.find { it.id == selectedPetId } ?: PetRepository.getSelectedPet()

    var petNameInput by remember { mutableStateOf(currentPet.name) }
    var dateTime by remember { mutableStateOf("") }
    var activityType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var photoAttached by remember { mutableStateOf(false) }
    var emotionInput by remember { mutableStateOf("") }

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
            // Header: "< Thêm mới"
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
                Text(
                    text = "Thêm mới",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Field 1: "Chọn thú cưng"
                FigmaCardTextField(
                    value = petNameInput,
                    onValueChange = { petNameInput = it },
                    placeholder = "Chọn thú cưng"
                )

                // Field 2: "Ngày & giờ"
                FigmaCardTextField(
                    value = dateTime,
                    onValueChange = { dateTime = it },
                    placeholder = "Ngày & giờ"
                )

                // Field 3: "Loại hoạt động"
                FigmaCardTextField(
                    value = activityType,
                    onValueChange = { activityType = it },
                    placeholder = "Loại hoạt động"
                )

                // Field 4: "Mô tả"
                FigmaCardTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Mô tả"
                )

                // Field 5: "Ảnh / video"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                        .clickable { photoAttached = !photoAttached }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (photoAttached) "Đã đính kèm ảnh" else "Ảnh / video",
                            fontSize = 15.sp,
                            color = if (photoAttached) GreenPrimary else TextSecondary.copy(alpha = 0.8f),
                            fontWeight = if (photoAttached) FontWeight.SemiBold else FontWeight.Normal
                        )
                        Icon(
                            imageVector = if (photoAttached) PetCareIcons.Check else PetCareIcons.Camera,
                            contentDescription = "Ảnh",
                            tint = if (photoAttached) GreenPrimary else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Field 6: "Cảm xúc"
                FigmaCardTextField(
                    value = emotionInput,
                    onValueChange = { emotionInput = it },
                    placeholder = "Cảm xúc"
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom CTA: "Lưu"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Button(
                    onClick = {
                        val finalPetName = petNameInput.trim().ifEmpty { currentPet.name }
                        val finalDateTime = dateTime.trim().ifEmpty { "26/9/2026-12h30" }
                        val finalActivity = activityType.trim().ifEmpty { "Theo dõi sức khỏe" }
                        val finalDesc = description.trim().ifEmpty { "Tình trạng ổn định" }

                        PetRepository.addMedicalRecord(
                            MedicalRecordEntryModel(
                                id = UUID.randomUUID().toString(),
                                petId = selectedPetId,
                                petName = finalPetName,
                                dateTime = finalDateTime,
                                activityType = finalActivity,
                                description = finalDesc,
                                photoUri = if (photoAttached) "attached" else null,
                                emotion = emotionInput.trim().ifEmpty { "Bình thường" }
                            )
                        )
                        onSaved()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = true
                ) {
                    Text(
                        text = "Lưu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddNewMedicalRecordBookScreenPreview() {
    PetCareTheme {
        AddNewMedicalRecordBookScreen()
    }
}
