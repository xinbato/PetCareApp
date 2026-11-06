package com.example.petcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.ui.components.PetCareTab
import com.example.petcare.ui.navigation.AppScreen
import com.example.petcare.ui.screens.AddNewMedicationScreen
import com.example.petcare.ui.screens.AddNewMedicalRecordBookScreen
import com.example.petcare.ui.screens.AddNewVaccinationScreen
import com.example.petcare.ui.screens.AddPetCameraScreen
import com.example.petcare.ui.screens.AddPetHandmadeScreen
import com.example.petcare.ui.screens.AddPetOptionScreen
import com.example.petcare.ui.screens.CheckupHistoryScreen
import com.example.petcare.ui.screens.EditPetScreen
import com.example.petcare.ui.screens.ErrorScreen
import com.example.petcare.ui.screens.ForgotPasswordScreen
import com.example.petcare.ui.screens.HealthDetailScreen
import com.example.petcare.ui.screens.HealthScreen
import com.example.petcare.ui.screens.HomeScreen
import com.example.petcare.ui.screens.ListPetScreen
import com.example.petcare.ui.screens.LoginScreen
import com.example.petcare.ui.screens.MedicalRecordBookScreen
import com.example.petcare.ui.screens.MedicationScreen
import com.example.petcare.ui.screens.OnboardingScreen
import com.example.petcare.ui.screens.OtpVerificationScreen
import com.example.petcare.ui.screens.RegisterScreen
import com.example.petcare.ui.screens.ResetPasswordScreen
import com.example.petcare.ui.screens.SelectPetToEditScreen
import com.example.petcare.ui.screens.SuccessScreen
import com.example.petcare.ui.screens.VaccinationScreen
import com.example.petcare.ui.screens.WeightHistoryScreen
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
                var selectedPetId by remember { mutableStateOf("1") }
                var listPetTab by remember { mutableStateOf(PetCareTab.HOME) }
                var healthDetailBackScreen by remember { mutableStateOf(AppScreen.HOME) }

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
                            onPetDetailClick = { petId ->
                                selectedPetId = petId
                                PetRepository.currentSelectedPetId.value = petId
                                healthDetailBackScreen = AppScreen.HOME
                                currentScreen = AppScreen.HEALTH_DETAIL
                            },
                            onViewAllPetsClick = {
                                listPetTab = PetCareTab.HOME
                                currentScreen = AppScreen.LIST_PET
                            },
                            onHealthTabClick = {
                                currentScreen = AppScreen.HEALTH
                            },
                        )
                    }

                    AppScreen.ADD_PET_OPTION -> {
                        AddPetOptionScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.HOME
                            },
                            onHandmadeClick = {
                                currentScreen = AppScreen.ADD_PET_HANDMADE
                            },
                            onCameraClick = {
                                currentScreen = AppScreen.ADD_PET_CAMERA
                            },
                        )
                    }

                    AppScreen.ADD_PET_CAMERA -> {
                        AddPetCameraScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.ADD_PET_OPTION
                            },
                            onScannedProceed = { breed ->
                                scannedPetBreed = breed
                                currentScreen = AppScreen.ADD_PET_HANDMADE
                            },
                        )
                    }

                    AppScreen.ADD_PET_HANDMADE -> {
                        AddPetHandmadeScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.ADD_PET_OPTION
                            },
                            onPetAdded = {
                                currentScreen = AppScreen.LIST_PET
                            },
                        )
                    }

                    AppScreen.LIST_PET -> {
                        ListPetScreen(
                            modifier = Modifier.fillMaxSize(),
                            currentTab = listPetTab,
                            onBackClick = {
                                currentScreen = AppScreen.HOME
                            },
                            onPetClick = { pet ->
                                selectedPetId = pet.id
                                PetRepository.currentSelectedPetId.value = pet.id
                                healthDetailBackScreen = AppScreen.LIST_PET
                                currentScreen = AppScreen.HEALTH_DETAIL
                            },
                            onEditPetClick = {
                                currentScreen = AppScreen.SELECT_PET_TO_EDIT
                            },
                            onAddPetClick = {
                                currentScreen = AppScreen.ADD_PET_HANDMADE
                            },
                            onTabSelected = { tab ->
                                when (tab) {
                                    PetCareTab.HOME -> {
                                        listPetTab = PetCareTab.HOME
                                        currentScreen = AppScreen.HOME
                                    }
                                    PetCareTab.HEALTH -> {
                                        currentScreen = AppScreen.HEALTH
                                    }
                                    else -> {}
                                }
                            },
                        )
                    }

                    AppScreen.HEALTH -> {
                        HealthScreen(
                            modifier = Modifier.fillMaxSize(),
                            onPetClick = { pet ->
                                selectedPetId = pet.id
                                PetRepository.currentSelectedPetId.value = pet.id
                                healthDetailBackScreen = AppScreen.HEALTH
                                currentScreen = AppScreen.HEALTH_DETAIL
                            },
                            onHistoryItemClick = { item ->
                                when (item.id) {
                                    "h1" -> {
                                        selectedPetId = "all"
                                        currentScreen = AppScreen.VACCINATION
                                    }
                                    "h2" -> {
                                        currentScreen = AppScreen.WEIGHT_HISTORY
                                    }
                                    "h3" -> {
                                        currentScreen = AppScreen.CHECKUP_HISTORY
                                    }
                                    else -> {}
                                }
                            },
                            onTabSelected = { tab ->
                                when (tab) {
                                    PetCareTab.HOME -> {
                                        listPetTab = PetCareTab.HOME
                                        currentScreen = AppScreen.HOME
                                    }
                                    PetCareTab.HEALTH -> {
                                        // Đang ở màn hình Sức khỏe
                                    }
                                    else -> {}
                                }
                            },
                        )
                    }

                    AppScreen.SELECT_PET_TO_EDIT -> {
                        SelectPetToEditScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.LIST_PET
                            },
                            onPetSelected = { petId ->
                                selectedPetId = petId
                                PetRepository.currentSelectedPetId.value = petId
                                currentScreen = AppScreen.EDIT_PET
                            },
                        )
                    }

                    AppScreen.EDIT_PET -> {
                        EditPetScreen(
                            petId = selectedPetId,
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.SELECT_PET_TO_EDIT
                            },
                            onSaved = {
                                currentScreen = AppScreen.LIST_PET
                            },
                            onDeleted = {
                                currentScreen = AppScreen.LIST_PET
                            },
                        )
                    }

                    AppScreen.HEALTH_DETAIL -> {
                        HealthDetailScreen(
                            petId = selectedPetId,
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = healthDetailBackScreen
                            },
                            onVaccinationClick = { petId ->
                                selectedPetId = petId
                                currentScreen = AppScreen.VACCINATION
                            },
                            onMedicationClick = { petId ->
                                selectedPetId = petId
                                currentScreen = AppScreen.MEDICATION
                            },
                            onMedicalRecordBookClick = { petId ->
                                selectedPetId = petId
                                currentScreen = AppScreen.MEDICATION_RECORD_BOOK
                            },
                            onEditPetClick = { petId ->
                                selectedPetId = petId
                                currentScreen = AppScreen.EDIT_PET
                            },
                            onTabSelected = { tab ->
                                when (tab) {
                                    PetCareTab.HOME -> {
                                        listPetTab = PetCareTab.HOME
                                        currentScreen = AppScreen.HOME
                                    }
                                    PetCareTab.HEALTH -> {
                                        currentScreen = AppScreen.HEALTH
                                    }
                                    else -> {}
                                }
                            },
                        )
                    }

                    AppScreen.VACCINATION -> {
                        VaccinationScreen(
                            petId = selectedPetId,
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = if (selectedPetId == "all") AppScreen.HEALTH else AppScreen.HEALTH_DETAIL
                            },
                            onAddVaccinationClick = { targetPetId ->
                                selectedPetId = targetPetId
                                currentScreen = AppScreen.ADD_NEW_VACCINATION
                            },
                            onViewInCalendarClick = {
                                // Calendar view
                            },
                        )
                    }

                    AppScreen.WEIGHT_HISTORY -> {
                        WeightHistoryScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.HEALTH
                            },
                        )
                    }

                    AppScreen.CHECKUP_HISTORY -> {
                        CheckupHistoryScreen(
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.HEALTH
                            },
                        )
                    }

                    AppScreen.ADD_NEW_VACCINATION -> {
                        AddNewVaccinationScreen(
                            petId = selectedPetId,
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.VACCINATION
                            },
                            onSaved = {
                                currentScreen = AppScreen.VACCINATION
                            },
                        )
                    }

                    AppScreen.MEDICATION -> {
                        MedicationScreen(
                            petId = selectedPetId,
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.HEALTH_DETAIL
                            },
                            onAddMedicationClick = {
                                currentScreen = AppScreen.ADD_NEW_MEDICATION
                            },
                            onViewInCalendarClick = {
                                // Calendar view
                            },
                        )
                    }

                    AppScreen.ADD_NEW_MEDICATION -> {
                        AddNewMedicationScreen(
                            petId = selectedPetId,
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.MEDICATION
                            },
                            onSaved = {
                                currentScreen = AppScreen.MEDICATION
                            },
                        )
                    }

                    AppScreen.MEDICATION_RECORD_BOOK -> {
                        MedicalRecordBookScreen(
                            petId = selectedPetId,
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.HEALTH_DETAIL
                            },
                            onAddNewRecordClick = {
                                currentScreen = AppScreen.ADD_NEW_MEDICATION_RECORD_BOOK
                            },
                            onTabSelected = { tab ->
                                when (tab) {
                                    PetCareTab.HOME -> {
                                        listPetTab = PetCareTab.HOME
                                        currentScreen = AppScreen.HOME
                                    }
                                    PetCareTab.HEALTH -> {
                                        currentScreen = AppScreen.HEALTH
                                    }
                                    else -> {}
                                }
                            },
                        )
                    }

                    AppScreen.ADD_NEW_MEDICATION_RECORD_BOOK -> {
                        AddNewMedicalRecordBookScreen(
                            initialPetId = selectedPetId,
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {
                                currentScreen = AppScreen.MEDICATION_RECORD_BOOK
                            },
                            onSaved = {
                                currentScreen = AppScreen.MEDICATION_RECORD_BOOK
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
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    PetCareTheme {
    }
}