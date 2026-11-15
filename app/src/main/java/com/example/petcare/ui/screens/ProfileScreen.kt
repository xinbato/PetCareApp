package com.example.petcare.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.ui.components.PetCareTab
import com.example.petcare.ui.components.PetCareTabBar
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary

@Composable
fun rememberBitmapFromUri(uriString: String?): ImageBitmap? {
    val context = LocalContext.current
    return remember(uriString) {
        if (uriString.isNullOrEmpty()) return@remember null
        try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }
}

private val HelpQuestionIcon: ImageVector by lazy {
    ImageVector.Builder("HelpQuestionIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFFE53935))) {
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveToRelative(0f, 5.52f, 4.48f, 10f, 10f, 10f)
            curveToRelative(5.52f, 0f, 10f, -4.48f, 10f, -10f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            moveTo(13f, 18f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            close()
            moveTo(13.07f, 13.25f)
            lineToRelative(-0.9f, 0.92f)
            curveTo(11.45f, 14.9f, 11f, 15.5f, 11f, 17f)
            horizontalLineToRelative(2f)
            curveToRelative(0f, -1.1f, 0.45f, -1.55f, 1.17f, -2.27f)
            lineToRelative(1.24f, -1.26f)
            curveTo(15.78f, 13.09f, 16f, 12.59f, 16f, 11.5f)
            curveTo(16f, 9.29f, 14.21f, 7.5f, 12f, 7.5f)
            curveTo(9.79f, 7.5f, 8f, 9.29f, 8f, 11.5f)
            horizontalLineToRelative(2f)
            curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
            curveToRelative(1.1f, 0f, 2f, 0.9f, 2f, 2f)
            curveToRelative(0f, 0.55f, -0.22f, 1.05f, -0.93f, 1.75f)
            close()
        }
    }.build()
}

private val PawIcon: ImageVector by lazy {
    ImageVector.Builder("PawIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(GreenPrimary)) {
            moveTo(12f, 11.5f)
            curveToRelative(-1.93f, 0f, -3.5f, 1.57f, -3.5f, 3.5f)
            curveToRelative(0f, 1.93f, 1.57f, 3.5f, 3.5f, 3.5f)
            curveToRelative(1.93f, 0f, 3.5f, -1.57f, 3.5f, -3.5f)
            curveToRelative(0f, -1.93f, -1.57f, -3.5f, -3.5f, -3.5f)
            close()
            moveTo(5f, 9.5f)
            curveTo(3.9f, 9.5f, 3f, 10.4f, 3f, 11.5f)
            curveTo(3f, 12.6f, 3.9f, 13.5f, 5f, 13.5f)
            curveTo(6.1f, 13.5f, 7f, 12.6f, 7f, 11.5f)
            curveTo(7f, 10.4f, 6.1f, 9.5f, 5f, 9.5f)
            close()
            moveTo(19f, 9.5f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            moveTo(8.5f, 4.5f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            moveTo(15.5f, 4.5f)
            curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
        }
    }.build()
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onUserProfileDetailClick: () -> Unit = {},
    onPersonalInfoClick: () -> Unit = {},
    onNotificationRemindersClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHelpSupportClick: () -> Unit = {},
    onMyPetsClick: () -> Unit = {},
    onTabSelected: (PetCareTab) -> Unit = {},
) {
    val userProfile = PetRepository.userProfile.value
    val petCount = PetRepository.pets.size

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
            // Scrollable Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Title: Tôi
                Text(
                    text = "Tôi",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle: Quản lý tài khoản và cài đặt
                Text(
                    text = "Quản lý tài khoản và cài đặt",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Green Banner Card (User Info)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onUserProfileDetailClick() },
                    color = GreenPrimary,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circle Avatar
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFDF8E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = GreenPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = userProfile.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$petCount thú cưng • ${userProfile.memberSince}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Options List matching Image 1
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Option 1: Thông tin cá nhân
                    ProfileMenuItem(
                        icon = Icons.Default.Person,
                        iconTint = Color(0xFF4B6984),
                        title = "Thông tin cá nhân",
                        onClick = onPersonalInfoClick
                    )

                    // Option 2: Thông báo & nhắc lịch
                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        iconTint = Color(0xFFD99B26),
                        title = "Thông báo & nhắc lịch",
                        onClick = onNotificationRemindersClick
                    )

                    // Option 3: Cài đặt
                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        iconTint = Color(0xFF6B7280),
                        title = "Cài đặt",
                        onClick = onSettingsClick
                    )

                    // Option 4: Trợ giúp & hỗ trợ
                    ProfileMenuItem(
                        icon = HelpQuestionIcon,
                        iconTint = Color(0xFFE53935),
                        title = "Trợ giúp & hỗ trợ",
                        onClick = onHelpSupportClick
                    )

                    // Option 5: Thú cưng của tôi
                    ProfileMenuItem(
                        icon = PawIcon,
                        iconTint = GreenPrimary,
                        title = "Thú cưng của tôi",
                        titleColor = GreenPrimary,
                        showChevron = false,
                        onClick = onMyPetsClick
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }

            // Bottom Navigation Bar
            PetCareTabBar(
                currentTab = PetCareTab.PROFILE,
                onTabSelected = onTabSelected
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    titleColor: Color = TextPrimary,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                modifier = Modifier.weight(1f)
            )

            if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    PetCareTheme {
        ProfileScreen()
    }
}
