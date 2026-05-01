package com.olapp.ui.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olapp.ui.theme.Brand
import com.olapp.ui.theme.Indigo
import com.olapp.ui.theme.LogoGradient
import com.olapp.ui.theme.Tangerine

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                "About Wave & Vibe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // Logo + tagline
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(LogoGradient))
                ) {
                    Text("👋", fontSize = 38.sp)
                }
                Text(
                    "Wave & Vibe",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "Make friends around you",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Mission
            AboutCard {
                GradientLabel("Our mission")
                Spacer(Modifier.height(10.dp))
                Text(
                    "Making friends should be free, private and fun.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(12.dp))
                AboutBody(
                    "We built Wave & Vibe because meeting people in real life shouldn't require a monthly subscription, an algorithm deciding who you deserve to see, or a company storing details about your social life on their servers."
                )
                Spacer(Modifier.height(10.dp))
                AboutBody(
                    "The best connections happen in person — at a coffee shop, a concert, a park. Wave & Vibe simply removes the awkwardness of that first move. You see someone nearby, you send a wave. If they wave back, you both know it's mutual. That's it. No swiping, no matches with strangers you'll never meet, no ghost profiles collected for advertising."
                )
                Spacer(Modifier.height(10.dp))
                AboutBody(
                    "We believe this is how meeting people should feel: spontaneous, mutual, and free."
                )
            }

            Spacer(Modifier.height(16.dp))

            // How it works
            AboutCard {
                GradientLabel("How it works")
                Spacer(Modifier.height(16.dp))

                HowItWorksStep(
                    emoji = "📡",
                    title = "Your phone becomes a beacon",
                    body = "When you open Wave & Vibe, your phone broadcasts a completely anonymous signal over Bluetooth — no name, no ID, nothing identifiable. People nearby running Wave & Vibe receive the signal and appear in each other's Nearby list. Both Bluetooth and Wi-Fi must be on, but no internet connection is needed."
                )
                AboutDivider()
                HowItWorksStep(
                    emoji = "👋",
                    title = "You wave at someone",
                    body = "See someone interesting? Tap Wave. They receive a notification that someone nearby waved at them — but they don't see your name or photo yet. Your identity is only revealed after the wave is mutual."
                )
                AboutDivider()
                HowItWorksStep(
                    emoji = "✦",
                    title = "They wave back — it's a Vibe",
                    body = "If they wave back, both of you are notified instantly. You both see each other's full profile — name, photo, bio, and the contact details you chose to share (Instagram, Twitter/X, email…). From that point on, the connection is yours — Wave & Vibe steps out of the way."
                )
                AboutDivider()
                HowItWorksStep(
                    emoji = "🔄",
                    title = "No wave back? You'll never know",
                    body = "If the other person doesn't wave back, you won't know whether they saw your wave — there's no \"seen\" receipt on your end. Unresponded waves expire quietly from your sent history."
                )
            }

            Spacer(Modifier.height(16.dp))

            // Privacy
            AboutCard(dark = true) {
                GradientLabel("Our data promise", light = true)
                Spacer(Modifier.height(10.dp))
                Text(
                    "We never save your data.\nWe literally cannot.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(14.dp))

                PrivacyPill(
                    emoji = "📱",
                    title = "Everything stays on your device",
                    body = "Your profile, your matches, your waves — all stored locally on your phone. There is no Wave & Vibe server. There is no database with your name in it. We have no backend that could be hacked or subpoenaed."
                )
                Spacer(Modifier.height(10.dp))
                PrivacyPill(
                    emoji = "🔒",
                    title = "Direct phone-to-phone",
                    body = "When two phones connect, profiles are exchanged directly between them over Bluetooth and Wi-Fi Direct — like AirDrop. No cloud relay, no middleman. The data never leaves the two devices involved."
                )
                Spacer(Modifier.height(10.dp))
                PrivacyPill(
                    emoji = "👁️",
                    title = "Contact info is gated by mutual consent",
                    body = "Your Instagram, Twitter/X, or email is never broadcast to the people around you. It is transmitted only at the moment both parties wave — and only to that one person."
                )
                Spacer(Modifier.height(10.dp))
                PrivacyPill(
                    emoji = "🔑",
                    title = "Stable device identity, no account",
                    body = "Your Wave & Vibe ID is generated once when you first set up the app and never changes. It exists only on your device — there is no account, no login, and no server that maps your ID to your real identity."
                )
                Spacer(Modifier.height(10.dp))
                PrivacyPill(
                    emoji = "🗑️",
                    title = "Delete = uninstall",
                    body = "Uninstalling the app removes everything. There is no \"delete account\" button because there is no account. No deletion request needed, no waiting period, no residual data."
                )
                Spacer(Modifier.height(10.dp))
                PrivacyPill(
                    emoji = "🆓",
                    title = "Free. No tracking.",
                    body = "Wave & Vibe is free to download and use. We do not sell data, or track behaviour. We have no business model that depends on your personal information."
                )
            }

            Spacer(Modifier.height(16.dp))

            // Feedback
            AboutCard {
                GradientLabel("Feedback & bugs")
                Spacer(Modifier.height(8.dp))
                AboutBody("Found a bug or have a suggestion? Leave a review or rating on Google Play — we read every one and ship fixes based on your feedback.")
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        val pkg = context.packageName
                        try {
                            context.startActivity(
                                android.content.Intent(android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("market://details?id=$pkg"))
                                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        } catch (_: Exception) {
                            context.startActivity(
                                android.content.Intent(android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://play.google.com/store/apps/details?id=$pkg"))
                                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(LogoGradient), RoundedCornerShape(50.dp))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Rate on Google Play ✦",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AboutCard(dark: Boolean = false, content: @Composable () -> Unit) {
    val bg = if (dark) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(20.dp)
    ) { content() }
}

@Composable
private fun GradientLabel(text: String, light: Boolean = false) {
    Text(
        text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        color = if (light) Brand else Brand
    )
}

@Composable
private fun AboutBody(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 20.sp
    )
}

@Composable
private fun AboutDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 14.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun HowItWorksStep(emoji: String, title: String, body: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(LogoGradient))
        ) {
            Text(emoji, fontSize = 18.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun PrivacyPill(emoji: String, title: String, body: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Text(emoji, fontSize = 16.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(body, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.55f), lineHeight = 18.sp)
        }
    }
}
