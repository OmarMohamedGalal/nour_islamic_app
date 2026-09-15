package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.receiver.AdhanAlarmScheduler
import com.example.receiver.NotificationHelper
import com.example.ui.NoorDestination
import com.example.ui.NoorUiState
import com.example.ui.NoorViewModel
import com.example.ui.components.NoorBottomNavBar
import com.example.ui.screens.*
import com.example.ui.theme.NoorTheme
import com.example.ui.theme.VoidBlack
import com.example.widget.WidgetUpdater

class MainActivity : ComponentActivity() {

    private val viewModel: NoorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Notification channels & alarm scheduler
        NotificationHelper.createNotificationChannels(this)
        AdhanAlarmScheduler.scheduleNextPrayers(this)
        WidgetUpdater.updateAllWidgets(this)

        handleDeepLink(intent)

        setContent {
            NoorTheme {
                RequestPermissionsEffect()

                val uiState by viewModel.uiState.collectAsState()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VoidBlack),
                    containerColor = VoidBlack,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        NoorBottomNavBar(
                            currentDestination = uiState.currentDestination,
                            onDestinationSelected = { viewModel.setDestination(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = uiState.currentDestination,
                            transitionSpec = {
                                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                                        fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                            },
                            label = "screenTransition"
                        ) { destination ->
                            when (destination) {
                                NoorDestination.PRAYER -> PrayerScreen(viewModel, uiState)
                                NoorDestination.QURAN -> QuranScreen(viewModel, uiState)
                                NoorDestination.AZKAR -> AzkarScreen(viewModel, uiState)
                                NoorDestination.DUAA -> DuaaScreen(viewModel, uiState)
                                NoorDestination.QIBLA -> QiblaScreen(viewModel, uiState)
                                NoorDestination.CALENDAR -> CalendarScreen(viewModel, uiState)
                                NoorDestination.SETTINGS -> SettingsScreen(viewModel, uiState)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null) {
            when (data.host) {
                "prayer" -> viewModel.setDestination(NoorDestination.PRAYER)
                "quran" -> viewModel.setDestination(NoorDestination.QURAN)
                "azkar" -> viewModel.setDestination(NoorDestination.AZKAR)
                "duaa" -> viewModel.setDestination(NoorDestination.DUAA)
                "qibla" -> viewModel.setDestination(NoorDestination.QIBLA)
                "calendar", "ramadan" -> viewModel.setDestination(NoorDestination.CALENDAR)
                "settings" -> viewModel.setDestination(NoorDestination.SETTINGS)
            }
        }
    }

    @Composable
    private fun RequestPermissionsEffect() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) {
            // Permissions granted / updated
            AdhanAlarmScheduler.scheduleNextPrayers(this@MainActivity)
        }

        LaunchedEffect(Unit) {
            if (permissionsToRequest.isNotEmpty()) {
                launcher.launch(permissionsToRequest.toTypedArray())
            }
        }
    }
}
