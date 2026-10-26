package com.example.geotask_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.geotask_app.models.GeoTask
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.UUID

@Composable
fun AddTaskScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var radiusStr by remember { mutableStateOf("100") }
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text("Add New GeoTask", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = radiusStr,
            onValueChange = { radiusStr = it },
            label = { Text("Radius (meters)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Location on Map:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Ücretsiz OSMDroid Haritası (Boyutu sınırlandırıldı)
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        setMultiTouchControls(true)
                        controller.setZoom(10.0)
                        controller.setCenter(GeoPoint(41.0082, 28.9784)) // İstanbul

                        // Asla 403 vermeyen Açık Topografik Harita
                        setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.OpenTopo)
                        
                        // Tıklama olaylarını yakalamak için receiver
                        val mapEventsReceiver = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                selectedLocation = p
                                // Eski marker'ı sil
                                overlays.removeAll { it is Marker }
                                
                                // Yeni marker ekle
                                val marker = Marker(this@apply)
                                marker.position = p
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                marker.title = "Target Location"
                                overlays.add(marker)
                                
                                invalidate() // Haritayı yenile
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint): Boolean = false
                        }
                        overlays.add(MapEventsOverlay(mapEventsReceiver))
                        mapView = this
                    }
                },
                // Haritanın yüksekliğini sınırla (ekranı kaplamasını engellemek için)
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp), // Klavyeden veya alttan biraz boşluk
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onNavigateBack, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (title.isNotBlank() && selectedLocation != null) {
                        val radius = radiusStr.toDoubleOrNull() ?: 100.0
                        val newTask = GeoTask(
                            id = UUID.randomUUID().toString(), // Geçici ID
                            title = title,
                            description = description.ifBlank { "No description" }, // Null yerine boş yazı gönder (Backend null kabul etmiyor olabilir)
                            targetLatitude = selectedLocation!!.latitude,
                            targetLongitude = selectedLocation!!.longitude,
                            radiusInMeters = radius,
                            isCompleted = false
                        )
                        viewModel.createTask(newTask)
                        onNavigateBack() // Geri dön
                    }
                },
                enabled = title.isNotBlank() && selectedLocation != null,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            ) {
                Text("Save Task")
            }
        }
    }
}
