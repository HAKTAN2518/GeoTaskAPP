package com.example.geotask_app.api

import com.example.geotask_app.models.AuthRequest
import com.example.geotask_app.models.AuthResponse
import com.example.geotask_app.models.GeoTask
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GeoTaskApiService {

    @POST("api/Auth/giris")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/Auth/kayit")
    suspend fun register(@Body request: AuthRequest): Response<Unit>

    @GET("api/GeoTask")
    suspend fun getTasks(): Response<List<GeoTask>>

    @POST("api/GeoTask")
    suspend fun createTask(@Body task: GeoTask): Response<GeoTask>

    @GET("api/GeoTask/check-location")
    suspend fun checkLocation(
        @Query("currentLat") lat: Double,
        @Query("currentLon") lng: Double
    ): Response<com.example.geotask_app.models.LocationCheckResponse>

    @PUT("api/GeoTask/{id}/tamamla")
    suspend fun completeTask(@Path("id") taskId: String): Response<Unit>

    @DELETE("api/GeoTask/{id}")
    suspend fun deleteTask(@Path("id") taskId: String): Response<Unit>
}