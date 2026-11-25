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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.data.model.PetModel
import com.example.petcare.ui.components.HealthSkeleton
import com.example.petcare.ui.components.PetAvatar
import com.example.petcare.ui.components.PetCareAdaptiveTabScaffold
import com.example.petcare.ui.components.PetCareIcons
import com.example.petcare.ui.components.PetCareTab
import com.example.petcare.ui.components.SmoothSkeletonWrapper
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.viewmodel.HealthViewModel

data class HealthHistoryItem(
    val id: String,
    val title: String,
    val date: String,
    val icon: ImageVector,
)

@Composable
fun HealthScreen(
    modifier: Modifier = Modifier,
    viewModel: HealthViewModel = viewModel(),
    onPetClick: (PetModel) -> Unit = {},
    onHistoryItemClick: ((HealthHistoryItem) -> Unit)? = null,
    onTabSelected: (PetCareTab) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pets = uiState.pets
    val colorScheme = MaterialTheme.colorScheme

    val lastVaccineDate = uiState.vaccinations.firstOrNull()?.dateTime?.ifBlank { null } ?: "Chưa có bản ghi"
    val lastWeightInfo = uiState.weightRecords.firstOrNull()?.let { "${it.weight} • ${it.dateTime}" } ?: "Chưa có bản ghi"
    val lastCheckupDate = uiState.checkupRecords.firstOrNull()?.dateTime?.ifBlank { null } ?: "Chưa có bản ghi"

    val healthHistoryList = listOf(
        HealthHistoryItem("h1", "Tiêm phòng", lastVaccineDate, PetCareIcons.Syringe),
        HealthHistoryItem("h2", "Cân nặng", lastWeightInfo, PetCareIcons.WeightScale),
        HealthHistoryItem("h3", "Khám định kỳ", lastCheckupDate, PetCareIcons.Stethoscope),
    )

    PetCareAdaptiveTabScaffold(
        currentTab = PetCareTab.HEALTH,
        onTabSelected = onTabSelected,
        modifier = modifier,
    ) { innerPadding ->
        SmoothSkeletonWrapper(
            isLoading = uiState.isLoading && pets.isEmpty(),
            skeleton = {
                HealthSkeleton(
                    modifier = Modifier
                        .padding(innerPadding)
                        .statusBarsPadding(),
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Sức khỏe",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Theo dõi sức khỏe của từng bé",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (pets.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            ) {
                                Text(
                                    text = "Chưa có thông tin thú cưng nào. Vui lòng thêm thú cưng để theo dõi sức khỏe.",
                                    fontSize = 14.sp,
                                    color = colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    } else {
                        items(pets, key = { it.id }) { pet ->
                            HealthPetCard(
                                pet = pet,
                                onClick = {
                                    viewModel.selectPet(pet.id)
                                    onPetClick(pet)
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Lịch sử sức khỏe",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    items(healthHistoryList, key = { it.id }) { item ->
                        HealthHistoryCard(
                            item = item,
                            onClick = {
                                onHistoryItemClick?.invoke(item)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
@Composable
fun HealthPetCard(
    pet: PetModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                PetAvatar(
                    photoUri = pet.photoUri,
                    fallbackIconRes = pet.safeIconRes,
                    contentDescription = pet.name,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pet.statusText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (pet.statusText == "Khỏe mạnh") colorScheme.primary else colorScheme.error
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pet.statusDetail.ifEmpty { "Cân nặng ${pet.weight}" },
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
@Composable
fun HealthHistoryCard(
    item: HealthHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
            }
            Text(
                text = item.date,
                fontSize = 13.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HealthScreenPreview() {
    PetCareTheme {
        HealthScreen()
    }
}
