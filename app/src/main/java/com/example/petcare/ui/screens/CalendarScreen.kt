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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.data.model.ScheduleModel
import com.example.petcare.ui.components.PetCareTab
import com.example.petcare.ui.components.PetCareTabBar
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary
import com.example.petcare.ui.viewmodel.CalendarViewModel

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel(),
    onAddScheduleClick: () -> Unit = {},
    onEditScheduleClick: (String) -> Unit = {},
    onTabSelected: (PetCareTab) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val schedules = uiState.schedules

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
            // Main content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Title: Lịch chăm sóc
                Text(
                    text = "Lịch chăm sóc",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle: Tất cả lịch nhắc quan trọng
                Text(
                    text = "Tất cả lịch nhắc quan trọng",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Today Green Banner Card
                TodayGreenBannerCard(
                    todayCount = uiState.todaySchedules.size
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Schedule list
                if (schedules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa có lịch nhắc nào. Hãy nhấn + Thêm để tạo mới!",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                } else {
                    if (uiState.todaySchedules.isNotEmpty()) {
                        Text(
                            text = "Việc hôm nay",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.todaySchedules.forEach { schedule ->
                                ScheduleCardItem(
                                    schedule = schedule,
                                    onClick = { onEditScheduleClick(schedule.id) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (uiState.upcomingSchedules.isNotEmpty()) {
                        Text(
                            text = "Việc sắp tới",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            uiState.upcomingSchedules.forEach { schedule ->
                                ScheduleCardItem(
                                    schedule = schedule,
                                    onClick = { onEditScheduleClick(schedule.id) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(90.dp)) // Space for FAB & TabBar
            }

            // Bottom Navigation Bar
            PetCareTabBar(
                selectedTab = PetCareTab.CALENDAR.ordinal,
                onTabSelected = { index -> onTabSelected(PetCareTab.entries[index]) }
            )
        }

        // Floating Action Button (+ Thêm) matching Image 1
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 80.dp)
                .clip(RoundedCornerShape(50.dp))
                .clickable { onAddScheduleClick() },
            color = GreenPrimary,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm lịch",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Thêm",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TodayGreenBannerCard(
    todayCount: Int,
) {
    val calendar = androidx.compose.runtime.remember { java.util.Calendar.getInstance() }
    val monthYearText = androidx.compose.runtime.remember(calendar) {
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val year = calendar.get(java.util.Calendar.YEAR)
        "THÁNG $month, $year"
    }
    val dayOfMonthText = androidx.compose.runtime.remember(calendar) {
        calendar.get(java.util.Calendar.DAY_OF_MONTH).toString()
    }
    val dayOfWeekText = androidx.compose.runtime.remember(calendar) {
        when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Thứ Hai"
            java.util.Calendar.TUESDAY -> "Thứ Ba"
            java.util.Calendar.WEDNESDAY -> "Thứ Tư"
            java.util.Calendar.THURSDAY -> "Thứ Năm"
            java.util.Calendar.FRIDAY -> "Thứ Sáu"
            java.util.Calendar.SATURDAY -> "Thứ Bảy"
            else -> "Chủ Nhật"
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GreenPrimary,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = monthYearText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dayOfMonthText,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = dayOfWeekText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Text(
                    text = "$todayCount việc hôm nay",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }
        }
    }
}

@Composable
private fun ScheduleCardItem(
    schedule: ScheduleModel,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time / Tag Label (e.g., 18:00, Ngày mai, Thứ 7)
            Text(
                text = schedule.timeOrDateLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary,
                modifier = Modifier.width(90.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Pet Name
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = schedule.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = schedule.petName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
    PetCareTheme {
        CalendarScreen()
    }
}
