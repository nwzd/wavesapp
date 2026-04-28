package com.olapp.ui.screen

import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.olapp.ui.theme.Brand
import com.olapp.ui.theme.Indigo
import com.olapp.ui.theme.LogoGradient
import com.olapp.ui.theme.Tangerine
import com.olapp.ui.theme.avatarGradient
import com.olapp.ui.viewmodel.DiscoveryViewModel
import com.olapp.ui.viewmodel.NearbyDevice
import com.olapp.ui.viewmodel.WaveStatus
import java.io.File

@Composable
fun DiscoveryScreen(viewModel: DiscoveryViewModel = hiltViewModel()) {
    val nearbyDevices by viewModel.nearbyDevices.collectAsState()
    val discoveryEnabled by viewModel.discoveryEnabled.collectAsState()
    val count = nearbyDevices.size
    val context = LocalContext.current

    val prefs = remember { context.getSharedPreferences("olapp_ui", Context.MODE_PRIVATE) }
    var showHowItWorks by remember { mutableStateOf(!prefs.getBoolean("hint_dismissed", false)) }

    var bluetoothEnabled by remember {
        mutableStateOf(
            runCatching {
                (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter?.isEnabled == true
            }.getOrDefault(true)
        )
    }
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val state = intent.getIntExtra(android.bluetooth.BluetoothAdapter.EXTRA_STATE, android.bluetooth.BluetoothAdapter.ERROR)
                bluetoothEnabled = state == android.bluetooth.BluetoothAdapter.STATE_ON
            }
        }
        context.registerReceiver(receiver, IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED))
        onDispose { context.unregisterReceiver(receiver) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    if (count == 0) "Nearby" else "$count nearby",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    if (discoveryEnabled) "You are visible" else "Discovery off",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (discoveryEnabled) Brand else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = discoveryEnabled,
                onCheckedChange = { viewModel.toggleDiscovery(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Brand
                )
            )
        }

        if (!bluetoothEnabled) {
            BluetoothOffBanner(onEnable = {
                context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            })
        }

        if (nearbyDevices.isNotEmpty() && showHowItWorks) {
            HowItWorksHint(onDismiss = {
                prefs.edit().putBoolean("hint_dismissed", true).apply()
                showHowItWorks = false
            })
        }

        if (nearbyDevices.isEmpty()) {
            RadarEmptyState(discoveryEnabled)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(nearbyDevices, key = { it.bleToken }) { device ->
                    NearbyDeviceCard(
                        device = device,
                        onSendOla = { viewModel.sendOla(device.bleToken) },
                        onPhotoZoomed = { viewModel.requestHdPhoto(device.bleToken) },
                        onBlock = { viewModel.blockUser(device.bleToken) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BluetoothOffBanner(onEnable: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.18f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            Icons.Default.WifiTethering,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            "Bluetooth off — discovery limited to this WiFi network",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        TextButton(
            onClick = onEnable,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Text("Enable", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun HowItWorksHint(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brand.copy(alpha = 0.08f))
            .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("〰", style = MaterialTheme.typography.bodyMedium, color = Brand)
        Text(
            "Tap Wave → they wave back → you both Vibe and exchange contact info",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Dismiss",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RadarEmptyState(discoveryEnabled: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (discoveryEnabled) {
            val t = rememberInfiniteTransition(label = "radar")
            val s1 by t.animateFloat(0.65f, 1.35f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Restart), label = "s1")
            val s2 by t.animateFloat(0.65f, 1.35f, infiniteRepeatable(tween(2200, 550, FastOutSlowInEasing), RepeatMode.Restart), label = "s2")
            val s3 by t.animateFloat(0.65f, 1.35f, infiniteRepeatable(tween(2200, 1100, FastOutSlowInEasing), RepeatMode.Restart), label = "s3")
            Box(Modifier.size(220.dp).scale(s3).clip(CircleShape).background(Brand.copy(alpha = 0.04f)))
            Box(Modifier.size(148.dp).scale(s2).clip(CircleShape).background(Brand.copy(alpha = 0.07f)))
            Box(Modifier.size(88.dp).scale(s1).clip(CircleShape).background(Brand.copy(alpha = 0.12f)))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 52.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(60.dp).clip(CircleShape).background(
                    if (discoveryEnabled) Brush.linearGradient(LogoGradient)
                    else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                )
            ) {
                Icon(
                    Icons.Default.WifiTethering, null, Modifier.size(28.dp),
                    if (discoveryEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                if (discoveryEnabled) "Looking around…" else "Discovery off",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (discoveryEnabled)
                    "Scanning nearby… people appear as they're found. Same WiFi is instant; Bluetooth reaches further."
                else
                    "Turn on discovery to see people around you.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NearbyDeviceCard(
    device: NearbyDevice,
    onSendOla: () -> Unit,
    onPhotoZoomed: () -> Unit,
    onBlock: () -> Unit
) {
    var showBlockDialog by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }

    if (showBlockDialog) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text("Block ${device.displayName}?") },
            text = { Text("They won't be able to see you nearby and you won't see them.") },
            confirmButton = {
                TextButton(onClick = { showBlockDialog = false; onBlock() }) {
                    Text("Block", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showProfile) {
        NearbyProfileDialog(
            device = device,
            onDismiss = { showProfile = false },
            onPhotoZoomed = onPhotoZoomed,
            onWave = { showProfile = false; onSendOla() },
            onBlock = { showProfile = false; showBlockDialog = true }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !device.isPending,
                onClick = { showProfile = true }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PeerAvatar(device = device, size = 50.dp, onZoomed = onPhotoZoomed)

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    device.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    when {
                        device.isPending                      -> "Connecting…"
                        device.status == WaveStatus.WAVE_SENT -> "Waiting for their wave…"
                        device.status == WaveStatus.MATCHED   -> "You're vibing"
                        device.description.isNotBlank()       -> device.description
                        else                                  -> "Tap to see profile"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (device.status) {
                        WaveStatus.MATCHED   -> Brand
                        WaveStatus.WAVE_SENT -> Indigo
                        else                 -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1
                )
            }

            if (device.isPending) {
                StatusChip(text = "•••", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            } else when (device.status) {
                WaveStatus.NONE -> Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showBlockDialog = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = "Block",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    GradientButton(text = "Wave", onClick = onSendOla)
                }
                WaveStatus.WAVE_SENT -> StatusChip(text = "Waved ✓", tint = Indigo)
                WaveStatus.MATCHED   -> StatusChip(text = "Vibing", tint = Brand)
            }
        }
    }
}

@Composable
private fun NearbyProfileDialog(
    device: NearbyDevice,
    onDismiss: () -> Unit,
    onPhotoZoomed: () -> Unit,
    onWave: () -> Unit,
    onBlock: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}  // absorb touches so they don't dismiss
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                PeerAvatar(device = device, size = 88.dp, onZoomed = onPhotoZoomed)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        device.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (device.description.isNotBlank()) {
                        Text(
                            device.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Wave button
                if (device.status == WaveStatus.NONE) {
                    GradientButton(
                        text = "👋  Wave",
                        onClick = onWave,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    StatusChip(
                        text = if (device.status == WaveStatus.WAVE_SENT) "Waved ✓" else "Vibing",
                        tint = if (device.status == WaveStatus.WAVE_SENT) Indigo else Brand
                    )
                }

                TextButton(onClick = onBlock) {
                    Text(
                        "Block ${device.displayName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PeerAvatar(device: NearbyDevice, size: Dp, onZoomed: (() -> Unit)? = null) {
    ProfileAvatar(
        photoPath = device.photoPath,
        name = device.displayName,
        size = size,
        photoIsSelfie = device.photoIsSelfie,
        onZoomed = onZoomed
    )
}

@Composable
fun ProfileAvatar(
    photoPath: String?,
    name: String,
    size: Dp,
    modifier: Modifier = Modifier,
    photoIsSelfie: Boolean = false,
    onZoomed: (() -> Unit)? = null
) {
    val photoFile = photoPath?.takeIf { it.isNotBlank() }?.let { File(it) }
    var showZoom by remember { mutableStateOf(false) }

    Box(modifier = modifier.size(size)) {
        if (photoFile != null && photoFile.exists()) {
            AsyncImage(
                model = photoFile,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape).clickable { showZoom = true }
            )
            if (showZoom) {
                ZoomablePhotoDialog(
                    photoFile = photoFile,
                    name = name,
                    onDismiss = { showZoom = false },
                    onOpened = onZoomed
                )
            }
        } else {
            InitialsAvatar(name = name, size = size)
        }

        if (photoIsSelfie && photoFile?.exists() == true) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(size * 0.34f)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Selfie",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.20f)
                )
            }
        }
    }
}

@Composable
private fun ZoomablePhotoDialog(
    photoFile: File,
    name: String,
    onDismiss: () -> Unit,
    onOpened: (() -> Unit)? = null
) {
    LaunchedEffect(Unit) { onOpened?.invoke() }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 6f)
        offset = if (scale > 1f) offset + pan else Offset.Zero
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.93f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { if (scale <= 1f) onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photoFile,
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)
                    .transformable(state = transformState)
            )
        }
    }
}

@Composable
private fun StatusChip(text: String, tint: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(tint.copy(alpha = 0.10f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = tint, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun GradientButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Brand, Tangerine)), RoundedCornerShape(50.dp))
                .padding(horizontal = 18.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}

@Composable
fun InitialsAvatar(name: String, size: Dp, modifier: Modifier = Modifier) {
    val initial = (name.firstOrNull()?.uppercaseChar() ?: '?').toString()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size).clip(CircleShape)
            .background(Brush.linearGradient(avatarGradient(name)))
    ) {
        Text(
            initial,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
