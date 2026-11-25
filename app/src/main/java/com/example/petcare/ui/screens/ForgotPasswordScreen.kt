package com.example.petcare.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import com.example.petcare.R
import com.example.petcare.ui.components.PetCareTextField
import com.example.petcare.ui.components.PetCareTopAppBar
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.RedError
import com.example.petcare.ui.viewmodel.AuthViewModel
import com.example.petcare.utils.InputType
import com.example.petcare.utils.InputTypeUtils

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onSendOtpClick: (String) -> Unit = {},
    onBackToLoginClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var emailOrPhone by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var sentEmailTarget by remember { mutableStateOf("") }

    val emailOrPhoneType = remember(emailOrPhone) {
        InputTypeUtils.determineEmailOrPhone(emailOrPhone)
    }

    val helperText = when {
        emailOrPhone.isBlank() -> null
        emailOrPhoneType == InputType.EMAIL -> stringResource(R.string.helper_type_email)
        emailOrPhoneType == InputType.PHONE -> stringResource(R.string.helper_type_phone)
        else -> stringResource(R.string.helper_valid_email_phone)
    }

    val helperColor = if (emailOrPhoneType == InputType.UNKNOWN && emailOrPhone.isNotBlank()) {
        RedError
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        // Top Back Button & Header
        PetCareTopAppBar(
            onBackClick = onBackClick,
            title = stringResource(R.string.forgot_password_title),
            subtitle = stringResource(R.string.forgot_password_subtitle),
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Input Field
        PetCareTextField(
            value = emailOrPhone,
            onValueChange = { emailOrPhone = it },
            label = stringResource(R.string.label_email_or_phone),
            placeholder = stringResource(R.string.placeholder_email_or_phone),
            helperText = helperText,
            helperColor = helperColor,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
        )

        // Error message if any
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage.orEmpty(),
                color = RedError,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Send Link Button
        Button(
            onClick = {
                viewModel.sendForgotPasswordOtp(
                    destination = emailOrPhone,
                    onSuccess = { email ->
                        sentEmailTarget = email
                        showSuccessDialog = true
                    },
                    onError = {}
                )
            },
            enabled = emailOrPhone.isNotBlank() && !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
            ),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Gửi liên kết khôi phục",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Back to Login Link
        Text(
            text = stringResource(R.string.btn_back_to_login),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBackToLoginClick() }
                .padding(vertical = 8.dp),
        )

        // Success Confirmation Dialog
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = {
                    Text(
                        text = "Đã gửi liên kết khôi phục",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = "Liên kết đặt lại mật khẩu đã được gửi đến email:\n$sentEmailTarget\n\nVui lòng mở hộp thư đến (kiểm tra cả mục Spam/Thư rác) và nhấn vào liên kết để tạo mật khẩu mới.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onBackToLoginClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "Quay lại Đăng nhập", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreview() {
    PetCareTheme {
        ForgotPasswordScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ForgotPasswordScreenDarkPreview() {
    PetCareTheme(darkTheme = true) {
        ForgotPasswordScreen()
    }
}
