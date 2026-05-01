package com.olapp.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olapp.ui.theme.Brand
import com.olapp.ui.theme.LogoGradient
import com.olapp.ui.theme.Tangerine

private data class TutorialPage(val emoji: String, val title: String, val body: String)

private val tutorialPages = listOf(
    TutorialPage(
        emoji = "👋",
        title = "Welcome to Wave & Vibe",
        body = "Meet people physically around you — no endless swiping, no algorithms. Just real connections with people in the same place."
    ),
    TutorialPage(
        emoji = "📡",
        title = "Discover nearby people",
        body = "Wave & Vibe scans using Bluetooth. Anyone with the app open nearby appears on your screen. Keep Bluetooth and Wi-Fi on — no internet needed, just the radios."
    ),
    TutorialPage(
        emoji = "✦",
        title = "Wave, vibe, connect",
        body = "Spot someone? Tap Wave. If they wave back — it's a Vibe! You'll both see each other's contact info: Instagram, Twitter/X, email, and more."
    ),
    TutorialPage(
        emoji = "🔒",
        title = "Your data, your phone",
        body = "No servers. Your profile lives only on your device. Uninstalling the app removes everything — nothing to request, nothing to breach."
    )
)

@Composable
fun TutorialScreen(onDone: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val isLast = currentPage == tutorialPages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.5f))

        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
                } else {
                    slideInHorizontally { -it } + fadeIn() togetherWith
                        slideOutHorizontally { it } + fadeOut()
                }
            },
            label = "tutorial_page"
        ) { page ->
            val p = tutorialPages[page]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(LogoGradient))
                ) {
                    Text(p.emoji, fontSize = 42.sp)
                }
                Text(
                    p.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    p.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(Modifier.weight(0.5f))

        // Page dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tutorialPages.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .size(
                            width = if (i == currentPage) 24.dp else 6.dp,
                            height = 6.dp
                        )
                        .clip(CircleShape)
                        .background(
                            if (i == currentPage) Brand
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { if (isLast) onDone() else currentPage++ },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = null
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(Brand, Tangerine)), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isLast) "Let's wave 👋" else "Next",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }

        if (!isLast) {
            TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Skip",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Spacer(Modifier.height(40.dp))
        }

        Spacer(Modifier.height(24.dp))
    }
}
