package com.example.petcare.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.ui.components.PetCareTextField
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary
import com.example.petcare.utils.InputType
import com.example.petcare.utils.InputTypeUtils

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onLoginSuccessClick: (String) -> Unit = {},
    onLoginErrorClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Input type detection logic
    val detectedInputType = remember(emailOrPhone) {
        InputTypeUtils.determineInputType(emailOrPhone)
    }

    val helperText = when {
        emailOrPhone.isBlank() -> null
        detectedInputType == InputType.EMAIL -> "✓ Loại: Email"
        detectedInputType == InputType.PHONE -> "✓ Loại: Số điện thoại"
        detectedInputType == InputType.USERNAME -> "✓ Loại: Tên đăng nhập"
        else -> "Định dạng chưa đúng"
    }

    val helperColor = if (detectedInputType == InputType.UNKNOWN && emailOrPhone.isNotBlank()) {
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
            text = "Đăng nhập",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Theo dõi sức khỏe và lịch chăm sóc",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Input Fields
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PetCareTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                label = "Email/ Số điện thoại/ Tên đăng nhập",
                placeholder = "Nhập email, số điện thoại hoặc tên đăng nhập",
                helperText = helperText,
                helperColor = helperColor,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )

            PetCareTextField(
                value = password,
                onValueChange = { password = it },
                label = "Mật khẩu",
                placeholder = "Nhập mật khẩu",
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Text(
                        text = if (isPasswordVisible) "Ẩn" else "Hiện",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary,
                        modifier = Modifier
                            .clickable { isPasswordVisible = !isPasswordVisible }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Forgot Password
        Text(
            text = "Quên mật khẩu?",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreenPrimary,
            modifier = Modifier
                .clickable { onForgotPasswordClick() }
                .padding(vertical = 4.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login & Register Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = {
                    if (detectedInputType == InputType.UNKNOWN && emailOrPhone.isNotBlank()) {
                        onLoginErrorClick()
                    } else {
                        onLoginSuccessClick(emailOrPhone)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "Đăng nhập",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = GreenPrimary,
                ),
            ) {
                Text(
                    text = "Tạo tài khoản",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenPrimary,
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Social Login Divider
        Text(
            text = "Hoặc",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Social Login Buttons (Google and Yahoo - disabled temporarily)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedButton(
                onClick = { /* Temporarily disabled */ },
                enabled = false,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.6f),
                    contentColor = TextPrimary,
                    disabledContentColor = TextPrimary.copy(alpha = 0.5f),
                ),
            ) {
                Text(
                    text = "Google",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            OutlinedButton(
                onClick = { /* Temporarily disabled */ },
                enabled = false,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.6f),
                    contentColor = TextPrimary,
                    disabledContentColor = TextPrimary.copy(alpha = 0.5f),
                ),
            ) {
                Text(
                    text = "yahoo",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    PetCareTheme {
        LoginScreen()
    }
}
