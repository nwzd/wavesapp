package com.olapp.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import java.io.File
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.olapp.data.model.ContactEntry
import com.olapp.data.model.ContactParser
import com.olapp.data.model.Match
import com.olapp.ui.theme.Brand
import com.olapp.ui.theme.LogoGradient
import com.olapp.ui.theme.Tangerine
import com.olapp.ui.viewmodel.OlaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MatchesScreen(viewModel: OlaViewModel = hiltViewModel()) {
    val matches by viewModel.matches.collectAsState()
    val count = matches.size

    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var detailMatch by remember { mutableStateOf<Match?>(null) }

    val allSelected = matches.isNotEmpty() && selectedIds.containsAll(matches.map { it.id }.toSet())

    fun enterSelection(id: String) {
        selectionMode = true
        selectedIds = setOf(id)
    }

    fun toggleId(id: String) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    if (showDeleteConfirm) {
        val n = selectedIds.size
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove $n vibe${if (n > 1) "s" else ""}?") },
            text = { Text("Due to the peer-to-peer nature of the app, data on the recipient's end is independent of your status. Consequently, your contact info may still be stored locally on their device.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteMatches(matches.filter { it.id in selectedIds })
                    selectionMode = false
                    selectedIds = emptySet()
                }) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    detailMatch?.let { match ->
        VibeDetailSheet(
            match = match,
            onDismiss = { detailMatch = null },
            onDelete = {
                detailMatch = null
                viewModel.deleteMatches(listOf(match))
            },
            onPhotoZoomed = { viewModel.requestHdPhoto(match.otherBleToken) }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        if (selectionMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectionMode = false; selectedIds = emptySet() }) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
                Text(
                    "${selectedIds.size} selected",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = {
                    selectedIds = if (allSelected) emptySet() else matches.map { it.id }.toSet()
                }) {
                    Text(if (allSelected) "Deselect all" else "Select all")
                }
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    enabled = selectedIds.isNotEmpty()
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete selected",
                        tint = if (selectedIds.isNotEmpty()) MaterialTheme.colorScheme.error
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Vibes", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        when (count) {
                            0    -> "People on your wavelength"
                            1    -> "1 connection made ✦"
                            else -> "$count connections made ✦"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (count > 0) Brand else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (count > 0) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(LogoGradient))
                    ) {
                        Text(
                            if (count > 99) "99+" else count.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (matches.isEmpty()) {
            VibesEmptyState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(matches, key = { it.id }) { match ->
                    MatchCard(
                        match = match,
                        selectionMode = selectionMode,
                        isSelected = match.id in selectedIds,
                        onTap = {
                            if (selectionMode) toggleId(match.id)
                            else detailMatch = match
                        },
                        onLongPress = { enterSelection(match.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MatchCard(
    match: Match,
    selectionMode: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    val name = match.otherDisplayName.ifEmpty { "Anonymous" }
    val hasContacts = match.otherContactInfo.isNotBlank()
    val hasLocation = match.latitude != null && match.longitude != null

    val dotScale by rememberInfiniteTransition(label = "dot").animateFloat(
        0.8f, 1.2f,
        infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .combinedClickable(onClick = onTap, onLongClick = onLongPress)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (selectionMode) {
            Checkbox(checked = isSelected, onCheckedChange = { onTap() })
        }

        Box(contentAlignment = Alignment.BottomEnd) {
            ProfileAvatar(photoPath = match.otherPhotoUrl, name = name, size = 50.dp)
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Brand, Tangerine))),
                contentAlignment = Alignment.Center
            ) {
                Text("✦", style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.sp), color = Color.White)
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                timeAgo(match.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Subtle indicators so users know there's more to see
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasContacts) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Brand.copy(alpha = 0.5f))
                )
            }
            if (hasLocation) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun VibeDetailSheet(
    match: Match,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onPhotoZoomed: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val name = match.otherDisplayName.ifEmpty { "Anonymous" }
    val lat = match.latitude
    val lon = match.longitude
    val contactEntries = remember(match.otherContactInfo) {
        ContactParser.parse(match.otherContactInfo)
    }
    var hdPhotoPath by remember(match.otherBleToken) { mutableStateOf<String?>(null) }
    var isLoadingHd by remember(match.otherBleToken) { mutableStateOf(false) }
    LaunchedEffect(match.otherBleToken) {
        val hdFile = File(context.filesDir, "peer_photos/${match.otherBleToken}_hd.jpg")
        if (hdFile.exists()) { hdPhotoPath = hdFile.absolutePath; return@LaunchedEffect }
        isLoadingHd = true
        repeat(30) {
            if (hdFile.exists()) { hdPhotoPath = hdFile.absolutePath; isLoadingHd = false; return@LaunchedEffect }
            delay(600)
        }
        isLoadingHd = false
    }
    val photoToShow = hdPhotoPath ?: match.otherPhotoUrl
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove vibe with $name?") },
            text = { Text("Due to the peer-to-peer nature of the app, your contact info may still be stored locally on their device.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

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
                        onClick = {}
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Avatar + name + time
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        ProfileAvatar(photoPath = photoToShow, name = name, size = 100.dp, onZoomed = onPhotoZoomed)
                        if (isLoadingHd) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(30.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Vibed ${timeAgo(match.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (match.otherDescription.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                match.otherDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Contacts
                if (contactEntries.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Contact",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            contactEntries.forEach { entry ->
                                ContactChip(entry = entry, onClick = {
                                    val uri = ContactParser.intentUri(entry) ?: return@ContactChip
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri))) }
                                })
                            }
                        }
                    }
                } else {
                    Text(
                        "No contact info shared",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                // Map button
                if (lat != null && lon != null) {
                    Button(
                        onClick = {
                            val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Where we met", style = MaterialTheme.typography.labelLarge)
                    }
                }

                // Remove
                TextButton(onClick = { showDeleteConfirm = true }) {
                    Text(
                        "Remove vibe",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun ContactChip(entry: ContactEntry, onClick: () -> Unit) {
    val isClickable = ContactParser.intentUri(entry) != null
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(entry.platform.brandColor.copy(alpha = 0.10f))
            .then(if (isClickable) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        PlatformIcon(platform = entry.platform, size = 16.dp)
        Text(
            entry.value,
            style = MaterialTheme.typography.labelMedium,
            color = if (isClickable) entry.platform.brandColor else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun VibesEmptyState() {
    val t = rememberInfiniteTransition(label = "vibe_pulse")
    val scale by t.animateFloat(
        0.92f, 1.08f,
        infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 52.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(88.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(LogoGradient))
            ) {
                Text("✦", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            }
            Text(
                "No vibes yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Go to Nearby, spot someone, and tap Wave. If they wave back — you both vibe and share contact info. ✨",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun timeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000L          -> "just now"
        diff < 3_600_000L       -> "${diff / 60_000}m ago"
        diff < 86_400_000L      -> "${diff / 3_600_000}h ago"
        diff < 7 * 86_400_000L  -> "${diff / 86_400_000}d ago"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
    }
}
