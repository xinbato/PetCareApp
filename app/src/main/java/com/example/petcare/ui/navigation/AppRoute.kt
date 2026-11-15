package com.example.petcare.ui.navigation

sealed class AppRoute(val route: String) {
    data object Onboarding : AppRoute("onboarding")
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object ForgotPassword : AppRoute("forgot_password")
    data object OtpVerification : AppRoute("otp_verification/{target}") {
        const val ARG_TARGET = "target"
        fun createRoute(target: String): String = "otp_verification/$target"
    }
    data object ResetPassword : AppRoute("reset_password")
    data object Success : AppRoute("success")
    data object Error : AppRoute("error")
    data object Home : AppRoute("home")
    data object AddPetOption : AppRoute("add_pet_option")
    data object AddPetCamera : AppRoute("add_pet_camera")
    data object AddPetHandmade : AppRoute("add_pet_handmade")
    data object ListPet : AppRoute("list_pet")
    data object Health : AppRoute("health")
    data object SelectPetToEdit : AppRoute("select_pet_to_edit")
    data object EditPet : AppRoute("edit_pet/{petId}") {
        const val ARG_PET_ID = "petId"
        fun createRoute(petId: String): String = "edit_pet/$petId"
    }
    data object HealthDetail : AppRoute("health_detail/{petId}") {
        const val ARG_PET_ID = "petId"
        fun createRoute(petId: String): String = "health_detail/$petId"
    }
    data object Vaccination : AppRoute("vaccination/{petId}") {
        const val ARG_PET_ID = "petId"
        fun createRoute(petId: String): String = "vaccination/$petId"
    }
    data object AddNewVaccination : AppRoute("add_new_vaccination/{petId}") {
        const val ARG_PET_ID = "petId"
        fun createRoute(petId: String): String = "add_new_vaccination/$petId"
    }
    data object Medication : AppRoute("medication/{petId}") {
        const val ARG_PET_ID = "petId"
        fun createRoute(petId: String): String = "medication/$petId"
    }
    data object AddNewMedication : AppRoute("add_new_medication/{petId}") {
        const val ARG_PET_ID = "petId"
        fun createRoute(petId: String): String = "add_new_medication/$petId"
    }
    data object MedicalRecordBook : AppRoute("medical_record_book/{petId}") {
        const val ARG_PET_ID = "petId"
        fun createRoute(petId: String): String = "medical_record_book/$petId"
    }
    data object AddNewMedicalRecordBook : AppRoute("add_new_medical_record_book/{petId}") {
        const val ARG_PET_ID = "petId"
        fun createRoute(petId: String): String = "add_new_medical_record_book/$petId"
    }
    data object WeightHistory : AppRoute("weight_history")
    data object CheckupHistory : AppRoute("checkup_history")
    data object Calendar : AppRoute("calendar")
    data object AddEditSchedule : AppRoute("add_edit_schedule?scheduleId={scheduleId}") {
        const val ARG_SCHEDULE_ID = "scheduleId"
        fun createRoute(scheduleId: String? = null): String =
            if (!scheduleId.isNullOrEmpty()) "add_edit_schedule?scheduleId=$scheduleId" else "add_edit_schedule"
    }
    data object Profile : AppRoute("profile")
    data object UserProfileDetail : AppRoute("user_profile_detail")
    data object PersonalInfo : AppRoute("personal_info")
    data object Settings : AppRoute("settings")
    data object HelpSupport : AppRoute("help_support")
    data object NotificationReminders : AppRoute("notification_reminders")
    data object Map : AppRoute("map")
}

/**
 * Extension mapping from legacy AppScreen enum to modern AppRoute string.
 */
fun AppScreen.toRoute(
    petId: String = "1",
    target: String = "",
    scheduleId: String? = null,
): String = when (this) {
    AppScreen.ONBOARDING -> AppRoute.Onboarding.route
    AppScreen.LOGIN -> AppRoute.Login.route
    AppScreen.REGISTER -> AppRoute.Register.route
    AppScreen.FORGOT_PASSWORD -> AppRoute.ForgotPassword.route
    AppScreen.OTP_VERIFICATION -> AppRoute.OtpVerification.createRoute(target)
    AppScreen.RESET_PASSWORD -> AppRoute.ResetPassword.route
    AppScreen.SUCCESS -> AppRoute.Success.route
    AppScreen.ERROR -> AppRoute.Error.route
    AppScreen.HOME -> AppRoute.Home.route
    AppScreen.ADD_PET_OPTION -> AppRoute.AddPetOption.route
    AppScreen.ADD_PET_CAMERA -> AppRoute.AddPetCamera.route
    AppScreen.ADD_PET_HANDMADE -> AppRoute.AddPetHandmade.route
    AppScreen.LIST_PET -> AppRoute.ListPet.route
    AppScreen.HEALTH -> AppRoute.Health.route
    AppScreen.SELECT_PET_TO_EDIT -> AppRoute.SelectPetToEdit.route
    AppScreen.EDIT_PET -> AppRoute.EditPet.createRoute(petId)
    AppScreen.HEALTH_DETAIL -> AppRoute.HealthDetail.createRoute(petId)
    AppScreen.VACCINATION -> AppRoute.Vaccination.createRoute(petId)
    AppScreen.ADD_NEW_VACCINATION -> AppRoute.AddNewVaccination.createRoute(petId)
    AppScreen.MEDICATION -> AppRoute.Medication.createRoute(petId)
    AppScreen.ADD_NEW_MEDICATION -> AppRoute.AddNewMedication.createRoute(petId)
    AppScreen.MEDICATION_RECORD_BOOK -> AppRoute.MedicalRecordBook.createRoute(petId)
    AppScreen.ADD_NEW_MEDICATION_RECORD_BOOK -> AppRoute.AddNewMedicalRecordBook.createRoute(petId)
    AppScreen.WEIGHT_HISTORY -> AppRoute.WeightHistory.route
    AppScreen.CHECKUP_HISTORY -> AppRoute.CheckupHistory.route
    AppScreen.CALENDAR -> AppRoute.Calendar.route
    AppScreen.ADD_EDIT_SCHEDULE -> AppRoute.AddEditSchedule.createRoute(scheduleId)
    AppScreen.PROFILE -> AppRoute.Profile.route
    AppScreen.USER_PROFILE_DETAIL -> AppRoute.UserProfileDetail.route
    AppScreen.PERSONAL_INFO -> AppRoute.PersonalInfo.route
    AppScreen.SETTINGS -> AppRoute.Settings.route
    AppScreen.HELP_SUPPORT -> AppRoute.HelpSupport.route
    AppScreen.NOTIFICATION_REMINDERS -> AppRoute.NotificationReminders.route
    AppScreen.MAP -> AppRoute.Map.route
}
