package com.olapp.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.olapp.data.model.ContactEntry
import com.olapp.data.model.ContactParser
import com.olapp.data.model.ContactPlatform
import com.olapp.ui.theme.Brand
import com.olapp.ui.theme.Tangerine
import com.olapp.ui.viewmodel.SetupViewModel

@Composable
fun SetupScreen(
    onSaved: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val isSaving by viewModel.isSaving.collectAsState()
    val saved by viewModel.saved.collectAsState()
    val error by viewModel.error.collectAsState()

    val existingName by viewModel.existingDisplayName.collectAsState()
    val existingContact by viewModel.existingContactInfo.collectAsState()
    val existingDesc by viewModel.existingDescription.collectAsState()
    val existingPhoto by viewModel.existingPhotoPath.collectAsState()
    val existingDiscoverable by viewModel.existingDiscoverable.collectAsState()

    var displayName by remember(existingName) { mutableStateOf(existingName) }
    var description by remember(existingDesc) { mutableStateOf(existingDesc) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var discoveryEnabled by remember(existingDiscoverable) { mutableStateOf(existingDiscoverable) }

    // Per-platform contact fields
    var igHandle  by remember { mutableStateOf("") }
    var waNumber  by remember { mutableStateOf("") }
    var tgHandle  by remember { mutableStateOf("") }
    var twHandle  by remember { mutableStateOf("") }
    var phNumber  by remember { mutableStateOf("") }
    var emAddress by remember { mutableStateOf("") }

    // Populate from existing stored value
    LaunchedEffect(existingContact) {
        if (existingContact.isBlank()) return@LaunchedEffect
        ContactParser.parse(existingContact).forEach { entry ->
            when (entry.platform) {
                ContactPlatform.INSTAGRAM -> igHandle  = entry.value
                ContactPlatform.WHATSAPP  -> waNumber  = entry.value
                ContactPlatform.TELEGRAM  -> tgHandle  = entry.value
                ContactPlatform.TWITTER   -> twHandle  = entry.value
                ContactPlatform.PHONE     -> phNumber  = entry.value
                ContactPlatform.EMAIL     -> emAddress = entry.value
                ContactPlatform.OTHER     -> if (igHandle.isBlank()) igHandle = entry.value
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) photoUri = uri }

    LaunchedEffect(saved) { if (saved) onSaved() }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Brand,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = Brand,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Your profile", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Contact info is only shared after a mutual wave.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(8.dp))

        // Photo picker
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 3.dp,
                        brush = Brush.linearGradient(listOf(Brand, Tangerine)),
                        shape = CircleShape
                    )
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            ) {
                when {
                    photoUri != null -> AsyncImage(
                        model = photoUri, contentDescription = "Photo",
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                    existingPhoto != null -> AsyncImage(
                        model = existingPhoto, contentDescription = "Photo",
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                    else -> Icon(
                        Icons.Default.AddAPhoto, "Add photo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp)
                    )
                }
                if (photoUri != null || existingPhoto != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brand),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, Modifier.size(16.dp), Color.White)
                    }
                }
            }
            Text(
                if (photoUri != null || existingPhoto != null) "Tap to change" else "Add your photo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display name") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 80) description = it },
            label = { Text("Short bio (optional)") },
            placeholder = { Text("e.g. Into hiking, coffee & jazz") },
            singleLine = false,
            maxLines = 2,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("${description.length}/80", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        )

        // Contact info section
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Contact info",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Shown only to people you vibe with — fill in as many as you like.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        PlatformField(
            platform = ContactPlatform.INSTAGRAM, label = "Instagram",
            placeholder = "@yourhandle",
            value = igHandle, onValueChange = { igHandle = it },
            fieldColors = fieldColors
        )
        PlatformField(
            platform = ContactPlatform.WHATSAPP, label = "WhatsApp",
            placeholder = "+351 9xx xxx xxx",
            value = waNumber, onValueChange = { waNumber = it },
            fieldColors = fieldColors
        )
        PlatformField(
            platform = ContactPlatform.TELEGRAM, label = "Telegram",
            placeholder = "@yourhandle",
            value = tgHandle, onValueChange = { tgHandle = it },
            fieldColors = fieldColors
        )
        PlatformField(
            platform = ContactPlatform.TWITTER, label = "Twitter / X",
            placeholder = "@yourhandle",
            value = twHandle, onValueChange = { twHandle = it },
            fieldColors = fieldColors
        )
        PlatformField(
            platform = ContactPlatform.PHONE, label = "Phone",
            placeholder = "+351 9xx xxx xxx",
            value = phNumber, onValueChange = { phNumber = it },
            fieldColors = fieldColors
        )
        PlatformField(
            platform = ContactPlatform.EMAIL, label = "Email (optional)",
            placeholder = "you@example.com",
            value = emAddress, onValueChange = { emAddress = it },
            fieldColors = fieldColors
        )

        // Discovery toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Discoverable", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Appear in nearby people's lists",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = discoveryEnabled,
                onCheckedChange = { discoveryEnabled = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Brand)
            )
        }

        if (error != null) {
            Text(error!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                val combinedContact = ContactParser.format(buildList {
                    if (igHandle.isNotBlank())  add(ContactEntry(ContactPlatform.INSTAGRAM, igHandle.trim()))
                    if (waNumber.isNotBlank())  add(ContactEntry(ContactPlatform.WHATSAPP,  waNumber.trim()))
                    if (tgHandle.isNotBlank())  add(ContactEntry(ContactPlatform.TELEGRAM,  tgHandle.trim()))
                    if (twHandle.isNotBlank())  add(ContactEntry(ContactPlatform.TWITTER,   twHandle.trim()))
                    if (phNumber.isNotBlank())  add(ContactEntry(ContactPlatform.PHONE,     phNumber.trim()))
                    if (emAddress.isNotBlank()) add(ContactEntry(ContactPlatform.EMAIL,     emAddress.trim()))
                })
                viewModel.save(displayName, combinedContact, description, photoUri, existingPhoto, discoveryEnabled)
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = null
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (!isSaving) Brush.linearGradient(listOf(Brand, Tangerine))
                        else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)),
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Brand, strokeWidth = 2.5.dp)
                } else {
                    Text("Save & start", style = MaterialTheme.typography.titleMedium, color = Color.White)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PlatformField(
    platform: ContactPlatform,
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    fieldColors: androidx.compose.material3.TextFieldColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PlatformIcon(platform = platform, size = 36.dp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors,
            modifier = Modifier.weight(1f)
        )
    }
}
