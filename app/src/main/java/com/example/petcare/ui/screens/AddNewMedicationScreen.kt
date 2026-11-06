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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.data.model.MedicationModel
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary
import java.util.UUID
@Composable
fun AddNewMedicationScreen(
    modifier: Modifier = Modifier,
    petId: String = "1",
    onBackClick: () -> Unit = {},
    onSaved: () -> Unit = {},
) {
    var medicationType by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    val commonMeds = listOf("Omega 3", "Men tiêu hóa", "Tẩy giun", "Canxi", "Kháng sinh", "Vitamin C")
    val commonSchedules = listOf("08:00 • mỗi ngày", "12:00 • mỗi ngày", "20:00 • mỗi ngày", "2 lần/ngày", "Cách ngày")
    val commonQuantities = listOf("1 viên", "2 viên", "1/2 viên", "1 gói", "5 ml")
    val commonStatuses = listOf("Đang dùng", "Đã hoàn thành", "Tạm dừng")

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

                // Field 1: "Loại thuốc"
                Column(modifier = Modifier.fillMaxWidth()) {
                    FigmaCardTextField(
                        value = medicationType,
                        onValueChange = { medicationType = it },
                        placeholder = "Loại thuốc"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        commonMeds.forEach { med ->
                            val isSelected = medicationType.equals(med, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) GreenLight else Color.White)
                                    .border(
                                        1.dp,
                                        if (isSelected) GreenPrimary else BorderLight,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { medicationType = med }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = med,
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
                        commonSchedules.forEach { sc ->
                            val isSelected = dateTime.equals(sc, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) GreenLight else Color.White)
                                    .border(
                                        1.dp,
                                        if (isSelected) GreenPrimary else BorderLight,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { dateTime = sc }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sc,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GreenPrimary else TextPrimary
                                )
                            }
                        }
                    }
                }

                // Field 3: "số lượng"
                Column(modifier = Modifier.fillMaxWidth()) {
                    FigmaCardTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        placeholder = "số lượng"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        commonQuantities.forEach { qty ->
                            val isSelected = quantity.equals(qty, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) GreenLight else Color.White)
                                    .border(
                                        1.dp,
                                        if (isSelected) GreenPrimary else BorderLight,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable { quantity = qty }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = qty,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GreenPrimary else TextPrimary
                                )
                            }
                        }
                    }
                }

                // Field 4: "Trạng thái"
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
                        val finalMedType = medicationType.trim().ifEmpty { "Thuốc mới" }
                        val finalDateTime = dateTime.trim().ifEmpty { "08:00 • mỗi ngày" }
                        val finalQuantity = quantity.trim().ifEmpty { "1 liều" }
                        val finalStatus = status.trim().ifEmpty { "Đang dùng" }

                        PetRepository.addMedication(
                            MedicationModel(
                                id = UUID.randomUUID().toString(),
                                petId = petId,
                                name = finalMedType,
                                dosage = finalQuantity,
                                scheduleTime = finalDateTime,
                                quantity = finalQuantity,
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
                    enabled = medicationType.isNotBlank() || true
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

@Composable
fun FigmaCardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary.copy(alpha = 0.8f)
                        )
                    }
                    innerTextField()
                }
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddNewMedicationScreenPreview() {
    PetCareTheme {
        AddNewMedicationScreen()
    }
}
