package com.olapp.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.olapp.R
import com.olapp.data.model.ContactPlatform

val ContactPlatform.brandColor: Color
    get() = when (this) {
        ContactPlatform.INSTAGRAM -> Color(0xFFE1306C)
        ContactPlatform.WHATSAPP  -> Color(0xFF25D366)
        ContactPlatform.TELEGRAM  -> Color(0xFF0088CC)
        ContactPlatform.TWITTER   -> Color(0xFF000000)
        ContactPlatform.PHONE     -> Color(0xFF4CAF50)
        ContactPlatform.EMAIL     -> Color(0xFF7B61FF)
        ContactPlatform.OTHER     -> Color(0xFF9E9E9E)
    }

// PNG logos that already include their own background colour
@DrawableRes
fun ContactPlatform.logoRes(): Int? = when (this) {
    ContactPlatform.INSTAGRAM -> R.drawable.ic_instagram
    ContactPlatform.WHATSAPP  -> R.drawable.ic_whatsapp
    ContactPlatform.TELEGRAM  -> R.drawable.ic_telegram
    ContactPlatform.TWITTER   -> R.drawable.ic_x_twitter
    else                      -> null
}

// Material icon fallback for platforms with no brand PNG
fun ContactPlatform.materialIcon(): ImageVector? = when (this) {
    ContactPlatform.PHONE -> Icons.Default.Phone
    ContactPlatform.EMAIL -> Icons.Default.AlternateEmail
    else                  -> null
}

@Composable
fun PlatformIcon(platform: ContactPlatform, size: Dp = 32.dp) {
    val res = platform.logoRes()
    if (res != null) {
        // PNG already has the correct background — just clip to circle and display as-is
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.size(size).clip(CircleShape)
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(res),
                contentDescription = platform.label,
                modifier = Modifier.size(size)
            )
        }
    } else {
        // Material icon: draw coloured circle + white icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size).clip(CircleShape).background(platform.brandColor)
        ) {
            val mat = platform.materialIcon()
            if (mat != null) {
                Icon(
                    imageVector = mat,
                    contentDescription = platform.label,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }
    }
}
