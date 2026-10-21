package com.example.petcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.petcare.ui.navigation.AppScreen
/*import com.example.petcare.ui.screens.AddPetCameraScreen
import com.example.petcare.ui.screens.AddPetHandmadeScreen
import com.example.petcare.ui.screens.AddPetOptionScreen*/
import com.example.petcare.ui.screens.ErrorScreen
import com.example.petcare.ui.screens.ForgotPasswordScreen
import com.example.petcare.ui.screens.HomeScreen
import com.example.petcare.ui.screens.LoginScreen
import com.example.petcare.ui.screens.OnboardingScreen
import com.example.petcare.ui.screens.OtpVerificationScreen
import com.example.petcare.ui.screens.RegisterScreen
import com.example.petcare.ui.screens.ResetPasswordScreen
import com.example.petcare.ui.screens.SuccessScreen
import com.example.petcare.ui.theme.PetCareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetCareTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.ONBOARDING) }
                var loggedInUserName by remember { mutableStateOf("Nguyễn Văn A") }
                var otpTargetDestination by remember { mutableStateOf("dat@example.com") }
                var scannedPetBreed by remember { mutableStateOf("") }

                when (currentScreen) {
                    AppScreen.ONBOARDING -> {
                        OnboardingScreen(
                            modifier = Modifier.fillMaxSize(),
                            onGetStartedClick = {
                                currentScreen = AppScreen.REGISTER
                            },
                            onLoginClick = {
                                currentScreen = AppScreen.LOGIN
                            },
                        )
                    }

                    AppScreen.LOGIN -> {
                        LoginScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.ONBOARDING
                            },
                            onLoginSuccessClick = { enteredName ->
                                if (enteredName.isNotBlank()) {
                                    loggedInUserName = enteredName
                                }
                                currentScreen = AppScreen.HOME
                            },
                            onLoginErrorClick = {
                                currentScreen = AppScreen.ERROR
                            },
                            onRegisterClick = {
                                currentScreen = AppScreen.REGISTER
                            },
                            onForgotPasswordClick = {
                                currentScreen = AppScreen.FORGOT_PASSWORD
                            },
                        )
                    }

                    AppScreen.REGISTER -> {
                        RegisterScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.ONBOARDING
                            },
                            onRegisterSuccessClick = { enteredName ->
                                if (enteredName.isNotBlank()) {
                                    loggedInUserName = enteredName
                                }
                                currentScreen = AppScreen.HOME
                            },
                            onRegisterErrorClick = {
                                currentScreen = AppScreen.ERROR
                            },
                            onDoctorRegisterClick = {
                                currentScreen = AppScreen.HOME
                            },
                        )
                    }

                    AppScreen.FORGOT_PASSWORD -> {
                        ForgotPasswordScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.LOGIN
                            },
                            onSendOtpClick = { target ->
                                otpTargetDestination = target
                                currentScreen = AppScreen.OTP_VERIFICATION
                            },
                            onBackToLoginClick = {
                                currentScreen = AppScreen.LOGIN
                            },
                        )
                    }

                    AppScreen.OTP_VERIFICATION -> {
                        OtpVerificationScreen(
                            modifier = Modifier.fillMaxSize(),
                            targetDestination = otpTargetDestination,
                            onBackClick = {
                                currentScreen = AppScreen.FORGOT_PASSWORD
                            },
                            onVerifyClick = { _ ->
                                currentScreen = AppScreen.RESET_PASSWORD
                            },
                            onResendClick = {
                                // Resend OTP logic
                            },
                        )
                    }

                    AppScreen.RESET_PASSWORD -> {
                        ResetPasswordScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.OTP_VERIFICATION
                            },
                            onResetSuccessClick = {
                                currentScreen = AppScreen.SUCCESS
                            },
                        )
                    }

                    AppScreen.HOME -> {
                        HomeScreen(
                            modifier = Modifier.fillMaxSize(),
                            userName = loggedInUserName,
                            onAddPetClick = {
                                currentScreen = AppScreen.ADD_PET_OPTION
                            },
                            onPetDetailClick = { _ ->
                                // Navigates to pet details
                            },
                        )
                    }



                    AppScreen.SUCCESS -> {
                        SuccessScreen(
                            modifier = Modifier.fillMaxSize(),
                            title = "Thao tác thành công!",
                            message = "Thông tin đã được cập nhật thành công. Hãy tiếp tục quản lý và chăm sóc thú cưng cùng PetCare nhé!",
                            onDoneClick = {
                                currentScreen = AppScreen.HOME
                            },
                            onBackClick = {
                                currentScreen = AppScreen.HOME
                            },
                        )
                    }

                    AppScreen.ERROR -> {
                        ErrorScreen(
                            modifier = Modifier.fillMaxSize(),
                            title = "Đã xảy ra lỗi!",
                            message = "Không thể hoàn tất thao tác. Vui lòng kiểm tra lại thông tin và thử lại.",
                            onRetryClick = {
                                currentScreen = AppScreen.HOME
                            },
                            onBackClick = {
                                currentScreen = AppScreen.HOME
                            },
                        )
                    }

                    else -> {
                        // Placeholders for other screens
                    }
                }
            }
        }
    }
}
