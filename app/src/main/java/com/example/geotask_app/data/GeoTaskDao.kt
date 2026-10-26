package com.example.geotask_app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GeoTaskDao {
    @Query("SELECT * FROM geo_tasks")
    fun getAllTasks(): Flow<List<GeoTaskEntity>>

    @Query("SELECT * FROM geo_tasks WHERE isCompleted = 0")
    fun getActiveTasks(): Flow<List<GeoTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<GeoTaskEntity>)

    @Query("DELETE FROM geo_tasks")
    suspend fun clearTasks()

    @Query("UPDATE geo_tasks SET isCompleted = 1 WHERE id = :taskId")
    suspend fun markTaskAsCompleted(taskId: String)
}