package com.example.petcare.ui.screens

import android.content.res.Configuration
import android.widget.Space
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.petcare.R
import com.example.petcare.ui.components.PetCareTopAppBar
import com.example.petcare.ui.theme.BackgroundLight
import com.example.petcare.ui.theme.BorderLight
import com.example.petcare.ui.theme.GreenPrimary
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.theme.TextPrimary
import com.example.petcare.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun OtpVerificationScreen(
    modifier: Modifier = Modifier,
    targetDestination: String = "dat@example.com",
    onBackClick: () -> Unit = {},
    onVerifyClick: (String) -> Unit = {},
    onResendClick: () -> Unit = {},
) {
    var otpCode by remember { mutableStateOf("") }
    var countdown by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(true) }

    // Timer countdown effect
    LaunchedEffect(isTimerRunning, countdown) {
        if (isTimerRunning && countdown > 0) {
            delay(1000L)
            countdown--
        } else if (countdown == 0) {
            isTimerRunning = false
        }
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
            title = stringResource(R.string.otp_title),
            subtitle = stringResource(R.string.otp_subtitle_format, targetDestination),
        )

        Spacer(modifier = Modifier.height(36.dp))

        // 4-Digit OTP Box Grid using BasicTextField Overlay
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            BasicTextField(
                value = otpCode,
                onValueChange = { newValue ->
                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                        otpCode = newValue
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
                decorationBox = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(4) { index ->
                            val digit = otpCode.getOrNull(index)?.toString() ?: ""
                            val isFocusedBox = otpCode.length == index || (otpCode.length == 4 && index == 3)
                            val borderColor = if (isFocusedBox || digit.isNotEmpty()) GreenPrimary else BorderLight

                            Box(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 64.dp)
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .border(1.5.dp, borderColor, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = digit,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                },
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Resend OTP Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (countdown > 0) {
                Text(
                    text = stringResource(R.string.otp_resend_prefix),
                    fontSize = 13.sp,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "00:${countdown.toString().padStart(2, '0')}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                )
            } else {
                Text(
                    text = stringResource(R.string.otp_did_not_receive),
                    fontSize = 13.sp,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.btn_resend_otp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary,
                    modifier = Modifier.clickable {
                        countdown = 60
                        isTimerRunning = true
                        onResendClick()
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Verify Button
        Button(
            onClick = { onVerifyClick(otpCode) },
            enabled = otpCode.length == 4,
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
                text = stringResource(R.string.btn_confirm),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OtpVerificationScreenPreview() {
    PetCareTheme {
        OtpVerificationScreen()
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun OtpVerificationScreenDarkPreview() {
    PetCareTheme(darkTheme = true) {
        OtpVerificationScreen()
    }
}
