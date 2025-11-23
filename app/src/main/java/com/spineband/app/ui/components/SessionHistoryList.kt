package com.spineband.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spineband.app.data.database.entities.PostureRecord
import com.spineband.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionHistoryList(
    records: List<PostureRecord>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SpineBandWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    tint = SpineBandCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Historial de Sesión",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpineBandNavy
                )
            }

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin registros aún",
                        color = SpineBandDarkGray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(records) { record ->
                        HistoryItem(record)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(record: PostureRecord) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val time = timeFormat.format(Date(record.timestamp))

    // ⬇️ LÓGICA CORREGIDA: Usar isGoodPosture en vez de status
    val isGood = record.isGoodPosture

    val statusColor = if (isGood) SpineBandGreen else SpineBandRed

    val statusIcon = if (isGood) {
        Icons.Default.SentimentVerySatisfied // 😊 Carita feliz
    } else {
        Icons.Default.SentimentDissatisfied  // 😟 Carita triste
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = statusColor.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de estado
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.status, // "Buena Postura" o "Mala Postura"
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = time,
                    fontSize = 12.sp,
                    color = SpineBandDarkGray
                )
            }

            // Ángulo
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.1f°".format(record.angle),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpineBandNavy
                )
                Text(
                    text = "ángulo",
                    fontSize = 10.sp,
                    color = SpineBandDarkGray
                )
            }
        }
    }
}