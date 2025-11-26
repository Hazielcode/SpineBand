package com.spineband.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.spineband.app.MainActivity
import com.spineband.app.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "posture_alerts"
        const val CHANNEL_NAME = "Alertas de Postura"
        const val NOTIFICATION_ID = 1001
    }

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Alertas cuando tu postura necesita corrección"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500) // Patrón de vibración
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showPostureAlert(angle: Float, duration: Int) {
        // Vibrar el dispositivo
        vibrateDevice()

        // Reproducir sonido de notificación
        playNotificationSound()

        // Crear intent para abrir la app al tocar la notificación
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Necesitarás crear este icono
            .setContentTitle("⚠️ ¡Corrige tu postura!")
            .setContentText("Tu espalda está inclinada ${angle.toInt()}°. Mantén mala postura por ${duration}s")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        // Mostrar la notificación
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    fun vibrateDevice() {
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Patrón: vibrar 500ms, pausa 200ms, vibrar 500ms
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 500, 200, 500),
                        -1 // No repetir
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
            }
        }
    }

    fun vibrateQuick() {
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        }
    }

    private fun playNotificationSound() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notification)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelNotification() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}