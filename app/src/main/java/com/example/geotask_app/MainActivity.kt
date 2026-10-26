package com.example.geotask_app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geotask_app.service.LocationService
import com.example.geotask_app.ui.MainViewModel
import com.example.geotask_app.ui.theme.Geotask_appTheme

import android.widget.Toast
import org.osmdroid.config.Configuration
import android.preference.PreferenceManager

// Temizlendi

class MainActivity : ComponentActivity() {

    // İzinleri istemek için Android'in modern permission launcher'ı
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                              permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (locationGranted) {
            // Android 10 (Q) ve üzeri için Arka Plan konumu da gerekiyorsa isteyelim
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                     requestBackgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                     startLocationService()
                }
            } else {
                startLocationService()
            }
        } else {
            Toast.makeText(this, "Location permission is required for GeoTask to work.", Toast.LENGTH_LONG).show()
        }
    }

    private val requestBackgroundLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startLocationService()
        } else {
            Toast.makeText(this, "Background location permission is recommended for tracking tasks while app is closed.", Toast.LENGTH_LONG).show()
            // Arka plan izni olmasa da ön plan izniyle servisi başlatalım
            startLocationService()
        }
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // OSMDroid ayarlarını başlat (Kullanıcı Agent'ı belirlemek önemli)
        val context = applicationContext
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
        // 403 hatasını aşmak için standart bir tarayıcı (Chrome) User-Agent'ı taklit edelim
        Configuration.getInstance().userAgentValue = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
        
        enableEdgeToEdge()
        
        // Uygulama açıldığında izinleri iste
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        // Android 13 ve üzeri için Bildirim izni gerekli
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())

        setContent {
            Geotask_appTheme {
                val viewModel: MainViewModel = viewModel()
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                        } else {
                            AppNavigation(viewModel = viewModel, isLoggedIn = isLoggedIn)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("GeoTask Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    }
}

// Eski TasksScreen silindi. Artık com.example.geotask_app.ui.TasksScreen kullanılıyor.