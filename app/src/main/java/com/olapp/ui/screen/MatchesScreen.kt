package com.olapp.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.olapp.data.model.ContactEntry
import com.olapp.data.model.ContactParser
import com.olapp.data.model.ContactPlatform
import com.olapp.data.model.Match
import com.olapp.ui.theme.Brand
import com.olapp.ui.theme.LogoGradient
import com.olapp.ui.theme.Tangerine
import com.olapp.ui.viewmodel.OlaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun MatchesScreen(viewModel: OlaViewModel = hiltViewModel()) {
    val matches by viewModel.matches.collectAsState()
    val count = matches.size

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

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

        if (matches.isEmpty()) {
            VibesEmptyState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(matches, key = { it.id }) { match ->
                    MatchCard(match = match, onDelete = { viewModel.deleteMatch(match.id, match.otherBleToken) })
                }
            }
        }
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

@Composable
private fun MatchCard(match: Match, onDelete: () -> Unit) {
    val context = LocalContext.current
    val name = match.otherDisplayName.ifEmpty { "Anonymous" }
    val lat = match.latitude
    val lon = match.longitude
    val contactEntries = remember(match.otherContactInfo) {
        ContactParser.parse(match.otherContactInfo)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left gradient accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (lat != null && lon != null || contactEntries.isNotEmpty()) 90.dp else 72.dp)
                    .background(Brush.verticalGradient(listOf(Brand, Tangerine)))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar with animated sparkle dot
                val t = rememberInfiniteTransition(label = "dot_${match.id}")
                val dotScale by t.animateFloat(
                    0.8f, 1.2f,
                    infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "dot"
                )
                Box(contentAlignment = Alignment.BottomEnd) {
                    ProfileAvatar(photoPath = match.otherPhotoUrl, name = name, size = 48.dp)
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Brand, Tangerine))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✦", style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp), color = Color.White)
                    }
                }

                // Content
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            timeAgo(match.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (lat != null && lon != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, null, Modifier.size(11.dp), Brand.copy(alpha = 0.7f))
                            Text(
                                formatCoords(lat, lon),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
                                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text("Map", style = MaterialTheme.typography.labelSmall, color = Brand)
                            }
                        }
                    }

                    // Platform-specific contact entries
                    contactEntries.forEach { entry ->
                        ContactEntryRow(entry = entry, onClick = {
                            val uri = ContactParser.intentUri(entry) ?: return@ContactEntryRow
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri))) }
                        })
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete, "Remove",
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactEntryRow(entry: ContactEntry, onClick: () -> Unit) {
    val isClickable = ContactParser.intentUri(entry) != null
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = if (isClickable) Modifier.clickable(onClick = onClick) else Modifier
    ) {
        PlatformIcon(platform = entry.platform, size = 16.dp)
        Text(
            entry.value,
            style = MaterialTheme.typography.labelSmall,
            color = if (isClickable) entry.platform.brandColor else MaterialTheme.colorScheme.onSurface
        )
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

private fun formatCoords(lat: Double, lon: Double): String {
    val latDir = if (lat >= 0) "N" else "S"
    val lonDir = if (lon >= 0) "E" else "W"
    return "%.3f°%s  %.3f°%s".format(abs(lat), latDir, abs(lon), lonDir)
}
