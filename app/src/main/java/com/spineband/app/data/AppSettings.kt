package com.spineband.app.data

import android.content.Context
import android.content.SharedPreferences

class AppSettings(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("spineband_settings", Context.MODE_PRIVATE)

    var esp32IP: String
        get() = prefs.getString("esp32_ip", "10.178.71.26") ?: "10.178.71.26"
        set(value) = prefs.edit().putString("esp32_ip", value).apply()

    var sensitivityLevel: Float
        get() = prefs.getFloat("sensitivity", 30f)
        set(value) = prefs.edit().putFloat("sensitivity", value).apply()

    var checkInterval: Float
        get() = prefs.getFloat("check_interval", 5f)
        set(value) = prefs.edit().putFloat("check_interval", value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications", true)
        set(value) = prefs.edit().putBoolean("notifications", value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean("vibration", true)
        set(value) = prefs.edit().putBoolean("vibration", value).apply()
}
