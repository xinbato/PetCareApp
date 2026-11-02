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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.petcare.R
import com.example.petcare.data.model.PetModel
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.components.PetCareIcons
import com.example.petcare.ui.components.PetCareTextField
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.RedError
import com.example.petcare.ui.theme.RedErrorLight
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary

@Composable
fun EditPetScreen(
    petId: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSaved: () -> Unit = {},
    onDeleted: () -> Unit = {},
) {
    val initialPet = remember(petId) {
        PetRepository.getPetById(petId) ?: PetRepository.getSelectedPet()
    }

    var name by remember { mutableStateOf(initialPet.name) }
    var species by remember { mutableStateOf(initialPet.species) }
    var breed by remember { mutableStateOf(initialPet.breed) }
    var dateOfBirth by remember { mutableStateOf(initialPet.dateOfBirth) }
    var weight by remember { mutableStateOf(initialPet.weight) }
    var gender by remember { mutableStateOf(initialPet.gender) }
    var iconRes by remember { mutableStateOf(initialPet.iconRes) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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
            // Top Bar
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
                        text = "Chỉnh sửa thú cưng",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Cập nhật thông tin ${initialPet.name}",
                        fontSize = 13.sp,
                        color = TextSecondary
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
                Spacer(modifier = Modifier.height(8.dp))

                // Field 1: Tên thú cưng
                PetCareTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Tên thú cưng",
                    placeholder = "Nhập tên thú cưng"
                )

                // Field 2: Loài
                PetCareTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = "Loài",
                    placeholder = "Chó, Mèo, Thỏ..."
                )

                // Field 3: Giống
                PetCareTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = "Giống",
                    placeholder = "Golden Retriever, Poodle..."
                )

                // Field 4: Ngày sinh
                PetCareTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = "Ngày sinh",
                    placeholder = "DD/MM/YYYY",
                    trailingIcon = {
                        Icon(
                            imageVector = PetCareIcons.Calendar,
                            contentDescription = "Chọn ngày",
                            tint = GreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                // Field 5: Cân nặng
                PetCareTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = "Cân nặng",
                    placeholder = "Ví dụ: 12.4 kg"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Button: Lưu thay đổi
                Button(
                    onClick = {
                        val updatedPet = initialPet.copy(
                            name = name,
                            species = species,
                            breed = breed,
                            dateOfBirth = dateOfBirth,
                            weight = weight,
                            gender = gender,
                            iconRes = iconRes,
                            statusDetail = "Cân nặng $weight"
                        )
                        PetRepository.savePet(updatedPet)
                        onSaved()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text(
                        text = "Lưu thay đổi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Secondary Button: Xóa thú cưng (matching Figma 13.1)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                        .clickable { showDeleteConfirmDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Xóa thú cưng",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GreenPrimary
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Xóa thú cưng",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn xóa bé ${initialPet.name} không? Dữ liệu này không thể khôi phục.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        PetRepository.deletePet(initialPet.id)
                        showDeleteConfirmDialog = false
                        onDeleted()
                    }
                ) {
                    Text("Xóa", color = RedError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Hủy", color = TextSecondary)
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditPetScreenPreview() {
    PetCareTheme {
        EditPetScreen(petId = "1")
    }
}
