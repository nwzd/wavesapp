package com.olapp

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olapp.ui.theme.Brand
import com.olapp.ui.theme.Tangerine
import androidx.hilt.navigation.compose.hiltViewModel
import com.olapp.ble.BleForegroundService
import com.olapp.ui.navigation.MainNavigation
import com.olapp.ui.screen.ContactScreen
import com.olapp.ui.screen.RestrictedScreen
import com.olapp.ui.screen.SetupScreen
import com.olapp.ui.screen.TermsScreen
import com.olapp.ui.screen.TutorialScreen
import com.olapp.ui.theme.OlaTheme
import com.olapp.ui.viewmodel.MainUiState
import com.olapp.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Must be registered before STARTED — class-level property satisfies this
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        try {
            BleForegroundService.start(this)
        } catch (e: Exception) {
            Toast.makeText(this, "Couldn't start discovery service", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OlaTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()

                when (state) {
                    MainUiState.Loading -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Brand, Tangerine)))
                            ) {
                                Text("👋", fontSize = 36.sp)
                            }
                            CircularProgressIndicator(
                                color = Brand,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp
                            )
                        }
                    }

                    MainUiState.NeedsTermsAcceptance -> TermsScreen(
                        onAccept = { viewModel.setTermsAccepted() },
                        onDecline = { finishAndRemoveTask() }
                    )

                    MainUiState.NeedsSetup -> SetupScreen(
                        onSaved = { viewModel.refreshState() }
                    )

                    MainUiState.Restricted -> {
                        val showContact = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                        if (showContact.value) {
                            ContactScreen(onBack = { showContact.value = false })
                        } else {
                            RestrictedScreen(onContact = { showContact.value = true })
                        }
                    }

                    MainUiState.NeedsTutorial -> TutorialScreen(
                        onDone = { viewModel.setTutorialSeen() }
                    )

                    MainUiState.Ready -> {
                        LaunchedEffect(Unit) { launchPermissions() }
                        MainNavigation()
                    }
                }
            }
        }
    }

    private fun launchPermissions() {
        val perms = buildList {
            add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(android.Manifest.permission.BLUETOOTH_SCAN)
                add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
                add(android.Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(android.Manifest.permission.POST_NOTIFICATIONS)
                add(android.Manifest.permission.NEARBY_WIFI_DEVICES)
            }
            add(android.Manifest.permission.WAKE_LOCK)
        }
        permissionLauncher.launch(perms.toTypedArray())
    }
}
