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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.R
import com.example.petcare.data.model.PetModel
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.components.PetCareIcons
import com.example.petcare.ui.components.PetCareTextField
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.viewmodel.PetManagementViewModel

@Composable
fun EditPetScreen(
    petId: String,
    modifier: Modifier = Modifier,
    viewModel: PetManagementViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSaved: () -> Unit = {},
    onDeleted: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val initialPet = remember(petId, uiState.pets) {
        uiState.pets.find { it.id == petId } ?: PetModel(
            id = petId,
            name = "",
            iconRes = R.drawable.cat,
        )
    }

    var name by remember(initialPet.id, initialPet.name) { mutableStateOf(initialPet.name) }
    var species by remember(initialPet.id, initialPet.species) { mutableStateOf(initialPet.species) }
    var breed by remember(initialPet.id, initialPet.breed) { mutableStateOf(initialPet.breed) }
    var dateOfBirth by remember(initialPet.id, initialPet.dateOfBirth) { mutableStateOf(initialPet.dateOfBirth) }
    var weight by remember(initialPet.id, initialPet.weight) { mutableStateOf(initialPet.weight) }
    var gender by remember(initialPet.id, initialPet.gender) { mutableStateOf(initialPet.gender) }
    var iconRes by remember(initialPet.id, initialPet.iconRes) { mutableIntStateOf(initialPet.iconRes) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = KeyboardArrowLeftIcon,
                        contentDescription = "Quay lại",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Chỉnh sửa thú cưng",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Cập nhật thông tin ${initialPet.name}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Scrollable Form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Field 1: Tên thú cưng
                PetCareTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Tên thú cưng",
                    placeholder = "Nhập tên thú cưng",
                )

                // Field 2: Loài
                PetCareTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = "Loài",
                    placeholder = "Chó, Mèo, Thỏ...",
                )

                // Field 3: Giống
                PetCareTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = "Giống",
                    placeholder = "Golden Retriever, Poodle...",
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
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                )

                // Field 5: Cân nặng
                PetCareTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = "Cân nặng",
                    placeholder = "Ví dụ: 12.4 kg",
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Button: Lưu thay đổi
                Button(
                    onClick = {
                        val currentPet = uiState.pets.find { it.id == petId } ?: initialPet
                        val updatedPet = currentPet.copy(
                            id = petId,
                            name = name,
                            species = species,
                            breed = breed,
                            dateOfBirth = dateOfBirth,
                            weight = weight,
                            gender = gender,
                            iconRes = iconRes,
                            statusDetail = "Cân nặng $weight",
                        )
                        viewModel.savePet(updatedPet)
                        onSaved()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text(
                        text = "Lưu thay đổi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }

                // Secondary Button: Xóa thú cưng
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .clickable { showDeleteConfirmDialog = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Xóa thú cưng",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
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
                    fontSize = 18.sp,
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn xóa bé ${initialPet.name} không? Dữ liệu này không thể khôi phục.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePet(petId)
                        showDeleteConfirmDialog = false
                        onDeleted()
                    },
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Hủy", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
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
