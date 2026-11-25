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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.data.model.VaccinationModel
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.components.PetCareIcons
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import com.example.petcare.ui.components.PetAvatar
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.viewmodel.HealthViewModel

@Composable
fun VaccinationScreen(
    modifier: Modifier = Modifier,
    petId: String = "all",
    viewModel: HealthViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddVaccinationClick: (String) -> Unit = {},
    onViewInCalendarClick: () -> Unit = {},
) {
    val isAllPets = petId == "all" || petId.isBlank()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allPets = uiState.pets
    val vaccinations = uiState.vaccinations
    val singlePet = if (!isAllPets) allPets.find { it.id == petId } ?: uiState.currentPet else null

    val colorScheme = MaterialTheme.colorScheme

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
                        text = "Tiêm phòng",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Text(
                        text = if (isAllPets) "Lịch sử và lịch nhắc vaccine của các bé" else "Lịch sử và lịch nhắc vaccine của ${singlePet?.name}",
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            // Vaccination List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (isAllPets) {
                    if (allPets.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            ) {
                                Text(
                                    text = "Chưa có thông tin thú cưng nào. Vui lòng thêm thú cưng để theo dõi lịch tiêm phòng.",
                                    fontSize = 14.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    } else {
                        allPets.forEach { pet ->
                            val petVaccinations = vaccinations.filter { it.petId == pet.id }

                            item(key = "header_${pet.id}") {
                                PetVaccinationSectionHeader(
                                    petName = pet.name,
                                    species = pet.species,
                                    iconRes = pet.safeIconRes,
                                    photoUri = pet.photoUri,
                                    count = petVaccinations.size
                                )
                            }

                            if (petVaccinations.isEmpty()) {
                                item(key = "empty_${pet.id}") {
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Chưa có lịch tiêm chủng cho ${pet.name}",
                                            fontSize = 13.sp,
                                            color = colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            } else {
                                items(petVaccinations, key = { it.id }) { item ->
                                    VaccinationItemCard(
                                        item = item,
                                        onToggleCompleted = { viewModel.toggleVaccinationCompleted(item) },
                                        onDelete = { viewModel.deleteVaccination(item.id) },
                                    )
                                }
                            }

                            item(key = "space_${pet.id}") {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                } else {
                    val petVaccinations = vaccinations.filter { it.petId == singlePet?.id }
                    if (petVaccinations.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            ) {
                                Text(
                                    text = "Chưa có lịch tiêm chủng cho ${singlePet?.name ?: "bé"}.",
                                    fontSize = 14.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    } else {
                        items(petVaccinations, key = { it.id }) { item ->
                            VaccinationItemCard(
                                item = item,
                                onToggleCompleted = { viewModel.toggleVaccinationCompleted(item) },
                                onDelete = { viewModel.deleteVaccination(item.id) },
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Buttons
            if (!isAllPets) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onAddVaccinationClick(singlePet?.id ?: uiState.selectedPetId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = PetCareIcons.Plus,
                                contentDescription = "Thêm",
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Thêm mũi tiêm",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onPrimary
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            .clickable(onClick = onViewInCalendarClick)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Xem trong lịch",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PetVaccinationSectionHeader(
    petName: String,
    iconRes: Int,
    count: Int,
    modifier: Modifier = Modifier,
    species: String = "",
    photoUri: String = "",
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                PetAvatar(
                    photoUri = photoUri,
                    fallbackIconRes = iconRes,
                    contentDescription = petName,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = petName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.primaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$count mũi tiêm",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.primary
            )
        }
    }
}

@Composable
fun VaccinationItemCard(
    item: VaccinationModel,
    modifier: Modifier = Modifier,
    onToggleCompleted: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDone = item.completed || item.status == "Đã tiêm"
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (onToggleCompleted != null) {
                    IconButton(onClick = onToggleCompleted, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isDone) androidx.compose.material.icons.Icons.Default.CheckCircle else androidx.compose.material.icons.Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (isDone) "Đã tiêm" else "Đánh dấu đã tiêm",
                            tint = if (isDone) colorScheme.primary else colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PetCareIcons.Syringe,
                            contentDescription = item.title,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.dateTime,
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDone) colorScheme.primaryContainer else colorScheme.tertiaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDone) colorScheme.primary else colorScheme.tertiary
                    )
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.DeleteOutline,
                            contentDescription = "Xóa",
                            tint = colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VaccinationScreenPreview() {
    PetCareTheme {
        VaccinationScreen(petId = "all")
    }
}
