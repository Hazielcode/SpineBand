package com.spineband.app.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class PostureData(
    val angleX: Float,
    val angleY: Float,
    val angleZ: Float,
    val goodPosture: Boolean,
    val timestamp: Long
)

class SpineBandApi(private val baseUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun getPostureData(): PostureData? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/api/posture"
            Log.d("SpineBandApi", "Intentando conectar a: $url")

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string()
                Log.d("SpineBandApi", "Respuesta recibida: $body")

                if (body != null) {
                    val json = JSONObject(body)

                    val data = PostureData(
                        angleX = json.getDouble("angleX").toFloat(),
                        angleY = json.getDouble("angleY").toFloat(),
                        angleZ = json.getDouble("angleZ").toFloat(),
                        goodPosture = json.getBoolean("goodPosture"),
                        timestamp = json.getLong("timestamp")
                    )

                    Log.d("SpineBandApi", "Datos parseados: angleX=${data.angleX}, goodPosture=${data.goodPosture}")
                    return@withContext data
                }
            } else {
                Log.e("SpineBandApi", "Error HTTP: ${response.code}")
            }

            null
        } catch (e: Exception) {
            Log.e("SpineBandApi", "Error en getPostureData: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    suspend fun calibrate(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/api/calibrate"
            Log.d("SpineBandApi", "Calibrando en: $url")

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val success = response.isSuccessful

            Log.d("SpineBandApi", "Calibración: ${if (success) "exitosa" else "fallida"}")
            success
        } catch (e: Exception) {
            Log.e("SpineBandApi", "Error en calibrate: ${e.message}", e)
            false
        }
    }

    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = baseUrl
            Log.d("SpineBandApi", "Probando conexión a: $url")

            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val success = response.isSuccessful

            Log.d("SpineBandApi", "Test de conexión: ${if (success) "OK" else "FALLÓ"}")
            success
        } catch (e: Exception) {
            Log.e("SpineBandApi", "Error en testConnection: ${e.message}", e)
            false
        }
    }
}