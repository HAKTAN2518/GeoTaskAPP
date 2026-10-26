package com.example.geotask_app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geo_tasks")
data class GeoTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val targetLatitude: Double,
    val targetLongitude: Double,
    val radiusInMeters: Double,
    val isCompleted: Boolean
)