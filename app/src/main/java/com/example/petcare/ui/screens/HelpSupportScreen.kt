package com.example.petcare.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.petcare.ui.components.KeyboardArrowLeftIcon
import com.example.petcare.ui.theme.PetCareTheme

@Composable
fun HelpSupportScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onRequestSent: () -> Unit = {},
) {
    val context = LocalContext.current
    var selectedOption by remember { mutableStateOf<String?>("faq") }
    var feedbackNote by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
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
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Trợ giúp & hỗ trợ",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                HelpSupportCardItem(
                    title = "Câu hỏi thường gặp",
                    isSelected = selectedOption == "faq",
                    onClick = {
                        selectedOption = "faq"
                        showSuccessMessage = false
                    }
                )
                if (selectedOption == "faq") {
                    HelpDetailBox(
                        title = "Câu hỏi thường gặp",
                        body = "1. Thêm thú cưng bằng AI: chọn Thêm thú cưng bằng AI, chụp hoặc nhập mô tả ngắn để Gemini tự điền hồ sơ và gợi ý mũi tiêm.\n" +
                            "2. Đổi đơn vị & giao diện: vào Tôi -> Cài đặt để chọn kg/lb và giao diện Sáng/Tối.\n" +
                            "3. Sao lưu từ Khách: vào tab Tôi -> Liên kết Email ngay để lưu dữ liệu lâu dài.",
                    )
                }

                HelpSupportCardItem(
                    title = "Hướng dẫn sử dụng",
                    isSelected = selectedOption == "guide",
                    onClick = {
                        selectedOption = "guide"
                        showSuccessMessage = false
                    }
                )
                if (selectedOption == "guide") {
                    HelpDetailBox(
                        title = "Hướng dẫn nhanh",
                        body = "- Tab Trang chủ: xem tổng quan và thêm nhanh thú cưng.\n" +
                            "- Tab Sức khỏe: theo dõi tiêm phòng, thuốc uống hàng ngày và biểu đồ cân nặng.\n" +
                            "- Tab Lịch: quản lý lịch hẹn tắm, khám và nhắc giờ tự động.\n" +
                            "- Tab Bản đồ: tìm phòng khám thú y gần bạn theo GPS.",
                    )
                }

                HelpSupportCardItem(
                    title = "Liên hệ hỗ trợ",
                    isSelected = selectedOption == "contact",
                    onClick = {
                        selectedOption = "contact"
                        showSuccessMessage = false
                    }
                )
                if (selectedOption == "contact") {
                    HelpDetailBox(
                        title = "Kênh liên hệ",
                        body = "Email: support@petcare.vn\nGiờ hỗ trợ: 08:00 - 20:00 (Thứ 2 - Chủ nhật)\nBạn có thể nhập nội dung cần hỗ trợ bên dưới.",
                    )
                }

                HelpSupportCardItem(
                    title = "Báo cáo vấn đề",
                    isSelected = selectedOption == "report",
                    onClick = {
                        selectedOption = "report"
                        showSuccessMessage = false
                    }
                )
                if (selectedOption == "report") {
                    HelpDetailBox(
                        title = "Mô tả lỗi hoặc góp ý",
                        body = "Ghi rõ màn hình đang mở và bước thao tác gây lỗi để đội ngũ kiểm tra nhanh hơn.",
                    )
                }

                if (selectedOption == "contact" || selectedOption == "report") {
                    OutlinedTextField(
                        value = feedbackNote,
                        onValueChange = { feedbackNote = it },
                        label = { Text("Nội dung cần hỗ trợ / mô tả lỗi") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    )
                }

                if (showSuccessMessage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Yêu cầu hỗ trợ của bạn đã được ghi nhận!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        val requiresText = selectedOption == "contact" || selectedOption == "report"
                        if (requiresText && feedbackNote.trim().isEmpty()) {
                            Toast.makeText(context, "Vui lòng nhập nội dung cần hỗ trợ.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        showSuccessMessage = true
                        feedbackNote = ""
                        Toast.makeText(context, "Đã ghi nhận yêu cầu hỗ trợ.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Gửi yêu cầu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpDetailBox(
    title: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = body,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 19.sp,
        )
    }
}

@Composable
private fun HelpSupportCardItem(
    title: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HelpSupportScreenPreview() {
    PetCareTheme {
        HelpSupportScreen()
    }
}
