package com.example.geotask_app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.geotask_app.api.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val triggeredTaskIds = mutableSetOf<String>() // Aynı görev için tekrar bildirim atmamak için

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val lat = location.latitude
                    val lng = location.longitude
                    
                    checkLocationWithApi(lat, lng)
                }
            }
        }
    }

    private fun checkLocationWithApi(lat: Double, lng: Double) {
        // Eğer kullanıcı giriş yapmamışsa (Token yoksa) kontrol etmeyiz
        if (RetrofitClient.authToken == null) return

        serviceScope.launch {
            try {
                val response = RetrofitClient.apiService.checkLocation(lat, lng)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val triggeredTasks = body.triggeredTasks
                    
                    if (!triggeredTasks.isNullOrEmpty()) {
                        for (task in triggeredTasks) {
                            val taskKey = task.taskTitle // Backend tetiklenen görevin ID'sini dönmüyor, başlığı key olarak kullanıyoruz
                            if (!triggeredTaskIds.contains(taskKey)) {
                                // Görev alanına girildi, bildirim at
                                val desc = task.description ?: ""
                                sendTaskNotification(task.taskTitle, "$desc (${task.distance})")
                                triggeredTaskIds.add(taskKey)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LocationService", "API Error: ${e.localizedMessage}")
            }
        }
    }

    private fun sendTaskNotification(title: String, message: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "GeoTaskAlerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Görev Hatırlatıcıları",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                // Bildirime varsayılan veya özel ses ekleme ve titretme
                val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .build()
                
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500) // İki defa hızlı titrer
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL) // Ses ve titreşimi her sistemde zorla
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun startForegroundService() {
        val channelId = "LocationServiceChannel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Konum Takip Servisi",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("GeoTask Çalışıyor")
            .setContentText("Görev bölgelerinizi kontrol etmek için arka planda konumunuz izleniyor.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}