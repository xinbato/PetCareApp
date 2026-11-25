package com.example.petcare.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.R
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.components.PetAvatar
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.viewmodel.ProfileViewModel
import com.example.petcare.utils.PetPhotoStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onPetClick: (String) -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userProfile = uiState.userProfile
    val pets = uiState.myPets
    val accounts = uiState.accounts

    var showAccountBottomSheet by remember { mutableStateOf(false) }
    var showEditChoiceDialog by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var showCoverPicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val avatarBitmap = rememberBitmapFromUri(userProfile.avatarUri)
    val coverBitmap = rememberBitmapFromUri(userProfile.headerBgUri)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Activity Result Launchers for Gallery selection (copied to internal storage for persistence)
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { picked ->
            coroutineScope.launch {
                val savedUri = withContext(Dispatchers.IO) {
                    PetPhotoStorage.copyUriToInternalStorage(context, picked)
                }
                if (!savedUri.isNullOrBlank()) {
                    viewModel.updateUserProfile(avatarUri = savedUri)
                }
            }
        }
    }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { picked ->
            coroutineScope.launch {
                val savedUri = withContext(Dispatchers.IO) {
                    PetPhotoStorage.copyUriToInternalStorage(context, picked)
                }
                if (!savedUri.isNullOrBlank()) {
                    viewModel.updateUserProfile(headerBgUri = savedUri)
                }
            }
        }
    }

    val currentHeaderBgColor = Color(userProfile.headerBgColorHex)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Banner with background image or color matching Image 2
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(currentHeaderBgColor)
                    .statusBarsPadding()
            ) {
                if (coverBitmap != null) {
                    Image(
                        bitmap = coverBitmap,
                        contentDescription = "Ảnh bìa",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Header Bar (Back button & Settings icon)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = KeyboardArrowLeftIcon,
                            contentDescription = "Quay lại",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Settings Gear Button
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cài đặt",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Avatar overlapping bottom of header
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 0.dp)
                ) {
                    // Circle Avatar Container
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            val avatarRes = userProfile.avatarRes?.takeIf {
                                it == R.drawable.dog || it == R.drawable.cat || it == R.drawable.rabbit
                            }
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (avatarRes != null) {
                                Image(
                                    painter = painterResource(id = avatarRes),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                    }

                    // Edit Pencil Button ✎
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .clickable {
                                // Show choice popup asking to change Avatar or Cover
                                showEditChoiceDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Sửa ảnh đại diện hoặc ảnh bìa",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Name & Subtitle matching Image 2 & 5
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = userProfile.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userProfile.rank,
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    textDecoration = TextDecoration.Underline,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Pet Count Card matching Image 2
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = "Số lượng thú cưng: ${pets.size}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // List of Pet Cards matching Image 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    pets.forEach { pet ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(130.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                                .clickable { onPetClick(pet.id) }
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                PetAvatar(
                                    photoUri = pet.photoUri,
                                    fallbackIconRes = pet.safeIconRes,
                                    contentDescription = pet.name,
                                    modifier = Modifier.size(52.dp).clip(CircleShape),
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = pet.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Single "Đổi tài khoản" Button
                Button(
                    onClick = { showAccountBottomSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Đổi tài khoản",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // 1. Popup "Khung nhỏ" asking Change Avatar or Change Cover Photo
        if (showEditChoiceDialog) {
            AlertDialog(
                onDismissRequest = { showEditChoiceDialog = false },
                title = {
                    Text(
                        text = "Chỉnh sửa hình ảnh",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Bạn muốn thay đổi ảnh đại diện hay thay đổi ảnh bìa?",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Option 1: Đổi ảnh đại diện
                        Button(
                            onClick = {
                                showEditChoiceDialog = false
                                showAvatarPicker = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "Đổi ảnh đại diện",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Option 2: Thay đổi ảnh bìa
                        Button(
                            onClick = {
                                showEditChoiceDialog = false
                                showCoverPicker = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "Thay đổi ảnh bìa",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    OutlinedButton(
                        onClick = { showEditChoiceDialog = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hủy", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }

        // 2. Avatar Picker Modal / Options
        if (showAvatarPicker) {
            AlertDialog(
                onDismissRequest = { showAvatarPicker = false },
                title = { Text("Đổi ảnh đại diện", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Button: Pick from gallery
                        Button(
                            onClick = {
                                showAvatarPicker = false
                                avatarPickerLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Chọn ảnh từ thư viện thiết bị", fontWeight = FontWeight.Bold)
                        }

                        Text("— Hoặc chọn ảnh mẫu có sẵn —", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        // Sample Preset Avatars
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Preset 1: Dog
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable {
                                        viewModel.updateUserProfile(avatarRes = R.drawable.dog)
                                        showAvatarPicker = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.dog),
                                    contentDescription = "Ảnh đại diện cún",
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            // Preset 2: Cat
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clickable {
                                        viewModel.updateUserProfile(avatarRes = R.drawable.cat)
                                        showAvatarPicker = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.cat),
                                    contentDescription = "Mèo Luna",
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            // Preset 3: Default Icon
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    .clickable {
                                        viewModel.updateUserProfile(clearAvatar = true)
                                        showAvatarPicker = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Mặc định",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    OutlinedButton(onClick = { showAvatarPicker = false }, shape = RoundedCornerShape(12.dp)) {
                        Text("Để sau", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }

        // 3. Cover Picker Modal / Options
        if (showCoverPicker) {
            AlertDialog(
                onDismissRequest = { showCoverPicker = false },
                title = { Text("Thay đổi ảnh bìa / Nền", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Button: Pick Cover from Gallery
                        Button(
                            onClick = {
                                showCoverPicker = false
                                coverPickerLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Chọn ảnh bìa từ thư viện", fontWeight = FontWeight.Bold)
                        }

                        Text("— Hoặc chọn màu nền bìa —", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        val colors = listOf(
                            0xFF338C63 to "Xanh lá (Mặc định)",
                            0xFF2C3E50 to "Xanh Đen",
                            0xFF4A90E2 to "Xanh Dương",
                            0xFF8E44AD to "Tím",
                            0xFFD35400 to "Cam"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            colors.forEach { (hex, name) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(hex))
                                        .clickable {
                                            viewModel.updateUserProfile(headerBgColorHex = hex, headerBgUri = null)
                                            showCoverPicker = false
                                        }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    OutlinedButton(onClick = { showCoverPicker = false }, shape = RoundedCornerShape(12.dp)) {
                        Text("Để sau", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }

        // Image 5: "Chuyển tài khoản" Bottom Sheet
        if (showAccountBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAccountBottomSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                AccountSwitchSheetContent(
                    accounts = accounts,
                    onSelectAccount = { accountId ->
                        viewModel.switchAccount(accountId)
                        showAccountBottomSheet = false
                    },
                    onAddNewAccount = { newName ->
                        if (newName.isNotBlank()) {
                            viewModel.addAccount(newName)
                            showAccountBottomSheet = false
                        }
                    },
                    onLogoutClick = {
                        showAccountBottomSheet = false
                        onLogoutClick()
                    },
                )
            }
        }
    }
}

@Composable
private fun AccountSwitchSheetContent(
    accounts: List<com.example.petcare.data.repository.AccountUser>,
    onSelectAccount: (String) -> Unit,
    onAddNewAccount: (String) -> Unit,
    onLogoutClick: () -> Unit = {},
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newAccountName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Drag indicator bar
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.6f))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title: Chuyển tài khoản
        Text(
            text = "Chuyển tài khoản",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Account list cards matching Image 5
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            accounts.forEachIndexed { index, account ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onSelectAccount(account.id) }
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tài khoản ${index + 1}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = account.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (account.isCurrent) {
                            Text(
                                text = "Hiện tại",
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                textDecoration = TextDecoration.Underline,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (showAddDialog) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = newAccountName,
                        onValueChange = { newAccountName = it },
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (newAccountName.isEmpty()) {
                                Text("Nhập tên tài khoản mới", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            inner()
                        }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onAddNewAccount(newAccountName)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Lưu", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Button: Tài khoản mới
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "Tài khoản mới",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
        ) {
            Text(
                text = "Đăng xuất & đổi tài khoản khác",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileDetailScreenPreview() {
    PetCareTheme {
        UserProfileDetailScreen()
    }
}
