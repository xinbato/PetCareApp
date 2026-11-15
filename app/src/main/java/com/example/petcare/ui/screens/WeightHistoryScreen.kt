package com.example.petcare.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.data.model.WeightRecordModel
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.components.PetCareIcons
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary

@Composable
fun WeightHistoryScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAddWeightClick: () -> Unit = {},
) {
    val allPets = PetRepository.pets
    val weightRecords = PetRepository.weightRecords

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
                        text = "Lịch sử cân nặng",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Theo dõi cân nặng của các bé theo thời gian",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            // Weight List Grouped by Pet
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                allPets.forEach { pet ->
                    val petRecords = weightRecords.filter { it.petId == pet.id }

                    item(key = "header_${pet.id}") {
                        Row(
                            modifier = Modifier
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
                                        .background(GreenLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = pet.iconRes),
                                        contentDescription = pet.name,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = pet.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GreenLight)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Hiện tại: ${pet.weight}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GreenPrimary
                                )
                            }
                        }
                    }

                    if (petRecords.isEmpty()) {
                        item(key = "empty_${pet.id}") {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                            ) {
                                Text(
                                    text = "Chưa có ghi nhận cân nặng cho ${pet.name}",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(petRecords, key = { it.id }) { item ->
                            WeightRecordItemCard(item = item)
                        }
                    }

                    item(key = "space_${pet.id}") {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun WeightRecordItemCard(
    item: WeightRecordModel,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PetCareIcons.WeightScale,
                        contentDescription = "Cân nặng",
                        tint = GreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = item.weight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.dateTime,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (item.status == "Ổn định" || item.status == "Khỏe mạnh") GreenLight else Color(0xFFFFF3E0))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.status == "Ổn định" || item.status == "Khỏe mạnh") GreenPrimary else Color(0xFFE65100)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeightHistoryScreenPreview() {
    PetCareTheme {
        WeightHistoryScreen()
    }
}
