package com.example.geotask_app.models

import com.google.gson.annotations.SerializedName

data class GeoTask(
    val id: String? = null,
    
    // .NET API bazen büyük, bazen küçük harf yollayabilir, iki ihtimali de ekleyelim
    @SerializedName("baslik", alternate = ["Baslik", "title"])
    val title: String = "Untitled Task",
    
    @SerializedName("aciklama", alternate = ["Aciklama", "description"])
    val description: String? = null,
    
    @SerializedName("latitude", alternate = ["Latitude"])
    val targetLatitude: Double,
    
    @SerializedName("longitude", alternate = ["Longitude"])
    val targetLongitude: Double,
    
    @SerializedName("radiusInMeters", alternate = ["RadiusInMeters"])
    val radiusInMeters: Double,
    
    @SerializedName("isCompleted", alternate = ["IsCompleted"])
    val isCompleted: Boolean = false
)

data class TriggeredTask(
    @SerializedName("gorev", alternate = ["Gorev"])
    val taskTitle: String,
    
    @SerializedName("aciklama", alternate = ["Aciklama"])
    val description: String?,
    
    @SerializedName("mesafe", alternate = ["Mesafe"])
    val distance: String
)

data class LocationCheckResponse(
    @SerializedName("mesaj", alternate = ["Mesaj"])
    val message: String,
    
    @SerializedName("tetiklenenGorevler", alternate = ["TetiklenenGorevler"])
    val triggeredTasks: List<TriggeredTask>? = null
)