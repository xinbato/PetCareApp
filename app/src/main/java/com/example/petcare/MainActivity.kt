package com.example.petcare

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.petcare.data.model.ThemeMode
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.firebase.configureAppCheck
import com.example.petcare.ui.navigation.AppNavHost
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.viewmodel.SettingsViewModel
import com.example.petcare.utils.NotificationHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.crashlytics

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)
        configureAppCheck(this, intent)

        NotificationHelper.createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settings by settingsViewModel.settings.collectAsState()
            var currentUser by remember {
                mutableStateOf<FirebaseUser?>(runCatching { FirebaseAuth.getInstance().currentUser }.getOrNull())
            }
            DisposableEffect(Unit) {
                val auth = runCatching { FirebaseAuth.getInstance() }.getOrNull()
                val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    currentUser = firebaseAuth.currentUser
                }
                auth?.addAuthStateListener(listener)
                onDispose {
                    auth?.removeAuthStateListener(listener)
                }
            }
            val vaccinations by PetRepository.vaccinations.collectAsState()
            val medications by PetRepository.medications.collectAsState()
            val schedules by PetRepository.schedules.collectAsState()

            LaunchedEffect(currentUser, vaccinations, medications, schedules, settings) {
                if (currentUser != null) {
                    NotificationHelper.syncReminders(
                        context = this@MainActivity,
                        vaccinations = vaccinations,
                        medications = medications,
                        schedules = schedules,
                        settings = settings,
                    )
                } else {
                    NotificationHelper.clearAllReminders(this@MainActivity)
                }
            }

            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            PetCareTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
