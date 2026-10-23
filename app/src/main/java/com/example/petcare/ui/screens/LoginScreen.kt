package com.example.petcare.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.R
import com.example.petcare.ui.components.PetCareTextField
import com.example.petcare.ui.components.PetCareTopAppBar
import com.example.petcare.ui.components.SocialLoginSection
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.RedError
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
        detectedInputType == InputType.EMAIL -> stringResource(R.string.helper_type_email)
        detectedInputType == InputType.PHONE -> stringResource(R.string.helper_type_phone)
        detectedInputType == InputType.USERNAME -> stringResource(R.string.helper_type_username)
        else -> stringResource(R.string.helper_invalid_format)
    }

    val helperColor = if (detectedInputType == InputType.UNKNOWN && emailOrPhone.isNotBlank()) {
        RedError
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
            text = stringResource(R.string.btn_forgot_password),
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
                border = BorderStroke(1.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = GreenPrimary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.btn_create_account),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GreenPrimary,
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Social Login Section
        SocialLoginSection()
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
