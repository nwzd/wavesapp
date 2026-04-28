package com.olapp.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olapp.R
import com.olapp.data.model.ContactPlatform

val ContactPlatform.brandColor: Color
    get() = when (this) {
        ContactPlatform.INSTAGRAM -> Color(0xFFE1306C)
        ContactPlatform.TIKTOK    -> Color(0xFF010101)
        ContactPlatform.SNAPCHAT  -> Color(0xFFFFFC00)
        ContactPlatform.FACEBOOK  -> Color(0xFF1877F2)
        ContactPlatform.TWITTER   -> Color(0xFF000000)
        ContactPlatform.LINKEDIN  -> Color(0xFF0A66C2)
        ContactPlatform.DISCORD   -> Color(0xFF5865F2)
        ContactPlatform.EMAIL     -> Color(0xFF7B61FF)
        ContactPlatform.OTHER     -> Color(0xFF9E9E9E)
    }

// Text label shown inside the colored circle for platforms without a drawable
val ContactPlatform.iconLabel: String
    get() = when (this) {
        ContactPlatform.SNAPCHAT -> "👻"
        ContactPlatform.FACEBOOK -> "f"
        ContactPlatform.LINKEDIN -> "in"
        ContactPlatform.DISCORD  -> "dc"
        ContactPlatform.TIKTOK   -> "tt"
        else                     -> label.first().toString()
    }

val ContactPlatform.iconLabelColor: Color
    get() = when (this) {
        ContactPlatform.SNAPCHAT -> Color.Black
        else                     -> Color.White
    }

@DrawableRes
fun ContactPlatform.logoRes(): Int? = when (this) {
    ContactPlatform.INSTAGRAM -> R.drawable.ic_instagram
    ContactPlatform.TWITTER   -> R.drawable.ic_x_twitter
    else                      -> null
}

fun ContactPlatform.materialIcon(): ImageVector? = when (this) {
    ContactPlatform.EMAIL -> Icons.Default.AlternateEmail
    else                  -> null
}

@Composable
fun PlatformIcon(platform: ContactPlatform, size: Dp = 32.dp) {
    val res = platform.logoRes()
    val matIcon = platform.materialIcon()
    when {
        res != null -> {
            Box(modifier = Modifier.size(size).clip(CircleShape)) {
                Image(
                    painter = painterResource(res),
                    contentDescription = platform.label,
                    modifier = Modifier.size(size)
                )
            }
        }
        matIcon != null -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(size).clip(CircleShape).background(platform.brandColor)
            ) {
                Icon(
                    imageVector = matIcon,
                    contentDescription = platform.label,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }
        else -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(size).clip(CircleShape).background(platform.brandColor)
            ) {
                val label = platform.iconLabel
                val isEmoji = label.any { it.code > 127 }
                Text(
                    text = label,
                    color = if (isEmoji) Color.Unspecified else platform.iconLabelColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (label.length == 1) (size.value * 0.48f).sp
                               else (size.value * 0.32f).sp,
                    lineHeight = size.value.sp
                )
            }
        }
    }
}
