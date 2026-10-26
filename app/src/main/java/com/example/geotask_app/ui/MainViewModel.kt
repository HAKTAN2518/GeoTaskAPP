package com.example.geotask_app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.geotask_app.api.RetrofitClient
import com.example.geotask_app.data.AppDatabase
import com.example.geotask_app.data.TokenManager
import com.example.geotask_app.models.AuthRequest
import com.example.geotask_app.models.GeoTask
import com.example.geotask_app.repository.GeoTaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).geoTaskDao()
    private val repository = GeoTaskRepository(dao)
    private val tokenManager = TokenManager(application)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(true) // Başlangıçta durumu kontrol ederken bekleme
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _tasks = MutableStateFlow<List<GeoTask>>(emptyList())
    val tasks: StateFlow<List<GeoTask>> = _tasks.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkTokenAndLogin()

        // ViewModel oluştuğunda local db'deki aktif görevleri dinlemeye başla
        viewModelScope.launch {
            repository.getLocalActiveTasks().collect { localTasks ->
                _tasks.value = localTasks
            }
        }
    }

    private fun checkTokenAndLogin() {
        viewModelScope.launch {
            _isLoading.value = true
            val savedToken = tokenManager.token.first()
            if (!savedToken.isNullOrEmpty()) {
                RetrofitClient.authToken = savedToken
                _isLoggedIn.value = true
                repository.syncTasksFromApi()
            } else {
                _isLoggedIn.value = false
            }
            _isLoading.value = false
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            // "email" ve "pass" arayüzdeki değişken adları, ancak bunları API'nin beklediği "eposta" ve "sifre" alanlarına eşliyoruz.
            val result = repository.login(AuthRequest(eposta = email, sifre = pass))
            if (result.isSuccess) {
                // Token'i DataStore'a kaydet
                val token = result.getOrNull()?.token
                if (!token.isNullOrEmpty()) {
                    tokenManager.saveToken(token)
                }

                _isLoggedIn.value = true
                _errorMessage.value = null
                // Giriş başarılı olunca API'deki güncel görevleri Local DB'ye eşitle (Senkronize et)
                repository.syncTasksFromApi()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            RetrofitClient.authToken = null
            _isLoggedIn.value = false
        }
    }

    fun createTask(task: GeoTask) {
        viewModelScope.launch {
            val result = repository.createTask(task)
            if (result.isFailure) {
                // Hatayı ekranda (Toast vb. yerine geçici olarak log/error state olarak) göster
                _errorMessage.value = "Failed to create task: ${result.exceptionOrNull()?.message}"
            } else {
                // Başarılı olursa listeyi zorla yenile
                repository.syncTasksFromApi()
            }
        }
    }

    fun completeTask(taskId: String, context: android.content.Context) {
        viewModelScope.launch {
            val result = repository.completeTask(taskId)
            if (result.isFailure) {
                _errorMessage.value = "Failed to complete task: ${result.exceptionOrNull()?.message}"
            } else {
                android.widget.Toast.makeText(context, "Görev başarıyla tamamlandı!", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteTask(taskId: String, context: android.content.Context) {
        viewModelScope.launch {
            val result = repository.deleteTask(taskId)
            if (result.isFailure) {
                _errorMessage.value = "Failed to delete task: ${result.exceptionOrNull()?.message}"
            } else {
                android.widget.Toast.makeText(context, "Görev silindi.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getTaskById(taskId: String): GeoTask? {
        return _tasks.value.find { it.id == taskId }
    }
}