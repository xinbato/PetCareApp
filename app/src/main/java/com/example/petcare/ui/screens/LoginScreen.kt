package com.example.petcare.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.petcare.R
import com.example.petcare.ui.components.PetCareTextField
import com.example.petcare.ui.components.PetCareTopAppBar
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import com.example.petcare.ui.components.SocialLoginSection
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.RedError
import com.example.petcare.ui.viewmodel.AuthViewModel
import com.example.petcare.utils.GoogleSignInHelper
import com.example.petcare.utils.InputType
import com.example.petcare.utils.InputTypeUtils

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onLoginSuccessClick: (String) -> Unit = {},
    onLoginErrorClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var inlineError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Input type detection logic — chỉ email mới được đăng nhập Firebase
    val detectedInputType = remember(emailOrPhone) {
        InputTypeUtils.determineInputType(emailOrPhone)
    }

    val helperText = when {
        emailOrPhone.isBlank() -> null
        detectedInputType == InputType.EMAIL -> stringResource(R.string.helper_type_email)
        detectedInputType == InputType.PHONE -> stringResource(R.string.helper_type_phone)
        detectedInputType == InputType.USERNAME -> stringResource(R.string.helper_type_username)
        else -> stringResource(R.string.helper_invalid_format)
    }

    val helperColor = if (detectedInputType == InputType.UNKNOWN && emailOrPhone.isNotBlank()) {
        RedError
    } else {
        MaterialTheme.colorScheme.primary
    }

    LaunchedEffect(Unit) {
        viewModel.tryGoogleAutoSignIn(context) { name ->
            onLoginSuccessClick(name)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
    Column(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        // Top Back Button & Header
        PetCareTopAppBar(
            onBackClick = onBackClick,
            title = stringResource(R.string.login_title),
            subtitle = stringResource(R.string.login_subtitle),
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Input Fields
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PetCareTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                label = stringResource(R.string.label_login_input),
                placeholder = stringResource(R.string.placeholder_login_input),
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
                label = stringResource(R.string.label_password),
                placeholder = stringResource(R.string.placeholder_password),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Text(
                        text = if (isPasswordVisible) {
                            stringResource(R.string.action_hide_password)
                        } else {
                            stringResource(R.string.action_show_password)
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
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
            text = stringResource(R.string.btn_forgot_password),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable { onForgotPasswordClick() }
                .padding(vertical = 4.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hiển thị lỗi nội tuyến thay vì navigate sang ErrorScreen
        if (inlineError != null) {
            Text(
                text = inlineError!!,
                fontSize = 13.sp,
                color = RedError,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        // Login & Register Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = {
                    inlineError = null
                    viewModel.login(
                        emailOrPhoneInput = emailOrPhone,
                        passwordInput = password,
                        onSuccess = onLoginSuccessClick,
                        onError = { msg -> inlineError = msg }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.btn_login),
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
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.btn_create_account),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Social Login Section
        SocialLoginSection(
            onGoogleClick = {
                inlineError = null
                GoogleSignInHelper.launchGoogleSignIn(
                    context = context,
                    coroutineScope = coroutineScope,
                    viewModel = viewModel,
                    onSuccess = onLoginSuccessClick,
                    onError = { msg -> inlineError = msg }
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        androidx.compose.material3.TextButton(
            onClick = {
                inlineError = null
                viewModel.signInAsGuest(
                    onSuccess = onLoginSuccessClick,
                    onError = { msg -> inlineError = msg }
                )
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Tiếp tục với vai trò Khách",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
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

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoginScreenDarkPreview() {
    PetCareTheme(darkTheme = true) {
        LoginScreen()
    }
}
