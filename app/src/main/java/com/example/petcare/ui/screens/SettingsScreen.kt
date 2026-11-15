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
import androidx.compose.material3.OutlinedButton
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
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.RedError
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSwitchAccountClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

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
            // Header: "< Cài đặt" matching Image 4
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    text = "Cài đặt",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Scrollable Settings List matching Image 4
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Item 1: Thông báo
                SettingCardItem(title = "Thông báo")

                // Item 2: Quyền riêng tư
                SettingCardItem(title = "Quyền riêng tư")

                // Item 3: Ngôn ngữ
                SettingCardItem(title = "Ngôn ngữ", subtitle = "Tiếng Việt")

                // Item 4: Đơn vị cân nặng
                SettingCardItem(title = "Đơn vị cân nặng", subtitle = "kg")

                // Item 5: Giao diện
                SettingCardItem(title = "Giao diện", subtitle = "Sáng")

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Option 1: Đổi tài khoản (Color: GREEN as requested)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.5.dp, GreenPrimary, RoundedCornerShape(16.dp))
                        .clickable { onSwitchAccountClick() }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Đổi tài khoản",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }

                // Bottom Option 2: Xóa tài khoản (Color: RED as requested)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.5.dp, RedError, RoundedCornerShape(16.dp))
                        .clickable { showDeleteAccountDialog = true }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Xóa tài khoản",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedError
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Delete Account Confirmation Dialog
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountDialog = false },
                title = { Text("Xác nhận xóa tài khoản", fontWeight = FontWeight.Bold, color = RedError) },
                text = { Text("Bạn có chắc chắn muốn xóa tài khoản này không? Mọi dữ liệu về thú cưng sẽ bị xóa vĩnh viễn.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteAccountDialog = false
                            onLogoutClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedError, contentColor = Color.White)
                    ) {
                        Text("Xóa tài khoản", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteAccountDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingCardItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = TextPrimary
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    PetCareTheme {
        SettingsScreen()
    }
}
