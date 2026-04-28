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

            // ── Terms of Service ──────────────────────────────────────────
            LegalHeading("Terms of Service")
            Spacer(Modifier.height(10.dp))

            TermsCard {
                TermsSection("1. The Service") {
                    "Wave & Vibe (\"App\") is a proximity-based social discovery application that enables " +
                    "users physically near each other to exchange expressions of interest (\"Waves\") " +
                    "using Bluetooth and Wi-Fi via the Google Nearby Connections API. The App " +
                    "operates without any server infrastructure maintained by us. All profile data, " +
                    "wave history, and connection records are stored exclusively on your local device. " +
                    "Profile data is transmitted directly between devices at the time a mutual Wave " +
                    "is completed. We have no access to, and do not store or process, any user data " +
                    "on our own servers."
                }

                TermsDivider()

                TermsSection("2. Eligibility") {
                    "You must be at least 18 years of age to use the App. By accepting these Terms " +
                    "you represent and warrant that you are at least 18 years old. If you are under 18 " +
                    "you must stop using the App and uninstall it immediately. Parents and legal " +
                    "guardians are responsible for monitoring minors' device use. We are not liable " +
                    "for any harm arising from use of the App by persons who misrepresent their age."
                }

                TermsDivider()

                TermsSection("3. Your Identity") {
                    "The App does not create or maintain user accounts. A unique identifier " +
                    "(\"Wave & Vibe ID\") is generated randomly on your device upon first use and persists " +
                    "until the App is uninstalled. This identifier is not linked to your real identity " +
                    "by us or any server. You are solely responsible for the accuracy and legality of " +
                    "the information you enter into the App, including your display name, photo, bio, " +
                    "and contact information."
                }

                TermsDivider()

                TermsSection("4. Acceptable Use") {
                    "You agree to use the App only for lawful, personal, and non-commercial purposes. " +
                    "You must not use the App to:\n\n" +
                    "(a) Harass, stalk, intimidate, threaten, or harm any person;\n" +
                    "(b) Impersonate any person or entity, or misrepresent your identity or affiliation;\n" +
                    "(c) Distribute content that is sexually explicit, depicts minors in any sexual " +
                    "context, promotes violence or hatred based on any protected characteristic, " +
                    "or otherwise violates applicable law;\n" +
                    "(d) Share another person's personal information without their explicit consent;\n" +
                    "(e) Engage in fraudulent, deceptive, or misleading conduct;\n" +
                    "(f) Probe, scan, or test the vulnerability of the App, or circumvent any " +
                    "security or safety feature;\n" +
                    "(g) Use the App for commercial solicitation, advertising, or promotion;\n" +
                    "(h) Interfere with or disrupt other users' experience;\n" +
                    "(i) Reverse-engineer, decompile, or extract source code from the App;\n" +
                    "(j) Violate any applicable local, national, or international law.\n\n" +
                    "Violations may result in your Wave & Vibe ID being blocked from the network through " +
                    "peer-to-peer enforcement mechanisms and may be reported to law enforcement."
                }

                TermsDivider()

                TermsSection("5. Safety and User Interactions") {
                    "The App facilitates contact between strangers in physical proximity. You " +
                    "acknowledge that:\n\n" +
                    "(a) We do not screen, verify, background-check, or endorse any user;\n" +
                    "(b) You are solely responsible for your safety and the decisions you make " +
                    "when interacting with other users;\n" +
                    "(c) You should meet new contacts in public places and inform someone you trust;\n" +
                    "(d) We cannot guarantee that any user is who they claim to be or will behave " +
                    "appropriately.\n\n" +
                    "We are not liable for any harm, injury, loss, or damage — whether physical, " +
                    "emotional, financial, or otherwise — arising from interactions between users, " +
                    "whether or not those interactions were facilitated by the App."
                }

                TermsDivider()

                TermsSection("6. Data Exchanged Between Users") {
                    "When a mutual Wave is completed, the App transmits your display name, profile " +
                    "photo, bio, and contact information directly from your device to the other " +
                    "user's device, and vice versa. Additionally, if location permissions are " +
                    "granted and a location fix is available, your approximate location at the " +
                    "time of the mutual Wave may also be transmitted as a record of where you met.\n\n" +
                    "These transmissions occur peer-to-peer via Google Nearby Connections and do " +
                    "not pass through any server operated by us. Once transmitted to another user's " +
                    "device, we have no ability to retrieve, modify, or delete that data. You accept " +
                    "sole responsibility for the content you choose to share and acknowledge this " +
                    "irrevocable P2P limitation."
                }

                TermsDivider()

                TermsSection("7. Intellectual Property") {
                    "All rights, title, and interest in the App — including its source code, design, " +
                    "branding, and content created by us — are and remain our exclusive property. " +
                    "Nothing in these Terms transfers any intellectual property rights to you. You " +
                    "are granted a limited, non-exclusive, non-transferable, revocable licence to " +
                    "use the App solely for personal, non-commercial purposes in accordance with " +
                    "these Terms. User-generated content (your profile data, photos, and contact " +
                    "information) remains your property. By entering it into the App, you grant " +
                    "other matched users a personal, non-commercial licence to store and view it " +
                    "on their devices."
                }

                TermsDivider()

                TermsSection("8. Disclaimer of Warranties") {
                    "THE APP IS PROVIDED \"AS IS\" AND \"AS AVAILABLE\" WITHOUT WARRANTY OF ANY KIND, " +
                    "EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY, " +
                    "FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT, OR UNINTERRUPTED OR " +
                    "ERROR-FREE OPERATION. WE DO NOT WARRANT THAT THE APP WILL DISCOVER NEARBY " +
                    "USERS, THAT WAVE DELIVERY WILL BE RELIABLE OR TIMELY, OR THAT THE APP WILL " +
                    "OPERATE WITHOUT DEFECT OR INTERRUPTION. DISCOVERY DEPENDS ON BLUETOOTH AND " +
                    "WI-FI AVAILABILITY, ANDROID SYSTEM PERMISSIONS, PHYSICAL PROXIMITY, NETWORK " +
                    "CONDITIONS, AND OTHER FACTORS ENTIRELY OUTSIDE OUR CONTROL."
                }

                TermsDivider()

                TermsSection("9. Limitation of Liability") {
                    "TO THE FULLEST EXTENT PERMITTED BY APPLICABLE LAW:\n\n" +
                    "(a) WE SHALL NOT BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, " +
                    "CONSEQUENTIAL, EXEMPLARY, OR PUNITIVE DAMAGES ARISING OUT OF OR RELATED TO " +
                    "YOUR USE OF OR INABILITY TO USE THE APP;\n\n" +
                    "(b) WE SHALL NOT BE LIABLE FOR ANY HARM ARISING FROM INTERACTIONS BETWEEN " +
                    "USERS, INCLUDING MEETINGS FACILITATED BY THE APP;\n\n" +
                    "(c) WE SHALL NOT BE LIABLE FOR ANY LOSS OR DAMAGE RESULTING FROM DATA " +
                    "TRANSMITTED PEER-TO-PEER BETWEEN USERS' DEVICES;\n\n" +
                    "(d) OUR TOTAL AGGREGATE LIABILITY FOR ANY CLAIM ARISING UNDER OR RELATED TO " +
                    "THESE TERMS SHALL NOT EXCEED THE TOTAL AMOUNT YOU PAID US IN THE TWELVE MONTHS " +
                    "PRECEDING THE CLAIM. AS THE APP IS FREE, THIS AMOUNT IS ZERO.\n\n" +
                    "Where applicable law prohibits the exclusion of certain warranties or liabilities, " +
                    "our liability is limited to the minimum extent permitted."
                }

                TermsDivider()

                TermsSection("10. Indemnification") {
                    "You agree to defend, indemnify, and hold harmless us and our officers, " +
                    "directors, employees, and agents from and against any and all claims, damages, " +
                    "losses, costs, and expenses (including reasonable legal fees) arising out of " +
                    "or related to: (a) your use of the App; (b) your breach of these Terms; " +
                    "(c) your violation of any applicable law or the rights of any third party; " +
                    "or (d) any content you transmit through the App."
                }

                TermsDivider()

                TermsSection("11. Third-Party Services") {
                    "The App uses the Google Nearby Connections API, operated by Google LLC. Your " +
                    "use of the App is therefore subject to Google's Terms of Service and Google's " +
                    "Privacy Policy. We are not responsible for Google's data practices or the " +
                    "availability of Google's services. If Google's service is unavailable, the " +
                    "App will not function."
                }

                TermsDivider()

                TermsSection("12. Modifications to These Terms") {
                    "We reserve the right to modify these Terms at any time for reasons including " +
                    "changes to the App's functionality, applicable law, or security requirements. " +
                    "When Terms are updated, the revised version will be presented within the App. " +
                    "Your continued use of the App after any update constitutes your acceptance of " +
                    "the revised Terms. If you do not accept the revised Terms, you must stop using " +
                    "the App and uninstall it."
                }

                TermsDivider()

                TermsSection("13. Termination") {
                    "You may terminate your use of the App at any time by uninstalling it. Because " +
                    "the App has no server component, uninstalling permanently removes all your data " +
                    "from your device. We may restrict a user's participation in the network — " +
                    "including by propagating block signals to other users' devices — if we have " +
                    "reasonable grounds to believe that user has violated these Terms or applicable law."
                }

                TermsDivider()

                TermsSection("14. Governing Law and Disputes") {
                    "These Terms are governed by and construed in accordance with the laws of " +
                    "[Jurisdiction], without regard to conflict-of-law principles. Before initiating " +
                    "any formal legal proceeding, you agree to first contact us to attempt informal " +
                    "resolution. Any dispute not resolved informally within 30 days shall be subject " +
                    "to the exclusive jurisdiction of the courts of [Jurisdiction]."
                }

                TermsDivider()

                TermsSection("15. Severability and Entire Agreement") {
                    "If any provision of these Terms is found invalid or unenforceable, it shall be " +
                    "modified to the minimum extent necessary to make it enforceable, and all " +
                    "remaining provisions shall continue in full force. These Terms, together with " +
                    "the Privacy Policy below, constitute the entire agreement between you and us " +
                    "regarding the App and supersede all prior agreements and representations."
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Privacy Policy ────────────────────────────────────────────
            LegalHeading("Privacy Policy")
            Spacer(Modifier.height(10.dp))

            TermsCard {
                TermsSection("1. What We Do Not Collect") {
                    "We do not collect, receive, store, or process any personal data about you on " +
                    "any server operated by us. We have no database, no analytics pipeline, no " +
                    "advertising network, and no technical ability to access your personal " +
                    "information. This is not a policy commitment that could be reversed by a " +
                    "future update — it is a consequence of the App's architecture: there is no " +
                    "server. We collect no usage data, no crash reports tied to your identity, " +
                    "and no behavioural analytics."
                }

                TermsDivider()

                TermsSection("2. Data Stored on Your Device") {
                    "The following data is created and stored exclusively on your device in a " +
                    "sandboxed local database:\n\n" +
                    "• Your display name, profile photo, bio, and contact information;\n" +
                    "• Your Wave & Vibe ID (a randomly generated, device-local identifier);\n" +
                    "• Records of waves you have sent and received;\n" +
                    "• Records of mutual connections (\"Vibes\"), including the approximate time " +
                    "and location where they occurred;\n" +
                    "• A list of users you have blocked.\n\n" +
                    "This data is under your sole control. You may delete it using \"Start fresh\" " +
                    "in your profile or by uninstalling the App. Uninstalling the App permanently " +
                    "and irreversibly removes all data from your device. We have no copy and " +
                    "cannot recover it."
                }

                TermsDivider()

                TermsSection("3. Data Transmitted to Other Users") {
                    "When a mutual Wave is completed, the following data is transmitted directly " +
                    "from your device to the matched user's device:\n\n" +
                    "• Display name, profile photo, bio, and contact information;\n" +
                    "• Your approximate location at the time of the match (if location permission " +
                    "is granted and a fix is available).\n\n" +
                    "This transmission occurs peer-to-peer via Google Nearby Connections and does " +
                    "not pass through any server operated by us. Once transmitted, a copy of your " +
                    "data resides on the other user's device. We have no ability to retrieve, " +
                    "modify, or erase that copy. The same applies to your Wave notifications: when " +
                    "you Wave at someone, they receive your display name in a notification on their " +
                    "device. Only enter contact information you are prepared to share."
                }

                TermsDivider()

                TermsSection("4. Permissions We Request") {
                    "• Bluetooth and Nearby Wi-Fi: Required to discover nearby users and establish " +
                    "peer-to-peer connections. Without these, the App cannot function.\n\n" +
                    "• Location (approximate, while using): Required by Android for Bluetooth and " +
                    "Wi-Fi scanning. Also used to record the approximate location of a mutual Wave.\n\n" +
                    "• Notifications: To alert you when someone nearby waves at you, or when a " +
                    "Vibe is created.\n\n" +
                    "• Camera / Photo access: To allow you to set a profile photo.\n\n" +
                    "You may revoke any permission in your device settings at any time. Revoking " +
                    "Bluetooth or Wi-Fi permissions will prevent discovery from functioning."
                }

                TermsDivider()

                TermsSection("5. Third-Party Services") {
                    "The App uses the Google Nearby Connections API (Google LLC) to facilitate " +
                    "peer-to-peer device discovery and data transfer. When the App runs, the " +
                    "Nearby Connections SDK may collect technical and diagnostic data (such as " +
                    "connection quality metrics or device identifiers) in accordance with Google's " +
                    "own Privacy Policy at policies.google.com/privacy. We do not control and " +
                    "are not responsible for Google's data practices.\n\n" +
                    "We do not use any other third-party analytics, advertising, or data-sharing " +
                    "services."
                }

                TermsDivider()

                TermsSection("6. Children's Privacy") {
                    "The App is intended exclusively for users aged 18 and over and is not " +
                    "directed at children. We do not knowingly facilitate the collection or " +
                    "transmission of personal data from children under 13 (or the applicable " +
                    "age in your jurisdiction). If you believe a minor is using the App, the " +
                    "appropriate remedy is to uninstall the App from their device, as we have " +
                    "no server-side mechanism to remove their data."
                }

                TermsDivider()

                TermsSection("7. Your Rights") {
                    "Depending on your jurisdiction (including the European Economic Area, " +
                    "United Kingdom, and California), you may have rights to access, rectify, " +
                    "erase, restrict, or port your personal data. Because all data is stored " +
                    "on your device, you exercise these rights directly:\n\n" +
                    "• Access and rectification: View and edit your data in the App at any time;\n" +
                    "• Erasure: Use \"Start fresh\" or uninstall the App;\n" +
                    "• Portability: Your data does not leave your device except as described in " +
                    "Section 3.\n\n" +
                    "For data already transmitted to another user's device, we cannot fulfil " +
                    "erasure or access requests, as we have no control over data held on " +
                    "third-party devices. This limitation is inherent to peer-to-peer " +
                    "architecture and is disclosed in these Terms."
                }

                TermsDivider()

                TermsSection("8. Data Retention") {
                    "Data on your device is retained until you delete it or uninstall the App. " +
                    "Unmatched Wave records (waves sent or received that did not result in a " +
                    "mutual connection) are automatically removed from your device after 30 days. " +
                    "All data is permanently deleted upon uninstall."
                }

                TermsDivider()

                TermsSection("9. Security") {
                    "Data on your device is stored in a local database protected by Android's " +
                    "application sandbox. Peer-to-peer transmissions occur over encrypted channels " +
                    "provided by the Google Nearby Connections API. Because we hold no copy of " +
                    "your data, a breach of our infrastructure cannot expose your personal " +
                    "information. Your data's security is ultimately governed by the security of " +
                    "your own device."
                }

                TermsDivider()

                TermsSection("10. Changes to This Policy") {
                    "We may update this Privacy Policy to reflect changes in the App's " +
                    "functionality, applicable law, or security practices. Updates will be " +
                    "incorporated into the App. Your continued use of the App after any update " +
                    "constitutes acceptance of the revised policy."
                }

                TermsDivider()

                TermsSection("11. Contact") {
                    "For any questions, concerns, or rights requests regarding these Terms or " +
                    "this Privacy Policy, contact us at: [contact@yourdomain.com]\n\n" +
                    "Effective date: 28 April 2026"
                }
            }

            Spacer(Modifier.height(24.dp))
        }

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
private fun LegalHeading(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Brand
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
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
