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
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.components.PetCareIcons
import com.example.petcare.ui.components.PetCareTab
import com.example.petcare.ui.components.PetCareTabBar
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary

@Composable
fun HealthDetailScreen(
    modifier: Modifier = Modifier,
    petId: String = "1",
    onBackClick: () -> Unit = {},
    onVaccinationClick: (String) -> Unit = {},
    onMedicationClick: (String) -> Unit = {},
    onMedicalRecordBookClick: (String) -> Unit = {},
    onEditPetClick: (String) -> Unit = {},
    onTabSelected: (PetCareTab) -> Unit = {},
) {
    val pet = PetRepository.getPetById(petId) ?: PetRepository.getSelectedPet()

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
                        text = "Chi tiết sức khỏe",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Theo dõi chỉ số của ${pet.name}",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Top Pet Status Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(GreenLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = pet.iconRes),
                                    contentDescription = pet.name,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "${pet.name} • ${pet.statusText}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Cập nhật gần nhất: ${pet.lastUpdated}",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Metric Card 1: Cân nặng
                HealthMetricRowCard(
                    label = "Cân nặng",
                    value = "${pet.weight} • ổn định"
                )

                // Metric Card 2: Nhiệt độ
                HealthMetricRowCard(
                    label = "Nhiệt độ",
                    value = pet.temperature
                )

                // Metric Card 3: Hoạt động
                HealthMetricRowCard(
                    label = "Hoạt động",
                    value = pet.activity
                )

                // Metric Card 4: Ăn uống
                HealthMetricRowCard(
                    label = "Ăn uống",
                    value = pet.diet
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Action buttons: Tiêm phòng & Thuốc side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                            .clickable { onVaccinationClick(pet.id) }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = PetCareIcons.Syringe,
                                    contentDescription = "Tiêm phòng",
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tiêm phòng",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                            .clickable { onMedicationClick(pet.id) }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = PetCareIcons.Pill,
                                    contentDescription = "Thuốc",
                                    tint = GreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Thuốc",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // Full-width button: Xem sổ bệnh
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                        .clickable { onMedicalRecordBookClick(pet.id) }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Xem sổ bệnh",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom Navigation Bar docked
            PetCareTabBar(
                currentTab = PetCareTab.HEALTH,
                onTabSelected = onTabSelected,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun HealthMetricRowCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HealthDetailScreenPreview() {
    PetCareTheme {
        HealthDetailScreen()
    }
}
