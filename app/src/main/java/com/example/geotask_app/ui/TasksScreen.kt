package com.example.geotask_app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: MainViewModel,
    onNavigateToAddTask: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My GeoTasks", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.geotask_app.ui.theme.Purple40,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                containerColor = com.example.geotask_app.ui.theme.BrightOrange,
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        // Backend'den dönen hataları göstermek için (Ekleme başarısız olursa göreceğiz)
        val errorMessage by viewModel.errorMessage.collectAsState()
        
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tasks found. Add a new one!", color = Color.Gray)
                }
            } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks) { task ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { task.id?.let { onNavigateToDetail(it) } },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Başlık ve Durum
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    // Eğer title boş gelirse "Unnamed Task" yazsın
                                    text = task.title.ifBlank { "Unnamed Task" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                AssistChip(
                                    onClick = { },
                                    label = { Text(if (task.isCompleted) "Completed" else "Active") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (task.isCompleted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                        labelColor = if (task.isCompleted) Color(0xFF2E7D32) else Color(0xFFE65100)
                                    ),
                                    border = null
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Açıklama
                            if (!task.description.isNullOrBlank()) {
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            // Mesafe / Radius bilgisi
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Radius: ${task.radiusInMeters}m", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            
                            // Alt Butonlar (Tamamla / Sil)
                            val context = androidx.compose.ui.platform.LocalContext.current
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (!task.isCompleted) {
                                    FilledTonalButton(
                                        onClick = { task.id?.let { viewModel.completeTask(it, context) } },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = com.example.geotask_app.ui.theme.SuccessGreen.copy(alpha = 0.2f),
                                            contentColor = com.example.geotask_app.ui.theme.SuccessGreen
                                        )
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Complete", fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    onClick = { task.id?.let { viewModel.deleteTask(it, context) } },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}