package com.example.petcare.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.data.model.WeightRecordModel
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.components.PetCareIcons
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.petcare.data.model.AppSettings
import com.example.petcare.ui.components.PetAvatar
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.viewmodel.HealthViewModel
import com.example.petcare.ui.viewmodel.SettingsViewModel
import com.example.petcare.utils.DateUtils

@Composable
fun WeightHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HealthViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddWeightClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsState()
    val allPets = uiState.pets
    val weightRecords = uiState.weightRecords
    val colorScheme = MaterialTheme.colorScheme

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
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Lịch sử cân nặng",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onBackground
                    )
                    Text(
                        text = "Theo dõi cân nặng (${settings.weightUnitLabel}) theo thời gian",
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            // Weight List Grouped by Pet
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (allPets.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        ) {
                            Text(
                                text = "Chưa có thông tin thú cưng nào. Vui lòng thêm thú cưng để theo dõi cân nặng.",
                                fontSize = 14.sp,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                } else {
                    allPets.forEach { pet ->
                        val petRecords = weightRecords.filter { it.petId == pet.id }
                        val currentWeightText = if (pet.weightKg > 0.0) {
                            settings.formatWeight(pet.weightKg)
                        } else {
                            pet.weight
                        }

                        item(key = "header_${pet.id}") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        PetAvatar(
                                            photoUri = pet.photoUri,
                                            fallbackIconRes = pet.safeIconRes,
                                            contentDescription = pet.name,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = pet.name,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.onBackground
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colorScheme.primaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Hiện tại: $currentWeightText",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (petRecords.isNotEmpty()) {
                            item(key = "chart_${pet.id}") {
                                WeightTrendChartCard(
                                    petName = pet.name,
                                    records = petRecords,
                                    settings = settings,
                                )
                            }
                        }

                        if (petRecords.isEmpty()) {
                            item(key = "empty_${pet.id}") {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                                ) {
                                    Text(
                                        text = "Chưa có ghi nhận cân nặng cho ${pet.name}",
                                        fontSize = 13.sp,
                                        color = colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        } else {
                            items(petRecords, key = { it.id }) { item ->
                                WeightRecordItemCard(
                                    item = item,
                                    settings = settings,
                                )
                            }
                        }

                        item(key = "space_${pet.id}") {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun WeightTrendChartCard(
    petName: String,
    records: List<WeightRecordModel>,
    settings: AppSettings,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val points = remember(records, settings.usePounds) {
        records.sortedBy { it.recordedAtMillis }
            .mapNotNull { rec ->
                val kg = if (rec.weightKg > 0.0) rec.weightKg else DateUtils.parseWeightKg(rec.weight)
                if (kg > 0.0) settings.toDisplayWeight(kg).toFloat() to rec.dateTime else null
            }
    }
    if (points.isEmpty()) return

    val values = points.map { it.first }
    val minVal = values.minOrNull() ?: 0f
    val maxVal = values.maxOrNull() ?: 1f
    val span = (maxVal - minVal).takeIf { it > 0.05f } ?: 1f
    val primaryColor = colorScheme.primary
    val gridColor = colorScheme.outlineVariant.copy(alpha = 0.6f)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Biểu đồ cân nặng ($petName)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = String.format(
                        java.util.Locale.getDefault(),
                        "%.1f - %.1f %s",
                        minVal,
                        maxVal,
                        settings.weightUnitLabel,
                    ),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryColor,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
            ) {
                val padX = 18.dp.toPx()
                val padY = 14.dp.toPx()
                val chartW = (size.width - padX * 2).coerceAtLeast(1f)
                val chartH = (size.height - padY * 2).coerceAtLeast(1f)

                repeat(3) { step ->
                    val y = padY + chartH * (step / 2f)
                    drawLine(
                        color = gridColor,
                        start = Offset(padX, y),
                        end = Offset(padX + chartW, y),
                        strokeWidth = 1.dp.toPx(),
                    )
                }

                val coords = points.mapIndexed { index, (v, _) ->
                    val x = if (points.size == 1) {
                        padX + chartW / 2f
                    } else {
                        padX + chartW * (index.toFloat() / (points.size - 1))
                    }
                    val normalized = (v - minVal) / span
                    val y = padY + chartH * (1f - normalized)
                    Offset(x, y)
                }

                if (coords.size >= 2) {
                    val linePath = Path().apply {
                        moveTo(coords.first().x, coords.first().y)
                        coords.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(coords.last().x, padY + chartH)
                        lineTo(coords.first().x, padY + chartH)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.28f),
                                primaryColor.copy(alpha = 0.0f),
                            ),
                            startY = padY,
                            endY = padY + chartH,
                        ),
                    )
                    drawPath(
                        path = linePath,
                        color = primaryColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }

                coords.forEach { pt ->
                    drawCircle(
                        color = primaryColor,
                        radius = 5.dp.toPx(),
                        center = pt,
                    )
                    drawCircle(
                        color = colorScheme.surface,
                        radius = 2.2.dp.toPx(),
                        center = pt,
                    )
                }
            }
        }
    }
}

@Composable
fun WeightRecordItemCard(
    item: WeightRecordModel,
    modifier: Modifier = Modifier,
    settings: AppSettings = AppSettings(),
) {
    val colorScheme = MaterialTheme.colorScheme
    val displayWeight = if (item.weightKg > 0.0) settings.formatWeight(item.weightKg) else item.weight
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PetCareIcons.WeightScale,
                        contentDescription = "Cân nặng",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = displayWeight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.dateTime,
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (item.status == "Ổn định" || item.status == "Khỏe mạnh") colorScheme.primaryContainer else colorScheme.tertiaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.status == "Ổn định" || item.status == "Khỏe mạnh") colorScheme.primary else colorScheme.tertiary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeightHistoryScreenPreview() {
    PetCareTheme {
        WeightHistoryScreen()
    }
}
