package com.example.petcare.ui.screens

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.data.model.AppSettings
import com.example.petcare.data.model.PetModel
import com.example.petcare.data.model.ThemeMode
import com.example.petcare.data.model.VaccinationModel
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onNotificationRemindersClick: () -> Unit = {},
    onSwitchAccountClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val settings by settingsViewModel.settings.collectAsState()
    val userProfile by PetRepository.userProfile.collectAsState()
    val pets by PetRepository.pets.collectAsState()
    val vaccinations by PetRepository.vaccinations.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    var showThemeDialog by remember { mutableStateOf(false) }
    var showWeightUnitDialog by remember { mutableStateOf(false) }
    var showReminderDaysDialog by remember { mutableStateOf(false) }
    var showRadiusDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = KeyboardArrowLeftIcon,
                        contentDescription = "Quay lại",
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cài đặt",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Master Notification Switch Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorScheme.surface)
                        .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .clickable { onNotificationRemindersClick() }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Thông báo & nhắc lịch",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                            )
                            Text(
                                text = if (settings.notificationsEnabled) {
                                    "Đang bật (nhắc tiêm trước ${settings.defaultVaccineReminderDays} ngày)"
                                } else {
                                    "Đang tắt toàn bộ thông báo"
                                },
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { settingsViewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colorScheme.onPrimary,
                                checkedTrackColor = colorScheme.primary,
                            ),
                        )
                    }
                }

                SettingCardItem(
                    title = "Nhắc lịch tiêm mặc định",
                    subtitle = "Trước ${settings.defaultVaccineReminderDays} ngày",
                    onClick = { showReminderDaysDialog = true },
                )

                SettingCardItem(
                    title = "Đơn vị cân nặng",
                    subtitle = if (settings.usePounds) "Pound (lb)" else "Kilogram (kg)",
                    onClick = { showWeightUnitDialog = true },
                )

                SettingCardItem(
                    title = "Giao diện",
                    subtitle = when (settings.themeMode) {
                        ThemeMode.SYSTEM -> "Theo hệ thống"
                        ThemeMode.LIGHT -> "Sáng"
                        ThemeMode.DARK -> "Tối"
                    },
                    onClick = { showThemeDialog = true },
                )

                SettingCardItem(
                    title = "Bán kính tìm phòng khám",
                    subtitle = "${settings.defaultSearchRadiusMeters / 1000} km",
                    onClick = { showRadiusDialog = true },
                )

                // Share Pet Profile Summary Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorScheme.primaryContainer.copy(alpha = 0.55f))
                        .border(1.dp, colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable {
                            val summary = buildPetSummaryText(
                                ownerName = userProfile.name,
                                pets = pets,
                                vaccinations = vaccinations,
                                settings = settings,
                            )
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Hồ sơ sức khỏe thú cưng - PetCare")
                                putExtra(Intent.EXTRA_TEXT, summary)
                            }
                            runCatching {
                                context.startActivity(
                                    Intent.createChooser(sendIntent, "Chia sẻ hồ sơ thú cưng")
                                )
                            }.onFailure {
                                Toast.makeText(context, "Không tìm thấy ứng dụng để chia sẻ.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Chia sẻ hồ sơ thú cưng",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface,
                            )
                            Text(
                                text = "Xuất tóm tắt thông tin và lịch tiêm gửi bác sĩ / người thân",
                                fontSize = 12.sp,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Chia sẻ",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                SettingCardItem(
                    title = "Quyền riêng tư & Bảo mật",
                    subtitle = "Cô lập dữ liệu theo tài khoản & App Check",
                    onClick = { showPrivacyDialog = true },
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorScheme.surface)
                        .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .clickable(onClick = onSwitchAccountClick)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "Hồ sơ chi tiết & Đổi tài khoản",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorScheme.surface)
                        .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .clickable { showLogoutDialog = true }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "Đăng xuất",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorScheme.surface)
                        .border(1.dp, colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .clickable { showDeleteAccountDialog = true }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "Xóa tài khoản",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Chọn giao diện", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(
                        ThemeMode.SYSTEM to "Theo hệ thống",
                        ThemeMode.LIGHT to "Chế độ sáng",
                        ThemeMode.DARK to "Chế độ tối",
                    ).forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.themeMode == mode,
                                onClick = {
                                    settingsViewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { showThemeDialog = false }) { Text("Đóng") }
            },
        )
    }

    if (showWeightUnitDialog) {
        AlertDialog(
            onDismissRequest = { showWeightUnitDialog = false },
            title = { Text("Đơn vị cân nặng", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(false to "Kilogram (kg)", true to "Pound (lb)").forEach { (pounds, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.setUsePounds(pounds)
                                    showWeightUnitDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.usePounds == pounds,
                                onClick = {
                                    settingsViewModel.setUsePounds(pounds)
                                    showWeightUnitDialog = false
                                },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { showWeightUnitDialog = false }) { Text("Đóng") }
            },
        )
    }

    if (showReminderDaysDialog) {
        AlertDialog(
            onDismissRequest = { showReminderDaysDialog = false },
            title = { Text("Nhắc lịch tiêm trước", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(1, 3, 7).forEach { days ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.setDefaultVaccineReminderDays(days)
                                    showReminderDaysDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.defaultVaccineReminderDays == days,
                                onClick = {
                                    settingsViewModel.setDefaultVaccineReminderDays(days)
                                    showReminderDaysDialog = false
                                },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Trước $days ngày", fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { showReminderDaysDialog = false }) { Text("Đóng") }
            },
        )
    }

    if (showRadiusDialog) {
        AlertDialog(
            onDismissRequest = { showRadiusDialog = false },
            title = { Text("Bán kính tìm kiếm phòng khám", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(3000, 5000, 10000).forEach { meters ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.setDefaultSearchRadiusMeters(meters)
                                    showRadiusDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.defaultSearchRadiusMeters == meters,
                                onClick = {
                                    settingsViewModel.setDefaultSearchRadiusMeters(meters)
                                    showRadiusDialog = false
                                },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("${meters / 1000} km", fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(onClick = { showRadiusDialog = false }) { Text("Đóng") }
            },
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Quyền riêng tư & Bảo mật", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Toàn bộ hồ sơ thú cưng và lịch chăm sóc được cô lập theo từng tài khoản qua Cloud Firestore Security Rules, khóa đồng bộ bởi Firebase App Check, và tắt sao lưu đám mây hệ thống (allowBackup=false)."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                ) {
                    Text("Đã hiểu")
                }
            },
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản này không?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                ) {
                    Text("Đăng xuất")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Hủy") }
            },
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Xác nhận xóa tài khoản", fontWeight = FontWeight.Bold, color = colorScheme.error) },
            text = {
                Text("Bạn có chắc chắn muốn xóa tài khoản này không? Mọi dữ liệu thú cưng và lịch hẹn trên cloud sẽ bị xóa vĩnh viễn.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccountClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error),
                ) {
                    Text("Xóa vĩnh viễn")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteAccountDialog = false }) { Text("Hủy") }
            },
        )
    }
}

internal fun buildPetSummaryText(
    ownerName: String,
    pets: List<PetModel>,
    vaccinations: List<VaccinationModel>,
    settings: AppSettings,
): String {
    if (pets.isEmpty()) {
        return "Hồ sơ PetCare (${ownerName.ifBlank { "Chủ nuôi" }}): Chưa có thú cưng được ghi nhận."
    }
    return buildString {
        appendLine("=== HỒ SƠ CHĂM SÓC THÚ CƯNG (PETCARE) ===")
        appendLine("Chủ nuôi: ${ownerName.ifBlank { "Người dùng PetCare" }}")
        appendLine("Số lượng thú cưng: ${pets.size}")
        appendLine()
        pets.forEachIndexed { idx, pet ->
            val weightText = if (pet.weightKg > 0.0) settings.formatWeight(pet.weightKg) else pet.weight
            appendLine("${idx + 1}. ${pet.name} (${pet.species} - ${pet.breed})")
            appendLine("   Tuổi: ${pet.age} | Giới tính: ${pet.gender} | Cân nặng: $weightText")
            val petVaccs = vaccinations.filter { it.petId == pet.id }
            if (petVaccs.isNotEmpty()) {
                appendLine("   Lịch tiêm:")
                petVaccs.forEach { v ->
                    val statusTag = if (v.completed) "[Đã tiêm]" else "[Sắp tới]"
                    appendLine("   - $statusTag ${v.title} (${v.dateTime})")
                }
            }
            if (pet.notes.isNotBlank()) {
                appendLine("   Ghi chú: ${pet.notes}")
            }
        }
    }.trim()
}

@Composable
private fun SettingCardItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
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
