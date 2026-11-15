package com.example.petcare.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.data.model.VaccinationModel
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import java.util.UUID
@Composable
fun AddNewVaccinationScreen(
    modifier: Modifier = Modifier,
    petId: String = "1",
    onBackClick: () -> Unit = {},
    onSaved: () -> Unit = {},
) {
    var content by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    val commonVaccines = listOf("Dại", "5 bệnh", "7 bệnh", "Kennel Cough", "Giảm bạch cầu")
    val commonDateTimes = listOf("15/09/2026", "Hôm nay", "Ngày mai", "Tuần sau", "Tháng sau")
    val commonStatuses = listOf("Đã tiêm", "Sắp tới", "Đến hẹn", "Chờ nhắc")

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

                // Field 1: "Nội dung tiêm"
                Column(modifier = Modifier.fillMaxWidth()) {
                    FigmaCardTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = "Nội dung tiêm"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        commonVaccines.forEach { vaccine ->
                            val isSelected = content.equals(vaccine, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) GreenLight else Color.White)
                                    .border(
                                        1.dp,
                                        if (isSelected) GreenPrimary else BorderLight,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { content = vaccine }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = vaccine,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GreenPrimary else TextPrimary
                                )
                            }
                        }
                    }
                }

                // Field 2: "Ngày & giờ"
                Column(modifier = Modifier.fillMaxWidth()) {
                    FigmaCardTextField(
                        value = dateTime,
                        onValueChange = { dateTime = it },
                        placeholder = "Ngày & giờ"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        commonDateTimes.forEach { dt ->
                            val isSelected = dateTime.equals(dt, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) GreenLight else Color.White)
                                    .border(
                                        1.dp,
                                        if (isSelected) GreenPrimary else BorderLight,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { dateTime = dt }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dt,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GreenPrimary else TextPrimary
                                )
                            }
                        }
                    }
                }

                // Field 3: "Trạng thái"
                Column(modifier = Modifier.fillMaxWidth()) {
                    FigmaCardTextField(
                        value = status,
                        onValueChange = { status = it },
                        placeholder = "Trạng thái"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        commonStatuses.forEach { st ->
                            val isSelected = status.equals(st, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) GreenLight else Color.White)
                                    .border(
                                        1.dp,
                                        if (isSelected) GreenPrimary else BorderLight,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { status = st }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = st,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GreenPrimary else TextPrimary
                                )
                            }
                        }
                    }
                }

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
                        val finalContent = content.trim().ifEmpty { "Vaccine mới" }
                        val finalDateTime = dateTime.trim().ifEmpty { "15/09/2026" }
                        val finalStatus = status.trim().ifEmpty { "Đã tiêm" }

                        PetRepository.addVaccination(
                            VaccinationModel(
                                id = UUID.randomUUID().toString(),
                                petId = petId,
                                title = finalContent,
                                dateTime = finalDateTime,
                                status = finalStatus
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
fun AddNewVaccinationScreenPreview() {
    PetCareTheme {
        AddNewVaccinationScreen()
    }
}
