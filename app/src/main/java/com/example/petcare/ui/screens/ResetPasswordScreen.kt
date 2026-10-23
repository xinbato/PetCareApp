package com.example.petcare.ui.screens

import android.content.res.Configuration
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
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.RedError

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
        isPasswordValid -> stringResource(R.string.helper_password_valid)
        else -> stringResource(R.string.helper_password_min)
    }

    val confirmHelperText = when {
        confirmPassword.isEmpty() -> null
        isPasswordMatching -> stringResource(R.string.helper_password_match)
        else -> stringResource(R.string.helper_password_mismatch)
    }

    val confirmHelperColor = if (!isPasswordMatching && confirmPassword.isNotEmpty()) {
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
            title = stringResource(R.string.reset_password_title),
            subtitle = stringResource(R.string.reset_password_subtitle),
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Input Fields
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PetCareTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = stringResource(R.string.label_new_password),
                placeholder = stringResource(R.string.placeholder_new_password),
                helperText = passwordHelperText,
                helperColor = if (isPasswordValid) GreenPrimary else RedError,
                visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Text(
                        text = if (isNewPasswordVisible) {
                            stringResource(R.string.action_hide_password)
                        } else {
                            stringResource(R.string.action_show_password)
                        },
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
                label = stringResource(R.string.label_confirm_password),
                placeholder = stringResource(R.string.placeholder_confirm_password),
                helperText = confirmHelperText,
                helperColor = confirmHelperColor,
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Text(
                        text = if (isConfirmPasswordVisible) {
                            stringResource(R.string.action_hide_password)
                        } else {
                            stringResource(R.string.action_show_password)
                        },
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
                text = stringResource(R.string.btn_save_password),
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

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ResetPasswordScreenDarkPreview() {
    PetCareTheme(darkTheme = true) {
        ResetPasswordScreen()
    }
}
