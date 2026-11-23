package com.example.petcare.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.petcare.ui.components.PetCareTab
import com.example.petcare.ui.screens.AddEditScheduleScreen
import com.example.petcare.ui.screens.AddNewMedicalRecordBookScreen
import com.example.petcare.ui.screens.AddNewMedicationScreen
import com.example.petcare.ui.screens.AddNewVaccinationScreen
import com.example.petcare.ui.screens.AddPetCameraScreen
import com.example.petcare.ui.screens.AddPetHandmadeScreen
import com.example.petcare.ui.screens.AddPetOptionScreen
import com.example.petcare.ui.screens.CalendarScreen
import com.example.petcare.ui.screens.CheckupHistoryScreen
import com.example.petcare.ui.screens.EditPetScreen
import com.example.petcare.ui.screens.ErrorScreen
import com.example.petcare.ui.screens.ForgotPasswordScreen
import com.example.petcare.ui.screens.HealthDetailScreen
import com.example.petcare.ui.screens.HealthScreen
import com.example.petcare.ui.screens.HelpSupportScreen
import com.example.petcare.ui.screens.HomeScreen
import com.example.petcare.ui.screens.ListPetScreen
import com.example.petcare.ui.screens.LoginScreen
import com.example.petcare.ui.screens.MapScreen
import com.example.petcare.ui.screens.MedicalRecordBookScreen
import com.example.petcare.ui.screens.MedicationScreen
import com.example.petcare.ui.screens.NotificationReminderScreen
import com.example.petcare.ui.screens.OnboardingScreen
import com.example.petcare.ui.screens.OtpVerificationScreen
import com.example.petcare.ui.screens.PersonalInfoScreen
import com.example.petcare.ui.screens.ProfileScreen
import com.example.petcare.ui.screens.RegisterScreen
import com.example.petcare.ui.screens.ResetPasswordScreen
import com.example.petcare.ui.screens.SelectPetToEditScreen
import com.example.petcare.ui.screens.SettingsScreen
import com.example.petcare.ui.screens.SuccessScreen
import com.example.petcare.ui.screens.UserProfileDetailScreen
import com.example.petcare.ui.screens.VaccinationScreen
import com.example.petcare.ui.screens.WeightHistoryScreen
import com.example.petcare.ui.viewmodel.AuthViewModel

fun NavHostController.navigateToTab(tab: PetCareTab) {
    val route = when (tab) {
        PetCareTab.HOME -> AppRoute.Home.route
        PetCareTab.HEALTH -> AppRoute.Health.route
        PetCareTab.CALENDAR -> AppRoute.Calendar.route
        PetCareTab.MAP -> AppRoute.Map.route
        PetCareTab.PROFILE -> AppRoute.Profile.route
    }
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = remember {
        val user = runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }.getOrNull()
        if (user != null) AppRoute.Home.route else AppRoute.Onboarding.route
    },
) {
    val currentUser = remember {
        runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }.getOrNull()
    }
    var loggedInUserName by remember {
        mutableStateOf(
            currentUser?.displayName?.ifBlank { null }
                ?: currentUser?.email?.substringBefore("@")
                ?: ""
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppRoute.Onboarding.route) {
            OnboardingScreen(
                modifier = Modifier.fillMaxSize(),
                onGetStartedClick = { navController.navigate(AppRoute.Register.route) },
                onLoginClick = { navController.navigate(AppRoute.Login.route) }
            )
        }

        composable(AppRoute.Login.route) {
            LoginScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onLoginSuccessClick = { enteredName ->
                    if (enteredName.isNotBlank()) {
                        loggedInUserName = enteredName
                    }
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                    }
                },
                onLoginErrorClick = { navController.navigate(AppRoute.Error.route) },
                onRegisterClick = { navController.navigate(AppRoute.Register.route) },
                onForgotPasswordClick = { navController.navigate(AppRoute.ForgotPassword.route) }
            )
        }

        composable(AppRoute.Register.route) {
            RegisterScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onRegisterSuccessClick = { enteredName ->
                    if (enteredName.isNotBlank()) {
                        loggedInUserName = enteredName
                    }
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                    }
                },
                onRegisterErrorClick = { navController.navigate(AppRoute.Error.route) }
            )
        }

        composable(AppRoute.ForgotPassword.route) {
            ForgotPasswordScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onBackToLoginClick = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoute.OtpVerification.route,
            arguments = listOf(navArgument(AppRoute.OtpVerification.ARG_TARGET) { type = NavType.StringType })
        ) { backStackEntry ->
            val target = backStackEntry.arguments?.getString(AppRoute.OtpVerification.ARG_TARGET) ?: ""
            OtpVerificationScreen(
                modifier = Modifier.fillMaxSize(),
                targetDestination = target,
                onBackClick = { navController.popBackStack() },
                onVerifyClick = { navController.navigate(AppRoute.ResetPassword.route) },
                onResendClick = {}
            )
        }

        composable(AppRoute.ResetPassword.route) {
            ResetPasswordScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onResetSuccessClick = { navController.navigate(AppRoute.Success.route) }
            )
        }

        composable(AppRoute.Home.route) {
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                userName = loggedInUserName,
                onAddPetClick = { navController.navigate(AppRoute.AddPetOption.route) },
                onPetDetailClick = { petId -> navController.navigate(AppRoute.HealthDetail.createRoute(petId)) },
                onViewAllPetsClick = { navController.navigate(AppRoute.ListPet.route) },
                onHealthTabClick = { navController.navigateToTab(PetCareTab.HEALTH) },
                onCalendarTabClick = { navController.navigateToTab(PetCareTab.CALENDAR) },
                onMapTabClick = { navController.navigateToTab(PetCareTab.MAP) },
                onProfileTabClick = { navController.navigateToTab(PetCareTab.PROFILE) }
            )
        }

        composable(AppRoute.AddPetOption.route) {
            AddPetOptionScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onHandmadeClick = { navController.navigate(AppRoute.AddPetHandmade.createRoute()) },
                onCameraClick = { navController.navigate(AppRoute.AddPetCamera.route) }
            )
        }

        composable(AppRoute.AddPetCamera.route) {
            AddPetCameraScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onScannedProceed = { species, breed, weight ->
                    navController.navigate(
                        AppRoute.AddPetHandmade.createRoute(
                            breed = breed,
                            species = species,
                            weight = weight
                        )
                    )
                }
            )
        }

        composable(
            route = AppRoute.AddPetHandmade.route,
            arguments = listOf(
                navArgument(AppRoute.AddPetHandmade.ARG_BREED) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                },
                navArgument(AppRoute.AddPetHandmade.ARG_SPECIES) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = "Chó"
                },
                navArgument(AppRoute.AddPetHandmade.ARG_WEIGHT) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                },
            )
        ) { backStackEntry ->
            val initialBreed = backStackEntry.arguments?.getString(AppRoute.AddPetHandmade.ARG_BREED).orEmpty()
            val initialSpecies = backStackEntry.arguments?.getString(AppRoute.AddPetHandmade.ARG_SPECIES).orEmpty().ifBlank { "Chó" }
            val initialWeight = backStackEntry.arguments?.getString(AppRoute.AddPetHandmade.ARG_WEIGHT).orEmpty()

            AddPetHandmadeScreen(
                modifier = Modifier.fillMaxSize(),
                initialSpecies = initialSpecies,
                initialBreed = initialBreed,
                initialWeight = initialWeight,
                onBackClick = { navController.popBackStack() },
                onPetAdded = {
                    navController.navigate(AppRoute.ListPet.route) {
                        popUpTo(AppRoute.Home.route)
                    }
                }
            )
        }

        composable(AppRoute.ListPet.route) {
            ListPetScreen(
                modifier = Modifier.fillMaxSize(),
                currentTab = PetCareTab.HOME,
                onBackClick = { navController.popBackStack() },
                onPetClick = { pet -> navController.navigate(AppRoute.HealthDetail.createRoute(pet.id)) },
                onEditPetClick = { navController.navigate(AppRoute.SelectPetToEdit.route) },
                onAddPetClick = { navController.navigate(AppRoute.AddPetHandmade.createRoute()) },
                onTabSelected = { tab -> navController.navigateToTab(tab) }
            )
        }

        composable(AppRoute.Health.route) {
            HealthScreen(
                modifier = Modifier.fillMaxSize(),
                onPetClick = { pet -> navController.navigate(AppRoute.HealthDetail.createRoute(pet.id)) },
                onHistoryItemClick = { item ->
                    when (item.id) {
                        "h1" -> navController.navigate(AppRoute.Vaccination.createRoute("all"))
                        "h2" -> navController.navigate(AppRoute.WeightHistory.route)
                        "h3" -> navController.navigate(AppRoute.CheckupHistory.route)
                    }
                },
                onTabSelected = { tab -> navController.navigateToTab(tab) }
            )
        }

        composable(AppRoute.SelectPetToEdit.route) {
            SelectPetToEditScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onPetSelected = { petId -> navController.navigate(AppRoute.EditPet.createRoute(petId)) }
            )
        }

        composable(
            route = AppRoute.EditPet.route,
            arguments = listOf(navArgument(AppRoute.EditPet.ARG_PET_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString(AppRoute.EditPet.ARG_PET_ID) ?: ""
            EditPetScreen(
                petId = petId,
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoute.HealthDetail.route,
            arguments = listOf(navArgument(AppRoute.HealthDetail.ARG_PET_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString(AppRoute.HealthDetail.ARG_PET_ID) ?: ""
            HealthDetailScreen(
                petId = petId,
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onVaccinationClick = { id -> navController.navigate(AppRoute.Vaccination.createRoute(id)) },
                onMedicationClick = { id -> navController.navigate(AppRoute.Medication.createRoute(id)) },
                onMedicalRecordBookClick = { id -> navController.navigate(AppRoute.MedicalRecordBook.createRoute(id)) },
                onEditPetClick = { id -> navController.navigate(AppRoute.EditPet.createRoute(id)) },
                onTabSelected = { tab -> navController.navigateToTab(tab) }
            )
        }

        composable(
            route = AppRoute.Vaccination.route,
            arguments = listOf(navArgument(AppRoute.Vaccination.ARG_PET_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString(AppRoute.Vaccination.ARG_PET_ID) ?: ""
            VaccinationScreen(
                petId = petId,
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onAddVaccinationClick = { id -> navController.navigate(AppRoute.AddNewVaccination.createRoute(id)) },
                onViewInCalendarClick = { navController.navigateToTab(PetCareTab.CALENDAR) }
            )
        }

        composable(
            route = AppRoute.AddNewVaccination.route,
            arguments = listOf(navArgument(AppRoute.AddNewVaccination.ARG_PET_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString(AppRoute.AddNewVaccination.ARG_PET_ID) ?: ""
            AddNewVaccinationScreen(
                petId = petId,
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoute.Medication.route,
            arguments = listOf(navArgument(AppRoute.Medication.ARG_PET_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString(AppRoute.Medication.ARG_PET_ID) ?: ""
            MedicationScreen(
                petId = petId,
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onAddMedicationClick = { navController.navigate(AppRoute.AddNewMedication.createRoute(petId)) },
                onViewInCalendarClick = { navController.navigateToTab(PetCareTab.CALENDAR) }
            )
        }

        composable(
            route = AppRoute.AddNewMedication.route,
            arguments = listOf(navArgument(AppRoute.AddNewMedication.ARG_PET_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString(AppRoute.AddNewMedication.ARG_PET_ID) ?: ""
            AddNewMedicationScreen(
                petId = petId,
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoute.MedicalRecordBook.route,
            arguments = listOf(navArgument(AppRoute.MedicalRecordBook.ARG_PET_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString(AppRoute.MedicalRecordBook.ARG_PET_ID) ?: ""
            MedicalRecordBookScreen(
                petId = petId,
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onAddNewRecordClick = { navController.navigate(AppRoute.AddNewMedicalRecordBook.createRoute(petId)) },
                onTabSelected = { tab -> navController.navigateToTab(tab) }
            )
        }

        composable(
            route = AppRoute.AddNewMedicalRecordBook.route,
            arguments = listOf(navArgument(AppRoute.AddNewMedicalRecordBook.ARG_PET_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString(AppRoute.AddNewMedicalRecordBook.ARG_PET_ID) ?: ""
            AddNewMedicalRecordBookScreen(
                initialPetId = petId,
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(AppRoute.WeightHistory.route) {
            WeightHistoryScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppRoute.CheckupHistory.route) {
            CheckupHistoryScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Calendar.route) {
            CalendarScreen(
                modifier = Modifier.fillMaxSize(),
                onAddScheduleClick = { navController.navigate(AppRoute.AddEditSchedule.createRoute(null)) },
                onEditScheduleClick = { scheduleId -> navController.navigate(AppRoute.AddEditSchedule.createRoute(scheduleId)) },
                onTabSelected = { tab -> navController.navigateToTab(tab) }
            )
        }

        composable(
            route = AppRoute.AddEditSchedule.route,
            arguments = listOf(
                navArgument(AppRoute.AddEditSchedule.ARG_SCHEDULE_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString(AppRoute.AddEditSchedule.ARG_SCHEDULE_ID)
            AddEditScheduleScreen(
                scheduleId = scheduleId,
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Profile.route) {
            ProfileScreen(
                modifier = Modifier.fillMaxSize(),
                onUserProfileDetailClick = { navController.navigate(AppRoute.UserProfileDetail.route) },
                onPersonalInfoClick = { navController.navigate(AppRoute.PersonalInfo.route) },
                onNotificationRemindersClick = { navController.navigate(AppRoute.NotificationReminders.route) },
                onSettingsClick = { navController.navigate(AppRoute.Settings.route) },
                onHelpSupportClick = { navController.navigate(AppRoute.HelpSupport.route) },
                onMyPetsClick = { navController.navigate(AppRoute.ListPet.route) },
                onTabSelected = { tab -> navController.navigateToTab(tab) }
            )
        }

        composable(AppRoute.UserProfileDetail.route) {
            UserProfileDetailScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(AppRoute.Settings.route) },
                onPetClick = { petId -> navController.navigate(AppRoute.HealthDetail.createRoute(petId)) }
            )
        }

        composable(AppRoute.PersonalInfo.route) {
            PersonalInfoScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Settings.route) {
            val authViewModel: AuthViewModel = viewModel()
            SettingsScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onSwitchAccountClick = { navController.navigate(AppRoute.UserProfileDetail.route) },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDeleteAccountClick = {
                    authViewModel.deleteAccount(
                        onSuccess = {
                            navController.navigate(AppRoute.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            )
        }

        composable(AppRoute.HelpSupport.route) {
            HelpSupportScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() },
                onRequestSent = { navController.popBackStack() }
            )
        }

        composable(AppRoute.NotificationReminders.route) {
            NotificationReminderScreen(
                modifier = Modifier.fillMaxSize(),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Map.route) {
            MapScreen(
                modifier = Modifier.fillMaxSize(),
                onTabSelected = { tab -> navController.navigateToTab(tab) }
            )
        }

        composable(AppRoute.Success.route) {
            SuccessScreen(
                modifier = Modifier.fillMaxSize(),
                onDoneClick = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Home.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.Error.route) {
            ErrorScreen(
                modifier = Modifier.fillMaxSize(),
                onRetryClick = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
