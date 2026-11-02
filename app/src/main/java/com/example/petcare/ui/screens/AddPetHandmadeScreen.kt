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
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary
import java.util.UUID

@Composable
fun AddPetHandmadeScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onPetAdded: () -> Unit = {},
) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("Chó") }
    var breed by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("01/01/2024") }
    var weight by remember { mutableStateOf("5.0 kg") }
    var gender by remember { mutableStateOf("Đực") }
    var iconRes by remember { mutableIntStateOf(R.drawable.dog) }

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
                        text = "Thêm thú cưng",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Nhập thông tin thú cưng mới của bạn",
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
                Spacer(modifier = Modifier.height(4.dp))

                // Avatar Selector
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
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
                                    .background(if (isSelected) GreenLight else Color.White)
                                    .border(
                                        2.dp,
                                        if (isSelected) GreenPrimary else BorderLight,
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
                        color = TextSecondary,
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
                                    .background(if (isSelected) GreenLight else Color.White)
                                    .border(
                                        1.dp,
                                        if (isSelected) GreenPrimary else BorderLight,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { gender = g },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = g,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GreenPrimary else TextPrimary
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
                            val newPet = PetModel(
                                id = newId,
                                name = name.trim(),
                                species = species.trim(),
                                breed = breed.trim().ifEmpty { "Chưa rõ giống" },
                                dateOfBirth = dateOfBirth.trim(),
                                age = "1 tuổi",
                                weight = weight.trim().ifEmpty { "5.0 kg" },
                                gender = gender,
                                iconRes = iconRes,
                                statusText = "Khỏe mạnh",
                                statusDetail = "Cân nặng ${weight.trim()}",
                                isFavorite = false,
                                temperature = "38.5°C",
                                activity = "5,000 bước",
                                diet = "Tốt • 2/2 bữa",
                                lastUpdated = "vừa xong"
                            )
                            PetRepository.savePet(newPet)
                            PetRepository.currentSelectedPetId.value = newId
                            onPetAdded()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled = name.isNotBlank()
                ) {
                    Text(
                        text = "Thêm thú cưng",
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
fun AddPetHandmadeScreenPreview() {
    PetCareTheme {
        AddPetHandmadeScreen()
    }
}
