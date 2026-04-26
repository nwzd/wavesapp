package com.olapp.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.olapp.ui.theme.Brand
import com.olapp.ui.theme.LogoGradient

@Composable
fun AgeVerificationScreen(onVerified: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo — gradient circle with one bold sine wave
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(108.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(LogoGradient))
            ) {
                Canvas(modifier = Modifier.size(72.dp)) {
                    val w = size.width
                    val h = size.height
                    val path = Path().apply {
                        // Single S-curve: left-centre → peaks up → centre → troughs down → right-centre
                        moveTo(w * 0.04f, h * 0.50f)
                        cubicTo(
                            w * 0.17f, h * 0.22f,
                            w * 0.33f, h * 0.22f,
                            w * 0.50f, h * 0.50f
                        )
                        cubicTo(
                            w * 0.67f, h * 0.78f,
                            w * 0.83f, h * 0.78f,
                            w * 0.96f, h * 0.50f
                        )
                    }
                    drawPath(path, Color.White, style = Stroke(5.5f * density, cap = StrokeCap.Round))
                }
            }

            Spacer(Modifier.height(36.dp))

            Text(
                "Waves",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Discover people nearby",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Bluetooth only · No account · No tracking\nJust the people around you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(60.dp))

            Button(
                onClick = onVerified,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand)
            ) {
                Text(
                    "I'm 18 or older — let's go",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Age verification is stored on this device only.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
