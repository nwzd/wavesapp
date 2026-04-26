package com.olapp.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.olapp.data.model.Match
import com.olapp.data.model.ReceivedOla
import com.olapp.data.model.SentOla
import com.olapp.ui.theme.Brand
import com.olapp.ui.theme.Indigo
import com.olapp.ui.theme.LogoGradient
import com.olapp.ui.viewmodel.OlaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OlasScreen(viewModel: OlaViewModel = hiltViewModel()) {
    val receivedOlas by viewModel.receivedOlas.collectAsState()
    val sentOlas by viewModel.sentOlas.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedReceived by remember { mutableStateOf<ReceivedOla?>(null) }
    var selectedSent by remember { mutableStateOf<SentOla?>(null) }

    val headerSub = when {
        receivedOlas.isNotEmpty() ->
            if (receivedOlas.size == 1) "1 person waved at you — wave back to vibe"
            else "${receivedOlas.size} people waved at you — wave back to vibe"
        sentOlas.isNotEmpty() -> "Waiting for someone to wave back"
        else -> "Wave at someone nearby to start"
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text("Waves", style = MaterialTheme.typography.headlineMedium)
            Text(
                headerSub,
                style = MaterialTheme.typography.bodySmall,
                color = if (receivedOlas.isNotEmpty()) Brand
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = Brand,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Brand, height = 2.dp
                )
            },
            divider = {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        ) {
            listOf(
                "They waved" + if (receivedOlas.isNotEmpty()) "  ${receivedOlas.size}" else "",
                "You waved" + if (sentOlas.isNotEmpty()) "  ${sentOlas.size}" else ""
            ).forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedTab == index) Brand
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> WaveList(
                empty = receivedOlas.isEmpty(),
                emptyMessage = "No waves yet",
                emptySub = "When someone nearby waves at you, they'll show up here. Wave back to vibe — you'll both appear in Vibes and share contact info."
            ) {
                items(receivedOlas, key = { it.id }) { ola ->
                    ReceivedWaveCard(
                        ola = ola,
                        onOpen = { selectedReceived = ola },
                        onWaveBack = {
                            viewModel.respondToOla(ola.senderBleToken)
                            selectedReceived = null
                        }
                    )
                }
            }
            1 -> WaveList(
                empty = sentOlas.isEmpty(),
                emptyMessage = "No waves sent",
                emptySub = "Go to Nearby, spot someone, and tap Wave. If they wave back you'll both appear in Vibes and exchange contact info."
            ) {
                items(sentOlas, key = { it.id }) { ola ->
                    SentWaveCard(ola) { selectedSent = ola }
                }
            }
        }
    }

    selectedReceived?.let { ola ->
        WaveDetailSheet(
            name = ola.senderDisplayName.ifEmpty { "Someone nearby" },
            photoPath = ola.senderPhotoUrl,
            timestamp = ola.timestamp,
            isReceived = true,
            onRespond = {
                viewModel.respondToOla(ola.senderBleToken)
                selectedReceived = null
            },
            onDismiss = { selectedReceived = null }
        )
    }
    selectedSent?.let { ola ->
        WaveDetailSheet(
            name = ola.receiverDisplayName.ifEmpty { "Someone nearby" },
            photoPath = ola.receiverPhotoUrl,
            timestamp = ola.timestamp,
            isReceived = false,
            onRespond = {},
            onDismiss = { selectedSent = null }
        )
    }
}

@Composable
private fun WaveList(
    empty: Boolean,
    emptyMessage: String,
    emptySub: String,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    if (empty) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(72.dp).clip(CircleShape)
                        .background(Brush.linearGradient(LogoGradient))
                ) {
                    Text("〰", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }
                Text(emptyMessage, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    emptySub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
private fun ReceivedWaveCard(ola: ReceivedOla, onOpen: () -> Unit, onWaveBack: () -> Unit) {
    val fmt = remember { SimpleDateFormat("HH:mm · d MMM", Locale.getDefault()) }
    val name = ola.senderDisplayName.ifEmpty { "Someone nearby" }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(Brush.verticalGradient(listOf(Brand, Indigo)))
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileAvatar(photoPath = ola.senderPhotoUrl, name = name, size = 48.dp)

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        fmt.format(Date(ola.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                GradientButton(text = "Wave back", onClick = onWaveBack)
            }
        }
    }
}

@Composable
private fun SentWaveCard(ola: SentOla, onClick: () -> Unit) {
    val fmt = remember { SimpleDateFormat("HH:mm · d MMM", Locale.getDefault()) }
    val name = ola.receiverDisplayName.ifEmpty { "Someone nearby" }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileAvatar(photoPath = ola.receiverPhotoUrl, name = name, size = 48.dp)

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Schedule, null, Modifier.size(11.dp), Indigo)
                    Text(
                        fmt.format(Date(ola.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier.clip(RoundedCornerShape(50.dp))
                    .background(Indigo.copy(alpha = 0.10f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    "Awaiting…",
                    style = MaterialTheme.typography.labelSmall,
                    color = Indigo,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WaveDetailSheet(
    name: String,
    photoPath: String,
    timestamp: Long,
    isReceived: Boolean,
    onRespond: () -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateFmt = remember { SimpleDateFormat("EEEE, d MMMM · HH:mm", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            ProfileAvatar(photoPath = photoPath, name = name, size = 80.dp)

            Spacer(Modifier.height(16.dp))

            Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        (if (isReceived) Brand else Indigo).copy(alpha = 0.1f)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    if (isReceived) "Waved at you" else "Waiting for their wave…",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isReceived) Brand else Indigo,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Schedule, null, Modifier.size(18.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text("When", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(dateFmt.format(Date(timestamp)), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }

            if (isReceived) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Wave back and you'll both appear in Vibes — exchanging contact info.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onRespond,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand)
                ) {
                    Text("Wave back", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
        }
    }
}
