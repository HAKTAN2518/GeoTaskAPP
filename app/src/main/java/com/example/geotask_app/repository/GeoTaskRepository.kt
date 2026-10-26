package com.example.geotask_app.repository

import com.example.geotask_app.api.RetrofitClient
import com.example.geotask_app.data.GeoTaskDao
import com.example.geotask_app.data.GeoTaskEntity
import com.example.geotask_app.models.AuthRequest
import com.example.geotask_app.models.AuthResponse
import com.example.geotask_app.models.GeoTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GeoTaskRepository(private val dao: GeoTaskDao) {
    private val api = RetrofitClient.apiService

    suspend fun login(request: AuthRequest): Result<AuthResponse> {
        return try {
            val response = api.login(request)
            if (response.isSuccessful && response.body() != null) {
                RetrofitClient.authToken = response.body()!!.token
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: AuthRequest): Result<Unit> {
        return try {
            val response = api.register(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Register failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hem API'den çeker hem de Room DB'yi günceller
    suspend fun syncTasksFromApi(): Result<Unit> {
        return try {
            val response = api.getTasks()
            if (response.isSuccessful && response.body() != null) {
                val apiTasks = response.body()!!
                // API'den gelen veriyi Room'un formatına çeviriyoruz
                val entities = apiTasks.map { task ->
                    GeoTaskEntity(
                        id = task.id ?: "",
                        title = task.title,
                        description = task.description,
                        targetLatitude = task.targetLatitude,
                        targetLongitude = task.targetLongitude,
                        radiusInMeters = task.radiusInMeters,
                        isCompleted = task.isCompleted
                    )
                }
                // Mevcut DB'yi temizle ve yenilerini kaydet (tam senkronizasyon)
                dao.clearTasks()
                dao.insertTasks(entities)
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to get tasks from API"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // UI veya Servis için Local veritabanından akış (Flow) olarak görevleri döndürür
    fun getLocalActiveTasks(): Flow<List<GeoTask>> {
        return dao.getActiveTasks().map { entities ->
            entities.map { entity ->
                GeoTask(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    targetLatitude = entity.targetLatitude,
                    targetLongitude = entity.targetLongitude,
                    radiusInMeters = entity.radiusInMeters,
                    isCompleted = entity.isCompleted
                )
            }
        }
    }

    suspend fun createTask(task: GeoTask): Result<GeoTask> {
        return try {
            val response = api.createTask(task)
            if (response.isSuccessful && response.body() != null) {
                syncTasksFromApi() // Yeni görev oluştuysa DB'yi güncelle
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create task"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeTask(taskId: String): Result<Unit> {
        return try {
            val response = api.completeTask(taskId)
            if (response.isSuccessful) {
                dao.markTaskAsCompleted(taskId) // DB'de tamamlandı olarak işaretle
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to complete task"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            val response = api.deleteTask(taskId)
            if (response.isSuccessful) {
                syncTasksFromApi() // DB'yi tekrar senkronize et
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete task"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}