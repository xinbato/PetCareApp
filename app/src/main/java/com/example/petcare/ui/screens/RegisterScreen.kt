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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petcare.R
import com.example.petcare.ui.components.PetCareTextField
import com.example.petcare.ui.components.PetCareTopAppBar
import com.example.petcare.ui.components.SocialLoginSection
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.DoctorLinkBlue
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.RedError
import com.example.petcare.ui.theme.TextSecondary
import com.example.petcare.utils.InputType
import com.example.petcare.utils.InputTypeUtils

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onRegisterSuccessClick: (String) -> Unit = {},
    onRegisterErrorClick: () -> Unit = {},
    onDoctorRegisterClick: () -> Unit = {},
) {
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Validation & Input Type detection logic
    val isUsernameValid = remember(username) { InputTypeUtils.isUsername(username) }
    val usernameHelperText = when {
        username.isBlank() -> null
        isUsernameValid -> stringResource(R.string.helper_username_valid)
        else -> stringResource(R.string.helper_username_min)
    }
    val usernameHelperColor = if (!isUsernameValid && username.isNotBlank()) {
        RedError
    } else {
        GreenPrimary
    }

    val emailOrPhoneType = remember(emailOrPhone) {
        InputTypeUtils.determineEmailOrPhone(emailOrPhone)
    }
    val emailOrPhoneHelperText = when {
        emailOrPhone.isBlank() -> null
        emailOrPhoneType == InputType.EMAIL -> stringResource(R.string.helper_type_email)
        emailOrPhoneType == InputType.PHONE -> stringResource(R.string.helper_type_phone)
        else -> stringResource(R.string.helper_enter_email_phone)
    }
    val emailOrPhoneHelperColor = if (emailOrPhoneType == InputType.UNKNOWN && emailOrPhone.isNotBlank()) {
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
            title = stringResource(R.string.register_title),
            subtitle = stringResource(R.string.register_subtitle),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input Fields
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PetCareTextField(
                value = username,
                onValueChange = { username = it },
                label = stringResource(R.string.label_username),
                placeholder = stringResource(R.string.placeholder_username),
                helperText = usernameHelperText,
                helperColor = usernameHelperColor,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
            )

            PetCareTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = stringResource(R.string.label_fullname),
                placeholder = stringResource(R.string.placeholder_fullname),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
            )

            PetCareTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                label = stringResource(R.string.label_email_phone),
                placeholder = stringResource(R.string.placeholder_email_phone),
                helperText = emailOrPhoneHelperText,
                helperColor = emailOrPhoneHelperColor,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )

            PetCareTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.label_password),
                placeholder = stringResource(R.string.placeholder_create_password),
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

        // Terms and Conditions Note
        Text(
            text = stringResource(R.string.terms_note),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Create Account Button
        Button(
            onClick = {
                if (!isUsernameValid || emailOrPhoneType == InputType.UNKNOWN) {
                    onRegisterErrorClick()
                } else {
                    val displayName = fullName.ifBlank { username.ifBlank { emailOrPhone } }
                    onRegisterSuccessClick(displayName)
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
                text = stringResource(R.string.btn_create_account),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Social Login Section
        SocialLoginSection()

        Spacer(modifier = Modifier.height(32.dp))

        // Doctor Register Link
        Text(
            text = stringResource(R.string.btn_doctor_register),
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic,
            color = DoctorLinkBlue,
            textDecoration = TextDecoration.Underline,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDoctorRegisterClick() }
                .padding(vertical = 8.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    PetCareTheme {
        RegisterScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RegisterScreenDarkPreview() {
    PetCareTheme(darkTheme = true) {
        RegisterScreen()
    }
}
