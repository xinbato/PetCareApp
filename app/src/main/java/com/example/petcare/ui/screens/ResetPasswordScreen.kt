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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.ui.components.PetCareTextField
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary

@Composable
fun ResetPasswordScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onResetSuccessClick: () -> Unit = {},
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    val isPasswordValid = newPassword.length >= 6
    val isPasswordMatching = newPassword.isNotEmpty() && newPassword == confirmPassword

    val passwordHelperText = when {
        newPassword.isEmpty() -> null
        isPasswordValid -> "✓ Mật khẩu hợp lệ"
        else -> "Tối thiểu 6 ký tự"
    }

    val confirmHelperText = when {
        confirmPassword.isEmpty() -> null
        isPasswordMatching -> "✓ Mật khẩu trùng khớp"
        else -> "Mật khẩu không trùng khớp"
    }

    val confirmHelperColor = if (!isPasswordMatching && confirmPassword.isNotEmpty()) {
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
            text = "Đặt lại mật khẩu",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Tạo mật khẩu mới cho tài khoản của bạn.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Input Fields
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PetCareTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Mật khẩu mới",
                placeholder = "Tạo mật khẩu mới",
                helperText = passwordHelperText,
                helperColor = if (isPasswordValid) GreenPrimary else Color(0xFFE53935),
                visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Text(
                        text = if (isNewPasswordVisible) "Ẩn" else "Hiện",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary,
                        modifier = Modifier
                            .clickable { isNewPasswordVisible = !isNewPasswordVisible }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
            )

            PetCareTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Xác nhận mật khẩu",
                placeholder = "Nhập lại mật khẩu mới",
                helperText = confirmHelperText,
                helperColor = confirmHelperColor,
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Text(
                        text = if (isConfirmPasswordVisible) "Ẩn" else "Hiện",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary,
                        modifier = Modifier
                            .clickable { isConfirmPasswordVisible = !isConfirmPasswordVisible }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Reset Password Button
        Button(
            onClick = onResetSuccessClick,
            enabled = isPasswordValid && isPasswordMatching,
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
                text = "Lưu mật khẩu",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResetPasswordScreenPreview() {
    PetCareTheme {
        ResetPasswordScreen()
    }
}
