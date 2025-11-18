package com.spineband.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spineband.app.ui.theme.*

@Composable
fun PostureAlertBanner(
    show: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = show,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it }
        ) + fadeOut()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SpineBandOrange,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono animado
                    PulsingWarningIcon()

                    Spacer(modifier = Modifier.width(12.dp))

                    // Mensaje
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚠️ ¡Atención!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpineBandWhite
                        )
                        Text(
                            text = "Has mantenido mala postura por más de 10 segundos",
                            fontSize = 13.sp,
                            color = SpineBandWhite.copy(alpha = 0.9f)
                        )
                    }

                    // Botón cerrar
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = SpineBandWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsingWarningIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Icon(
        Icons.Default.Warning,
        contentDescription = null,
        tint = SpineBandWhite,
        modifier = Modifier
            .size(32.dp)
            .scale(scale)
    )
}