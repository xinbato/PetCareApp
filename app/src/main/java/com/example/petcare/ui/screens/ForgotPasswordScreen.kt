package com.example.petcare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.ui.components.PetCareTextField
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary
import com.example.petcare.utils.InputType
import com.example.petcare.utils.InputTypeUtils

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onSendOtpClick: (String) -> Unit = {},
    onBackToLoginClick: () -> Unit = {},
) {
    var emailOrPhone by remember { mutableStateOf("") }

    val emailOrPhoneType = remember(emailOrPhone) {
        InputTypeUtils.determineEmailOrPhone(emailOrPhone)
    }

    val helperText = when {
        emailOrPhone.isBlank() -> null
        emailOrPhoneType == InputType.EMAIL -> "✓ Loại: Email"
        emailOrPhoneType == InputType.PHONE -> "✓ Loại: Số điện thoại"
        else -> "Nhập Email hoặc SĐT hợp lệ"
    }

    val helperColor = if (emailOrPhoneType == InputType.UNKNOWN && emailOrPhone.isNotBlank()) {
        Color(0xFFE53935)
    } else {
        GreenPrimary
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        // Top Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(start = 0.dp),
        ) {
            Text(
                text = "‹",
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                color = TextPrimary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title and Subtitle
        Text(
            text = "Quên mật khẩu",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Nhập email hoặc số điện thoại của bạn để nhận mã xác thực OTP khôi phục mật khẩu.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Input Field
        PetCareTextField(
            value = emailOrPhone,
            onValueChange = { emailOrPhone = it },
            label = "Email / Số điện thoại",
            placeholder = "Nhập email hoặc số điện thoại",
            helperText = helperText,
            helperColor = helperColor,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Send OTP Button
        Button(
            onClick = { onSendOtpClick(emailOrPhone) },
            enabled = emailOrPhoneType != InputType.UNKNOWN,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = Color.White,
                disabledContainerColor = GreenPrimary.copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.7f),
            ),
        ) {
            Text(
                text = "Gửi mã OTP",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Back to Login Link
        Text(
            text = "Quay lại đăng nhập",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreenPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBackToLoginClick() }
                .padding(vertical = 8.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    PetCareTheme {
        ForgotPasswordScreen()
    }
}
