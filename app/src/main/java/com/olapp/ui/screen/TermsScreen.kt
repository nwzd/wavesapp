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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olapp.ui.theme.Brand
import com.olapp.ui.theme.LogoGradient
import com.olapp.ui.theme.Tangerine

@Composable
fun TermsScreen(onAccept: () -> Unit, onDecline: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(72.dp).clip(CircleShape)
                    .background(Brush.linearGradient(LogoGradient))
            ) {
                Text("👋", fontSize = 32.sp)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Before you wave",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Please read and accept these terms to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))

            TermsCard {
                TermsSection("How Waves works") {
                    "Waves connects you with people physically nearby using Bluetooth and Wi-Fi. " +
                    "All profile data (name, photo, bio, contact info) is exchanged directly " +
                    "between phones — no server stores it. Your contact info is only shared " +
                    "after a mutual wave."
                }

                TermsDivider()

                TermsSection("You are responsible for your interactions") {
                    "Waves does not screen, verify, or endorse any user. You are solely " +
                    "responsible for how you interact with people you meet through this app. " +
                    "Always meet in public places and trust your instincts. We are not liable " +
                    "for any harm, loss, or dispute arising from interactions made through Waves."
                }

                TermsDivider()

                TermsSection("Acceptable use") {
                    "You agree not to use Waves to harass, stalk, threaten, or deceive others. " +
                    "You agree not to use the app for any unlawful purpose. Misuse may result " +
                    "in permanent removal from the app. We reserve the right to terminate access " +
                    "at any time, for any reason."
                }

                TermsDivider()

                TermsSection("No guarantees") {
                    "We do not guarantee that you will discover anyone nearby, that discovery " +
                    "will be instant, or that the app will work in all environments. " +
                    "Discovery depends on Bluetooth and Wi-Fi availability, Android permissions, " +
                    "and proximity. The app is provided as-is, without warranty of any kind."
                }

                TermsDivider()

                TermsSection("Privacy") {
                    "We collect no personal data on our servers. Your profile lives on your " +
                    "device. Location coordinates (if available) are stored locally and only " +
                    "shared with the person you vibed with, to help you both remember where " +
                    "you met. We have no access to your data."
                }

                TermsDivider()

                TermsSection("Age requirement") {
                    "You must be 18 years of age or older to use Waves. By accepting these " +
                    "terms you confirm that you meet this requirement."
                }

                TermsDivider()

                TermsSection("Deleting your account") {
                    "To delete your account and all associated data, simply uninstall the app. " +
                    "All your profile information is stored only on your device and is permanently " +
                    "removed when you uninstall. There is no server-side data to request deletion of."
                }

                TermsDivider()

                TermsSection("Changes to these terms") {
                    "We may update these terms at any time. Continued use of the app after " +
                    "changes constitutes acceptance of the new terms."
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // Sticky accept/decline bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAccept,
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
                    Text("I agree — let's go", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
            TextButton(
                onClick = onDecline,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Decline",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TermsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        content()
    }
}

@Composable
private fun TermsSection(title: String, body: () -> String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = Brand)
        Text(body(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
    }
}

@Composable
private fun TermsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 14.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
