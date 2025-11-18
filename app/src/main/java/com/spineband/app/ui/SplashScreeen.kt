package com.spineband.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spineband.app.R
import com.spineband.app.ui.theme.SpineBandWhite
import com.spineband.app.ui.theme.SpineBandOffWhite
import com.spineband.app.ui.theme.SpineBandCyan
import com.spineband.app.ui.theme.SpineBandNavy
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToNext: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "alpha"
    )

    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)
        onNavigateToNext()
    }

    // Fondo BLANCO con gradiente sutil
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SpineBandWhite,      // Blanco puro arriba
                        SpineBandOffWhite,   // Blanco suave medio
                        SpineBandWhite       // Blanco puro abajo
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
                .padding(32.dp)
        ) {
            // Logo (grande y centrado)
            Image(
                painter = painterResource(id = R.drawable.spineband),
                contentDescription = "SpineBand Logo",
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Nombre con color navy del logo
            Text(
                text = "SpineBand",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = SpineBandNavy,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtítulo con turquesa del logo
            Text(
                text = "Monitor de Postura Inteligente",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = SpineBandCyan,
                letterSpacing = 0.5.sp
            )
        }
    }
}