package com.example.geotask_app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val task = viewModel.getTaskById(taskId)
    
    if (task == null) {
        // Görev bulunamadıysa geri dön
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
        return
    }

    val targetLocation = GeoPoint(task.targetLatitude, task.targetLongitude)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Harita Alanı
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            setMultiTouchControls(true)
                            setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.OpenTopo)
                            controller.setZoom(16.0)
                            controller.setCenter(targetLocation)

                            // İşaretçi (Pin)
                            val marker = Marker(this)
                            marker.position = targetLocation
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = task.title
                            overlays.add(marker)

                            // Yarıçap çemberi (Polygon ile yaklaşık daire çizimi)
                            val circle = Polygon(this)
                            circle.points = Polygon.pointsAsCircle(targetLocation, task.radiusInMeters)
                            circle.fillColor = 0x330000FF // Transparan Mavi
                            circle.strokeColor = 0xFF0000FF.toInt() // Koyu Mavi
                            circle.strokeWidth = 2f
                            overlays.add(circle)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Detay Kartı
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (task.isCompleted) "Completed" else "Active",
                            color = if (task.isCompleted) Color(0xFF2E7D32) else Color(0xFFE65100),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (!task.description.isNullOrBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text("Target Radius: ${task.radiusInMeters} meters", style = MaterialTheme.typography.bodyMedium)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Alt Butonlar
                    val context = androidx.compose.ui.platform.LocalContext.current
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        if (!task.isCompleted) {
                            Button(
                                onClick = {
                                    task.id?.let { viewModel.completeTask(it, context) }
                                    onNavigateBack() // Tamamlayınca geri dön
                                },
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            ) {
                                Text("Mark as Completed")
                            }
                        }
                        
                        OutlinedButton(
                            onClick = {
                                task.id?.let { viewModel.deleteTask(it, context) }
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f).padding(start = if (!task.isCompleted) 8.dp else 0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete Task")
                        }
                    }
                }
            }
        }
    }
}