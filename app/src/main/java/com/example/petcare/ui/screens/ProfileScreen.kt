package com.example.petcare.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.petcare.R
import com.example.petcare.data.model.resolvePetIconRes
import com.example.petcare.ui.components.PetCareAdaptiveTabScaffold
import com.example.petcare.ui.components.PetCareTab
import com.example.petcare.ui.components.ProfileSkeleton
import com.example.petcare.ui.components.SmoothSkeletonWrapper
import com.example.petcare.ui.theme.PetCareTheme
import androidx.compose.runtime.produceState
import com.example.petcare.ui.viewmodel.AuthViewModel
import com.example.petcare.ui.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun rememberBitmapFromUri(uriString: String?): ImageBitmap? {
    val context = LocalContext.current
    val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = uriString) {
        if (uriString.isNullOrEmpty()) {
            value = null
            return@produceState
        }
        value = withContext(Dispatchers.IO) {
            runCatching {
                if (uriString.startsWith("http://", ignoreCase = true) ||
                    uriString.startsWith("https://", ignoreCase = true)
                ) {
                    val conn = (URL(uriString).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 8000
                        readTimeout = 8000
                        doInput = true
                    }
                    conn.inputStream.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                } else if (uriString.startsWith("/")) {
                    BitmapFactory.decodeFile(uriString)?.asImageBitmap()
                } else {
                    val uri = Uri.parse(uriString)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }
            }.getOrNull()
        }
    }
    return bitmapState.value
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
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
    val colorScheme = MaterialTheme.colorScheme

    val context = LocalContext.current
    val avatarBitmap = rememberBitmapFromUri(userProfile.avatarUri)
    val avatarRes = userProfile.avatarRes?.let { resolvePetIconRes(it) }
        ?.takeIf { userProfile.avatarRes == R.drawable.dog || userProfile.avatarRes == R.drawable.cat || userProfile.avatarRes == R.drawable.rabbit }
    val firebaseUser = remember(userProfile.email, userProfile.name, userProfile.isAnonymous) {
        runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull()
    }
    var isGuestUser by remember(firebaseUser?.uid, userProfile.isAnonymous) {
        mutableStateOf(firebaseUser?.isAnonymous == true || userProfile.isAnonymous)
    }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var upgradeFullName by remember(userProfile.name) { mutableStateOf(userProfile.name.takeUnless { it == "Khách" }.orEmpty()) }
    var upgradeEmail by remember { mutableStateOf("") }
    var upgradePassword by remember { mutableStateOf("") }
    var upgradeConfirm by remember { mutableStateOf("") }
    var upgradeError by remember { mutableStateOf<String?>(null) }

    PetCareAdaptiveTabScaffold(
        currentTab = PetCareTab.PROFILE,
        onTabSelected = onTabSelected,
        modifier = modifier,
    ) { innerPadding ->
        SmoothSkeletonWrapper(
            isLoading = uiState.isLoading && uiState.myPets.isEmpty(),
            skeleton = {
                ProfileSkeleton(
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Title: Tôi
                Text(
                    text = "Tôi",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle: Quản lý tài khoản và cài đặt
                Text(
                    text = "Quản lý tài khoản và cài đặt",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Green Banner Card (User Info)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onUserProfileDetailClick() },
                    color = colorScheme.primary,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                avatarBitmap != null -> Image(
                                    bitmap = avatarBitmap,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                avatarRes != null -> Image(
                                    painter = painterResource(id = avatarRes),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(40.dp),
                                )
                                else -> Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(36.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = userProfile.name.ifBlank { if (isGuestUser) "Tài khoản Khách" else "Người dùng" },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$petCount thú cưng • ${if (isGuestUser) "Khách dùng thử" else userProfile.memberSince}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                if (isGuestUser) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                        color = colorScheme.primaryContainer.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Bạn đang dùng tài khoản Khách",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                            )
                            Text(
                                text = "Liên kết Email để lưu dữ liệu an toàn và đồng bộ trên nhiều thiết bị.",
                                fontSize = 13.sp,
                                color = colorScheme.onSurfaceVariant,
                            )
                            Button(
                                onClick = {
                                    upgradeError = null
                                    showUpgradeDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text("Liên kết Email ngay", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProfileMenuItem(
                        icon = Icons.Default.Person,
                        iconTint = colorScheme.primary,
                        title = "Thông tin cá nhân",
                        onClick = onPersonalInfoClick
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        iconTint = colorScheme.primary,
                        title = "Thông báo & nhắc lịch",
                        onClick = onNotificationRemindersClick
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.Settings,
                        iconTint = colorScheme.onSurfaceVariant,
                        title = "Cài đặt",
                        onClick = onSettingsClick
                    )

                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        iconTint = colorScheme.error,
                        title = "Trợ giúp & hỗ trợ",
                        onClick = onHelpSupportClick
                    )

                    ProfileMenuItem(
                        icon = Icons.Default.Pets,
                        iconTint = colorScheme.primary,
                        title = "Thú cưng của tôi",
                        titleColor = colorScheme.primary,
                        showChevron = false,
                        onClick = onMyPetsClick
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = { Text("Liên kết tài khoản Email", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = upgradeFullName,
                        onValueChange = { upgradeFullName = it },
                        label = { Text("Họ và tên") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = upgradeEmail,
                        onValueChange = { upgradeEmail = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = upgradePassword,
                        onValueChange = { upgradePassword = it },
                        label = { Text("Mật khẩu (>= 6 ký tự)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = upgradeConfirm,
                        onValueChange = { upgradeConfirm = it },
                        label = { Text("Xác nhận mật khẩu") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    upgradeError?.let { err ->
                        Text(
                            text = err,
                            color = colorScheme.error,
                            fontSize = 12.sp,
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.linkGuestToEmail(
                            fullName = upgradeFullName,
                            email = upgradeEmail,
                            password = upgradePassword,
                            confirmPassword = upgradeConfirm,
                            onSuccess = {
                                isGuestUser = false
                                showUpgradeDialog = false
                                Toast.makeText(context, "Đã liên kết tài khoản Email.", Toast.LENGTH_SHORT).show()
                            },
                            onError = { msg -> upgradeError = msg },
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                ) {
                    Text("Liên kết")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUpgradeDialog = false }) {
                    Text("Hủy")
                }
            },
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    titleColor: Color = Color.Unspecified,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val resolvedTitleColor = if (titleColor != Color.Unspecified) titleColor else colorScheme.onSurface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colorScheme.surface)
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(18.dp))
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
                color = resolvedTitleColor,
                modifier = Modifier.weight(1f)
            )

            if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
