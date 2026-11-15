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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.ui.components.PetCareTab
import com.example.petcare.ui.components.PetCareTabBar
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary
import com.example.petcare.ui.viewmodel.ProfileViewModel

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

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
    onUserProfileDetailClick: () -> Unit = {},
    onPersonalInfoClick: () -> Unit = {},
    onNotificationRemindersClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onHelpSupportClick: () -> Unit = {},
    onMyPetsClick: () -> Unit = {},
    onTabSelected: (PetCareTab) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userProfile = uiState.userProfile
    val petCount = uiState.petCount

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
                                .background(MaterialTheme.colorScheme.surfaceVariant),
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
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Thông tin cá nhân",
                        onClick = onPersonalInfoClick
                    )

                    // Option 2: Thông báo & nhắc lịch
                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        title = "Thông báo & nhắc lịch",
                        onClick = onNotificationRemindersClick
                    )

                    // Option 3: Cài đặt
                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        title = "Cài đặt",
                        onClick = onSettingsClick
                    )

                    // Option 4: Trợ giúp & hỗ trợ
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        iconTint = MaterialTheme.colorScheme.error,
                        title = "Trợ giúp & hỗ trợ",
                        onClick = onHelpSupportClick
                    )

                    // Option 5: Thú cưng của tôi
                    ProfileMenuItem(
                        icon = Icons.Default.Pets,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Thú cưng của tôi",
                        titleColor = MaterialTheme.colorScheme.primary,
                        showChevron = false,
                        onClick = onMyPetsClick
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }

            // Bottom Navigation Bar
            PetCareTabBar(
                selectedTab = PetCareTab.PROFILE.ordinal,
                onTabSelected = { index -> onTabSelected(PetCareTab.entries[index]) }
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
