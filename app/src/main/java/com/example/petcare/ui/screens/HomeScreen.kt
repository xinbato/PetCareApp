package com.example.petcare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.petcare.R
import com.example.petcare.ui.components.PetCareTabBar
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.LightGreenIcon
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary

// --- Pure Jetpack Compose ImageVectors (No Emojis) ---
val PawIconVector: ImageVector by lazy {
    ImageVector.Builder("PawIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(LightGreenIcon)) {
            // Main Pad
            moveTo(12f, 13f)
            curveToRelative(-2.2f, 0f, -4f, 1.8f, -4f, 4f)
            curveToRelative(0f, 1.5f, 1.2f, 2.7f, 2.7f, 2.9f)
            curveToRelative(0.4f, 0.1f, 0.9f, 0.1f, 1.3f, 0.1f)
            curveToRelative(0.4f, 0f, 0.9f, 0f, 1.3f, -0.1f)
            curveToRelative(1.5f, -0.2f, 2.7f, -1.4f, 2.7f, -2.9f)
            curveToRelative(0f, -2.2f, -1.8f, -4f, -4f, -4f)
            close()
            // Toe 1
            moveTo(6.5f, 10f)
            curveToRelative(-1.1f, 0f, -2f, 1.1f, -2f, 2.5f)
            curveToRelative(0f, 1.4f, 0.9f, 2.5f, 2f, 2.5f)
            curveToRelative(1.1f, 0f, 2f, -1.1f, 2f, -2.5f)
            curveToRelative(0f, -1.4f, -0.9f, -2.5f, -2f, -2.5f)
            close()
            // Toe 2
            moveTo(9.5f, 5.5f)
            curveToRelative(-1.1f, 0f, -2f, 1.1f, -2f, 2.5f)
            curveToRelative(0f, 1.4f, 0.9f, 2.5f, 2f, 2.5f)
            curveToRelative(1.1f, 0f, 2f, -1.1f, 2f, -2.5f)
            curveToRelative(0f, -1.4f, -0.9f, -2.5f, -2f, -2.5f)
            close()
            // Toe 3
            moveTo(14.5f, 5.5f)
            curveToRelative(-1.1f, 0f, -2f, 1.1f, -2f, 2.5f)
            curveToRelative(0f, 1.4f, 0.9f, 2.5f, 2f, 2.5f)
            curveToRelative(1.1f, 0f, 2f, -1.1f, 2f, -2.5f)
            curveToRelative(0f, -1.4f, -0.9f, -2.5f, -2f, -2.5f)
            close()
            // Toe 4
            moveTo(17.5f, 10f)
            curveToRelative(-1.1f, 0f, -2f, 1.1f, -2f, 2.5f)
            curveToRelative(0f, 1.4f, 0.9f, 2.5f, 2f, 2.5f)
            curveToRelative(1.1f, 0f, 2f, -1.1f, 2f, -2.5f)
            curveToRelative(0f, -1.4f, -0.9f, -2.5f, -2f, -2.5f)
            close()
        }
    }.build()
}

val CatIconVector: ImageVector by lazy {
    ImageVector.Builder("CatIcon", 24.dp, 24.dp, 24f, 24f).apply {
        // Cat Head
        path(fill = SolidColor(LightGreenIcon)) {
            moveTo(12f, 4f)
            curveTo(7.58f, 4f, 4f, 7.58f, 4f, 12f)
            curveToRelative(0f, 2.5f, 1.15f, 4.73f, 2.96f, 6.22f)
            lineTo(5f, 21f)
            lineToRelative(3.5f, -1.25f)
            curveTo(9.6f, 20.24f, 10.77f, 20.5f, 12f, 20.5f)
            curveToRelative(4.42f, 0f, 8f, -3.58f, 8f, -8.5f)
            curveToRelative(0f, -4.42f, -3.58f, -8f, -8f, -8f)
            close()
        }
        // Cute Ears & Eyes Detailing
        path(fill = SolidColor(GreenLight)) {
            // Left Ear
            moveTo(6.5f, 7.5f)
            lineTo(4.8f, 4.5f)
            lineTo(8.5f, 5.8f)
            close()
            // Right Ear
            moveTo(17.5f, 7.5f)
            lineTo(19.2f, 4.5f)
            lineTo(15.5f, 5.8f)
            close()
            // Eyes
            moveTo(8.5f, 11f)
            curveTo(9.33f, 11f, 10f, 11.67f, 10f, 12.5f)
            curveTo(10f, 13.33f, 9.33f, 14f, 8.5f, 14f)
            curveTo(7.67f, 14f, 7f, 13.33f, 7f, 12.5f)
            curveTo(7f, 11.67f, 7.67f, 11f, 8.5f, 11f)
            close()
            moveTo(15.5f, 11f)
            curveTo(16.33f, 11f, 17f, 11.67f, 17f, 12.5f)
            curveTo(17f, 13.33f, 16.33f, 14f, 15.5f, 14f)
            curveTo(14.67f, 14f, 14f, 13.33f, 14f, 12.5f)
            curveTo(14f, 11.67f, 14.67f, 11f, 15.5f, 11f)
            close()
        }
    }.build()
}

val DogIconVector: ImageVector by lazy {
    ImageVector.Builder("DogIcon", 24.dp, 24.dp, 24f, 24f).apply {
        // Dog Head Base
        path(fill = SolidColor(LightGreenIcon)) {
            moveTo(12f, 2f)
            curveTo(8.13f, 2f, 5f, 5.13f, 5f, 9f)
            curveToRelative(0f, 2.38f, 1.19f, 4.47f, 3f, 5.74f)
            verticalLineTo(19f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(4f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineToRelative(-4.26f)
            curveToRelative(1.81f, -1.27f, 3f, -3.36f, 3f, -5.74f)
            curveToRelative(0f, -3.87f, -3.13f, -7f, -7f, -7f)
            close()
        }
        // Floppy Ears & Eyes/Muzzle
        path(fill = SolidColor(GreenLight)) {
            // Left Ear
            moveTo(4.5f, 5f)
            curveTo(3f, 7f, 2.5f, 10f, 3.5f, 13f)
            curveTo(4.5f, 15f, 6.5f, 15f, 7f, 12f)
            lineTo(6.5f, 7f)
            close()
            // Right Ear
            moveTo(19.5f, 5f)
            curveTo(21f, 7f, 21.5f, 10f, 20.5f, 13f)
            curveTo(19.5f, 15f, 17.5f, 15f, 17f, 12f)
            lineTo(17.5f, 7f)
            close()
            // Eyes
            moveTo(9f, 9f)
            curveToRelative(0.83f, 0f, 1.5f, 0.67f, 1.5f, 1.5f)
            curveTo(10.5f, 11.33f, 9.83f, 12f, 9f, 12f)
            curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f)
            curveTo(7.5f, 9.67f, 8.17f, 9f, 9f, 9f)
            close()
            moveTo(15f, 9f)
            curveToRelative(0.83f, 0f, 1.5f, 0.67f, 1.5f, 1.5f)
            curveTo(16.5f, 11.33f, 15.83f, 12f, 15f, 12f)
            curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f)
            curveTo(13.5f, 9.67f, 14.17f, 9f, 15f, 9f)
            close()
        }
    }.build()
}

val RabbitIconVector: ImageVector by lazy {
    ImageVector.Builder("RabbitIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(GreenPrimary)) {
            moveTo(12f, 10f)
            curveTo(8.69f, 10f, 6f, 12.69f, 6f, 16f)
            curveToRelative(0f, 3.31f, 2.69f, 6f, 6f, 6f)
            curveToRelative(3.31f, 0f, 6f, -2.69f, 6f, -6f)
            curveToRelative(0f, -3.31f, -2.69f, -6f, -6f, -6f)
            close()
            moveTo(8.5f, 2f)
            curveToRelative(-1.1f, 0f, -2f, 1.5f, -2f, 4f)
            curveToRelative(0f, 2.5f, 1.5f, 4f, 2.5f, 4f)
            curveToRelative(1f, 0f, 1.5f, -1.5f, 1.5f, -4f)
            curveToRelative(0f, -2.5f, -0.9f, -4f, -2f, -4f)
            close()
            moveTo(15.5f, 2f)
            curveToRelative(-1.1f, 0f, -2f, 1.5f, -2f, 4f)
            curveToRelative(0f, 2.5f, 0.5f, 4f, 1.5f, 4f)
            curveToRelative(1f, 0f, 2.5f, -1.5f, 2.5f, -4f)
            curveToRelative(0f, -2.5f, -0.9f, -4f, -2f, -4f)
            close()
        }
    }.build()
}

val HospitalIconVector: ImageVector by lazy {
    ImageVector.Builder("HospitalIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(GreenPrimary)) {
            moveTo(19f, 3f)
            horizontalLineTo(5f)
            curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
            verticalLineToRelative(14f)
            curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
            horizontalLineToRelative(14f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
            verticalLineTo(5f)
            curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
            close()
            moveTo(10.5f, 17f)
            verticalLineToRelative(-3.5f)
            horizontalLineTo(7f)
            verticalLineToRelative(-3f)
            horizontalLineToRelative(3.5f)
            verticalLineTo(7f)
            horizontalLineToRelative(3f)
            verticalLineToRelative(3.5f)
            horizontalLineTo(17f)
            verticalLineToRelative(3f)
            horizontalLineToRelative(-3.5f)
            verticalLineTo(17f)
            horizontalLineToRelative(-3f)
            close()
        }
    }.build()
}

val ChartIconVector: ImageVector by lazy {
    ImageVector.Builder("ChartIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFFE65100))) {
            moveTo(19f, 3f)
            horizontalLineTo(5f)
            curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
            verticalLineToRelative(14f)
            curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
            horizontalLineToRelative(14f)
            curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
            verticalLineTo(5f)
            curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
            close()
            moveTo(9f, 17f)
            horizontalLineTo(7f)
            verticalLineToRelative(-5f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(5f)
            close()
            moveTo(13f, 17f)
            horizontalLineToRelative(-2f)
            verticalLineTo(7f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(10f)
            close()
            moveTo(17f, 17f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(-8f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(8f)
            close()
        }
    }.build()
}

val AlarmClockIconVector: ImageVector by lazy {
    ImageVector.Builder("AlarmClockIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFF283593))) {
            moveTo(22f, 5.72f)
            lineToRelative(-4.6f, -3.86f)
            lineToRelative(-1.29f, 1.53f)
            lineTo(20.71f, 7.25f)
            close()
            moveTo(7.88f, 3.39f)
            lineTo(6.6f, 1.86f)
            lineTo(2f, 5.71f)
            lineToRelative(1.29f, 1.53f)
            close()
            moveTo(12.5f, 8f)
            horizontalLineTo(11f)
            verticalLineToRelative(6f)
            lineToRelative(4.75f, 2.85f)
            lineToRelative(0.75f, -1.23f)
            lineToRelative(-4f, -2.37f)
            close()
            moveTo(12f, 4f)
            curveTo(7.03f, 4f, 3f, 8.03f, 3f, 13f)
            curveTo(3f, 17.97f, 7.03f, 22f, 12f, 22f)
            curveTo(16.97f, 22f, 21f, 17.97f, 21f, 13f)
            curveTo(21f, 8.03f, 16.97f, 4f, 12f, 4f)
            close()
            moveTo(12f, 20f)
            curveTo(8.13f, 20f, 5f, 16.87f, 5f, 13f)
            curveTo(5f, 9.13f, 8.13f, 6f, 12f, 6f)
            curveTo(15.87f, 6f, 19f, 9.13f, 19f, 13f)
            curveTo(19f, 16.87f, 15.87f, 20f, 12f, 20f)
            close()
        }
    }.build()
}

val BookOpenIconVector: ImageVector by lazy {
    ImageVector.Builder("BookOpenIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFF6A1B9A))) {
            moveTo(21f, 5f)
            curveToRelative(-1.11f, -0.35f, -2.33f, -0.5f, -3.5f, -0.5f)
            curveToRelative(-1.95f, 0f, -4.05f, 0.4f, -5.5f, 1.5f)
            curveTo(10.55f, 4.9f, 8.45f, 4.5f, 6.5f, 4.5f)
            curveTo(4.55f, 4.5f, 2.45f, 4.9f, 1f, 6f)
            verticalLineToRelative(14.65f)
            curveToRelative(0f, 0.25f, 0.25f, 0.5f, 0.5f, 0.5f)
            curveToRelative(0.1f, 0f, 0.15f, -0.05f, 0.25f, -0.05f)
            curveTo(3.1f, 20.45f, 4.81f, 20f, 6.5f, 20f)
            curveToRelative(1.95f, 0f, 4.05f, 0.4f, 5.5f, 1.5f)
            curveToRelative(1.35f, -0.85f, 3.8f, -1.5f, 5.5f, -1.5f)
            curveToRelative(1.65f, 0f, 3.35f, 0.3f, 4.75f, 1.05f)
            curveToRelative(0.1f, 0.05f, 0.15f, 0.05f, 0.25f, 0.05f)
            curveToRelative(0.25f, 0f, 0.5f, -0.25f, 0.5f, -0.5f)
            verticalLineTo(6f)
            curveToRelative(-0.6f, -0.45f, -1.25f, -0.75f, -2f, -1f)
            close()
            moveTo(19f, 18.5f)
            curveToRelative(-1.15f, -0.35f, -2.3f, -0.5f, -3.5f, -0.5f)
            curveToRelative(-1.7f, 0f, -3.85f, 0.5f, -4.5f, 1.25f)
            verticalLineTo(7.5f)
            curveToRelative(0.8f, -0.6f, 2.65f, -1f, 4.5f, -1f)
            curveToRelative(1.2f, 0f, 2.35f, 0.15f, 3.5f, 0.5f)
            verticalLineToRelative(11.5f)
            close()
        }
    }.build()
}

val LightbulbIconVector: ImageVector by lazy {
    ImageVector.Builder("LightbulbIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(Color(0xFFF57F17))) {
            moveTo(9f, 21f)
            horizontalLineToRelative(6f)
            verticalLineToRelative(-1f)
            horizontalLineTo(9f)
            verticalLineToRelative(1f)
            close()
            moveTo(12f, 2f)
            curveTo(7.86f, 2f, 4.5f, 5.36f, 4.5f, 9.5f)
            curveToRelative(0f, 2.82f, 1.55f, 5.27f, 3.86f, 6.55f)
            verticalLineTo(18f)
            curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f)
            horizontalLineToRelative(5.28f)
            curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
            verticalLineToRelative(-1.95f)
            curveToRelative(2.31f, -1.28f, 3.86f, -3.73f, 3.86f, -6.55f)
            curveTo(19.5f, 5.36f, 16.14f, 2f, 12f, 2f)
            close()
        }
    }.build()
}

val BellIconVector: ImageVector by lazy {
    ImageVector.Builder("BellIcon", 24.dp, 24.dp, 24f, 24f).apply {
        path(fill = SolidColor(TextPrimary)) {
            moveTo(12f, 22f)
            curveTo(13.1f, 22f, 14f, 21.1f, 14f, 20f)
            lineTo(10f, 20f)
            curveTo(10f, 21.1f, 10.89f, 22f, 12f, 22f)
            close()
            moveTo(18f, 16f)
            lineTo(18f, 11f)
            curveTo(18f, 7.93f, 16.36f, 5.36f, 13.5f, 4.68f)
            lineTo(13.5f, 4f)
            curveTo(13.5f, 3.17f, 12.83f, 2.5f, 12f, 2.5f)
            curveTo(11.17f, 2.5f, 10.5f, 3.17f, 10.5f, 4f)
            lineTo(10.5f, 4.68f)
            curveTo(7.63f, 5.36f, 6f, 7.92f, 6f, 11f)
            lineTo(6f, 16f)
            lineTo(4f, 18f)
            lineTo(4f, 19f)
            lineTo(20f, 19f)
            lineTo(20f, 18f)
            lineTo(18f, 16f)
            close()
        }
    }.build()
}

// --- MVI Data Models ---
data class PetUiModel(
    val id: String,
    val name: String,
    val breed: String,
    val age: String,
    val weight: String,
    val iconRes: Int,
    val statusText: String,
)

data class ScheduleUiModel(
    val id: String,
    val title: String,
    val petName: String,
    val timeText: String,
    val category: String,
    val isCompleted: Boolean,
)

data class ServiceUiModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val backgroundColor: Color,
)

data class HomeUiState(
    val userName: String = "Nguyễn Văn A",
    val pets: List<PetUiModel> = listOf(
        PetUiModel(
            id = "1",
            name = "Miu Miu",
            breed = "Mèo Anh Lông Ngắn",
            age = "2 tuổi",
            weight = "4.2 kg",
            iconRes = R.drawable.cat,
            statusText = "Sắp đến lịch tiêm phòng",
        ),
        PetUiModel(
            id = "2",
            name = "Cậu Vàng",
            breed = "Cún Poodle",
            age = "1.5 tuổi",
            weight = "5.0 kg",
            iconRes = R.drawable.dog,
            statusText = "Khỏe mạnh",
        ),
        PetUiModel(
            id = "3",
            name = "Lô Lô",
            breed = "Thỏ Lốp",
            age = "8 tháng",
            weight = "1.8 kg",
            iconRes = R.drawable.rabbit,
            statusText = "Đã tắm rửa",
        ),
    ),
    val selectedPetId: String = "1",
    val schedules: List<ScheduleUiModel> = listOf(
        ScheduleUiModel(
            id = "s1",
            title = "Tắm rửa & Cắt tỉa lông",
            petName = "Miu Miu",
            timeText = "15:00 - Hôm nay",
            category = "Grooming",
            isCompleted = false,
        ),
        ScheduleUiModel(
            id = "s2",
            title = "Tiêm vắc-xin định kỳ",
            petName = "Miu Miu",
            timeText = "09:30 - Ngày mai",
            category = "Y tế",
            isCompleted = false,
        ),
        ScheduleUiModel(
            id = "s3",
            title = "Uống thuốc tẩy giun",
            petName = "Cậu Vàng",
            timeText = "08:00 - 15/11",
            category = "Thuốc",
            isCompleted = true,
        ),
    ),
    val services: List<ServiceUiModel> = listOf(
        ServiceUiModel("srv1", "Đặt lịch khám", "Bác sĩ thú y 24/7", HospitalIconVector, GreenLight),
        ServiceUiModel("srv2", "Nhật ký sức khỏe", "Theo dõi cân nặng & số đo", ChartIconVector, Color(0xFFFFF3E0)),
        ServiceUiModel("srv3", "Nhắc nhở ăn uống", "Thiết lập giờ ăn", AlarmClockIconVector, Color(0xFFE8EAF6)),
        ServiceUiModel("srv4", "Mẹo chăm sóc", "Kiến thức nuôi bé", BookOpenIconVector, Color(0xFFF3E5F5)),
    ),
)

sealed interface HomeUiIntent {
    data class SelectPet(val petId: String) : HomeUiIntent
    data class ToggleScheduleComplete(val scheduleId: String) : HomeUiIntent
    data class SelectBottomTab(val tabIndex: Int) : HomeUiIntent
    data class ClickService(val serviceId: String) : HomeUiIntent
    data object ClickNotification : HomeUiIntent
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userName: String = "Nguyễn Văn A",
    onAddPetClick: () -> Unit = {},
    onPetDetailClick: (String) -> Unit = {},
) {
    var uiState by remember(userName) { mutableStateOf(HomeUiState(userName = userName)) }
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    fun processIntent(intent: HomeUiIntent) {
        when (intent) {
            is HomeUiIntent.SelectPet -> {
                uiState = uiState.copy(selectedPetId = intent.petId)
                onPetDetailClick(intent.petId)
            }

            is HomeUiIntent.ToggleScheduleComplete -> {
                val updatedList = uiState.schedules.map { schedule ->
                    if (schedule.id == intent.scheduleId) schedule.copy(isCompleted = !schedule.isCompleted) else schedule
                }
                uiState = uiState.copy(schedules = updatedList)
            }

            is HomeUiIntent.SelectBottomTab -> {
                selectedBottomTab = intent.tabIndex
            }

            is HomeUiIntent.ClickService -> {
                /* Action on service click */
            }

            is HomeUiIntent.ClickNotification -> {
                /* Action on notification click */
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Top App Header
            HomeTopHeader(
                userName = uiState.userName,
            ) { processIntent(HomeUiIntent.ClickNotification) }

            // Scrollable Main Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
            ) {
                // 1. My Pets Section
                MyPetsSection(
                    pets = uiState.pets,
                    selectedPetId = uiState.selectedPetId,
                    onPetSelected = { petId ->
                        processIntent(HomeUiIntent.SelectPet(petId))
                    },
                    onAddPetClick = onAddPetClick,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Quick Services Section
                QuickServicesSection(
                    services = uiState.services,
                    onServiceClick = { serviceId ->
                        processIntent(HomeUiIntent.ClickService(serviceId))
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Upcoming Schedule Section
                UpcomingScheduleSection(
                    schedules = uiState.schedules,
                    onToggleComplete = { scheduleId ->
                        processIntent(HomeUiIntent.ToggleScheduleComplete(scheduleId))
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Care Tips Section
                CareTipsSection()
            }

            // Bottom Navigation TabBar (Pill Shape with Jetpack Compose Icons)
            PetCareTabBar(
                selectedTab = selectedBottomTab,
                onTabSelected = { tabIndex ->
                    processIntent(HomeUiIntent.SelectBottomTab(tabIndex))
                },
                onAddPetClick = onAddPetClick,
            )
        }
    }
}

@Composable
private fun HomeTopHeader(
    userName: String,
    onNotificationClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(GreenLight)
                    .border(1.5.dp, GreenPrimary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = PawIconVector,
                    contentDescription = "Avatar Badge",
                    tint = GreenPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Xin chào, $userName",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(
                    text = "Thú cưng của bạn hôm nay thế nào?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                )
            }
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, BorderLight, CircleShape)
                .clickable { onNotificationClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = BellIconVector,
                contentDescription = "Thông báo",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp),
            )
            // Notification Dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .align(Alignment.TopEnd)
                    .padding(2.dp),
            )
        }
    }
}

@Composable
private fun MyPetsSection(
    pets: List<PetUiModel>,
    selectedPetId: String,
    onPetSelected: (String) -> Unit,
    onAddPetClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Thú cưng của tôi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = "+ Thêm bé mới",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
                modifier = Modifier.clickable { onAddPetClick() },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(pets) { pet ->
                val isSelected = pet.id == selectedPetId
                PetCardItem(
                    pet = pet,
                    isSelected = isSelected,
                    onClick = {
                        onPetSelected(pet.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun PetCardItem(
    pet: PetUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GreenLight else Color.White,
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (isSelected) GreenPrimary else BorderLight),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, BorderLight, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = pet.iconRes),
                        contentDescription = pet.name,
                        tint = LightGreenIcon,
                        modifier = Modifier.size(24.dp),
                    )
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenPrimary)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "Đang chọn",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = pet.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = pet.breed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BackgroundLight)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(text = pet.age, fontSize = 10.sp, color = TextSecondary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BackgroundLight)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(text = pet.weight, fontSize = 10.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun QuickServicesSection(
    services: List<ServiceUiModel>,
    onServiceClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = "Dịch vụ & Tiện ích",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        services.chunked(2).forEachIndexed { index, rowServices ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowServices.forEach { service ->
                    ServiceCard(
                        service = service,
                        modifier = Modifier.weight(1f),
                        onClick = { onServiceClick(service.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(
    service: ServiceUiModel,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .height(84.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderLight)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(service.backgroundColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = service.title,
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = service.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = service.subtitle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun UpcomingScheduleSection(
    schedules: List<ScheduleUiModel>,
    onToggleComplete: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Lịch trình hôm nay",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = "Xem tất cả",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            schedules.forEach { schedule ->
                ScheduleCardItem(
                    schedule = schedule,
                    onToggleComplete = { onToggleComplete(schedule.id) },
                )
            }
        }
    }
}

@Composable
private fun ScheduleCardItem(
    schedule: ScheduleUiModel,
    onToggleComplete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderLight)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (schedule.isCompleted) GreenPrimary else Color.White)
                        .border(1.5.dp, if (schedule.isCompleted) GreenPrimary else BorderLight, CircleShape)
                        .clickable { onToggleComplete() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (schedule.isCompleted) {
                        Text(text = "✓", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = schedule.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (schedule.isCompleted) TextSecondary else TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = PawIconVector,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = schedule.petName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = GreenPrimary,
                        )
                        Text(
                            text = "  •  ${schedule.timeText}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenLight)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = schedule.category,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenPrimary,
                )
            }
        }
    }
}

@Composable
private fun CareTipsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Text(
            text = "Mẹo chăm sóc bé",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GreenLight.copy(alpha = 0.6f)),
            border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(BorderLight)),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = LightbulbIconVector,
                        contentDescription = "Mẹo chăm sóc",
                        tint = Color(0xFFF57F17),
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Chế độ dinh dưỡng cho Mèo con",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Đảm bảo cung cấp đủ lượng Protein và Nước mỗi ngày để bé phát triển toàn diện.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PetCareTheme {
        HomeScreen()
    }
}
