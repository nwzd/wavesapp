package com.olapp.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import coil.compose.AsyncImage
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import com.olapp.data.model.ContactEntry
import com.olapp.data.model.ContactParser
import com.olapp.data.model.ContactPlatform
import com.olapp.ui.theme.Brand
import com.olapp.ui.theme.Tangerine
import com.olapp.ui.viewmodel.SetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
private class SelfieContract : ActivityResultContract<Uri, Boolean>() {
    override fun createIntent(context: Context, input: Uri): Intent =
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, input)
            putExtra("android.intent.extras.CAMERA_FACING", 1)
            putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
    override fun parseResult(resultCode: Int, intent: Intent?) =
        resultCode == android.app.Activity.RESULT_OK
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onSaved: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val isSaving by viewModel.isSaving.collectAsState()
    val saved by viewModel.saved.collectAsState()
    val error by viewModel.error.collectAsState()
    val photoError by viewModel.photoError.collectAsState()
    val photoValidating by viewModel.photoValidating.collectAsState()
    val validatedPhotoUri by viewModel.validatedPhotoUri.collectAsState()
    val photoIsSelfie by viewModel.photoIsSelfie.collectAsState()
    val isFirstSetup by viewModel.isFirstSetup.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDobMillis by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                utcTimeMillis <= System.currentTimeMillis()
        }
    )

    val existingName by viewModel.existingDisplayName.collectAsState()
    val existingContact by viewModel.existingContactInfo.collectAsState()
    val existingDesc by viewModel.existingDescription.collectAsState()
    val existingPhoto by viewModel.existingPhotoPath.collectAsState()
    val existingDiscoverable by viewModel.existingDiscoverable.collectAsState()

    var displayName by remember(existingName) { mutableStateOf(existingName) }
    var description by remember(existingDesc) { mutableStateOf(existingDesc) }
    var discoveryEnabled by remember(existingDiscoverable) { mutableStateOf(existingDiscoverable) }

    var igHandle  by remember { mutableStateOf("") }
    var ttHandle  by remember { mutableStateOf("") }
    var scHandle  by remember { mutableStateOf("") }
    var fbHandle  by remember { mutableStateOf("") }
    var twHandle  by remember { mutableStateOf("") }
    var liHandle  by remember { mutableStateOf("") }
    var dcHandle  by remember { mutableStateOf("") }
    var emAddress by remember { mutableStateOf("") }

    LaunchedEffect(existingContact) {
        if (existingContact.isBlank()) return@LaunchedEffect
        ContactParser.parse(existingContact).forEach { entry ->
            when (entry.platform) {
                ContactPlatform.INSTAGRAM -> igHandle  = entry.value
                ContactPlatform.TIKTOK    -> ttHandle  = entry.value
                ContactPlatform.SNAPCHAT  -> scHandle  = entry.value
                ContactPlatform.FACEBOOK  -> fbHandle  = entry.value
                ContactPlatform.TWITTER   -> twHandle  = entry.value
                ContactPlatform.LINKEDIN  -> liHandle  = entry.value
                ContactPlatform.DISCORD   -> dcHandle  = entry.value
                ContactPlatform.EMAIL     -> emAddress = entry.value
                ContactPlatform.OTHER     -> if (igHandle.isBlank()) igHandle = entry.value
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onPhotoSelected(it, isSelfie = false) }
    }

    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    val selfieLauncher = rememberLauncherForActivityResult(SelfieContract()) { success ->
        if (success) selfieUri?.let { viewModel.onPhotoSelected(it, isSelfie = true) }
    }

    LaunchedEffect(saved) { if (saved) onSaved() }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDobMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

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
            val displayUri = validatedPhotoUri ?: existingPhoto?.let { Uri.parse(it) }
            val hasPhoto = displayUri != null

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(listOf(Brand, Tangerine)),
                            shape = CircleShape
                        )
                ) {
                    when {
                        photoValidating -> CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Brand,
                            strokeWidth = 2.5.dp
                        )
                        displayUri != null -> AsyncImage(
                            model = displayUri,
                            contentDescription = "Profile photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        else -> Icon(
                            Icons.Default.AddAPhoto, "Add photo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                // Selfie badge — shown when the validated photo was taken with the camera
                if (!photoValidating && hasPhoto && photoIsSelfie) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, "Selfie", Modifier.size(16.dp), Color.White)
                    }
                }
            }

            if (photoError != null) {
                Text(
                    photoError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Two pick options
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !photoValidating
                ) {
                    Text(
                        if (hasPhoto && !photoIsSelfie) "🖼 Change photo" else "🖼 Gallery",
                        style = MaterialTheme.typography.labelMedium,
                        color = Brand
                    )
                }
                Text("·", color = MaterialTheme.colorScheme.outlineVariant)
                TextButton(
                    onClick = {
                        val uri = viewModel.createTempPhotoUri()
                        selfieUri = uri
                        selfieLauncher.launch(uri)
                    },
                    enabled = !photoValidating
                ) {
                    Text(
                        if (hasPhoto && photoIsSelfie) "📷 Retake selfie" else "📷 Take selfie",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (photoIsSelfie) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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

        if (isFirstSetup) {
            val dobLabel = selectedDobMillis?.let { millis ->
                Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    .format(DateTimeFormatter.ofPattern("dd / MM / yyyy"))
            } ?: ""

            val dobInteraction = remember { MutableInteractionSource() }
            LaunchedEffect(dobInteraction) {
                dobInteraction.interactions.collect { interaction ->
                    if (interaction is PressInteraction.Press) showDatePicker = true
                }
            }

            OutlinedTextField(
                value = dobLabel,
                onValueChange = {},
                label = { Text("Date of birth (18+ only)") },
                placeholder = { Text("dd / mm / yyyy", style = MaterialTheme.typography.bodySmall) },
                readOnly = true,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth(),
                interactionSource = dobInteraction,
                trailingIcon = {
                    Icon(Icons.Default.DateRange, null, Modifier.size(18.dp),
                        MaterialTheme.colorScheme.onSurfaceVariant)
                }
            )
        }

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

        // Phone-number advisory
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("💡", style = MaterialTheme.typography.bodyMedium)
            Text(
                "We recommend using social handles rather than a phone number. Because Wave & Vibe is peer-to-peer — no server involved — once your contact info is shared after a vibe, it cannot be recalled. A username is easier to manage if you ever change your mind.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        PlatformField(
            platform = ContactPlatform.INSTAGRAM, label = "Instagram",
            placeholder = "@yourhandle",
            value = igHandle, onValueChange = { igHandle = it },
            fieldColors = fieldColors
        )
        PlatformField(
            platform = ContactPlatform.TIKTOK, label = "TikTok",
            placeholder = "@yourhandle",
            value = ttHandle, onValueChange = { ttHandle = it },
            fieldColors = fieldColors
        )
        PlatformField(
            platform = ContactPlatform.SNAPCHAT, label = "Snapchat",
            placeholder = "yourname",
            value = scHandle, onValueChange = { scHandle = it },
            fieldColors = fieldColors
        )
        PlatformField(
            platform = ContactPlatform.FACEBOOK, label = "Facebook",
            placeholder = "your.name",
            value = fbHandle, onValueChange = { fbHandle = it },
            fieldColors = fieldColors
        )
        PlatformField(
            platform = ContactPlatform.TWITTER, label = "Twitter / X",
            placeholder = "@yourhandle",
            value = twHandle, onValueChange = { twHandle = it },
            fieldColors = fieldColors
        )
        PlatformField(
            platform = ContactPlatform.LINKEDIN, label = "LinkedIn",
            placeholder = "yourname",
            value = liHandle, onValueChange = { liHandle = it },
            fieldColors = fieldColors
        )
        PlatformField(
            platform = ContactPlatform.DISCORD, label = "Discord",
            placeholder = "username",
            value = dcHandle, onValueChange = { dcHandle = it },
            fieldColors = fieldColors
        )
        PlatformField(
            platform = ContactPlatform.EMAIL, label = "Email",
            placeholder = "you@example.com",
            value = emAddress, onValueChange = { emAddress = it },
            fieldColors = fieldColors
        )

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
                    if (ttHandle.isNotBlank())  add(ContactEntry(ContactPlatform.TIKTOK,    ttHandle.trim()))
                    if (scHandle.isNotBlank())  add(ContactEntry(ContactPlatform.SNAPCHAT,  scHandle.trim()))
                    if (fbHandle.isNotBlank())  add(ContactEntry(ContactPlatform.FACEBOOK,  fbHandle.trim()))
                    if (twHandle.isNotBlank())  add(ContactEntry(ContactPlatform.TWITTER,   twHandle.trim()))
                    if (liHandle.isNotBlank())  add(ContactEntry(ContactPlatform.LINKEDIN,  liHandle.trim()))
                    if (dcHandle.isNotBlank())  add(ContactEntry(ContactPlatform.DISCORD,   dcHandle.trim()))
                    if (emAddress.isNotBlank()) add(ContactEntry(ContactPlatform.EMAIL,     emAddress.trim()))
                })
                viewModel.save(displayName, combinedContact, description, discoveryEnabled, selectedDobMillis)
            },
            enabled = !isSaving && !photoValidating,
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
