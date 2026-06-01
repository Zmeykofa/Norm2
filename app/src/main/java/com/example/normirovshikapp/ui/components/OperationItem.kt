package com.example.normirovshikapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.normirovshikapp.data.OperationEntity
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OperationItem(
    op: OperationEntity,
    currentTime: Long,
    onStop: () -> Unit,
    onEdit: () -> Unit,
    onSplit: () -> Unit,
    onDelete: () -> Unit,
    onRepeat: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()) }

    val durationMillis = (op.stopEpoch ?: currentTime) - op.startEpoch
    val durationSeconds = durationMillis / 1000
    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60
    val durationFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val isActive = op.stopEpoch == null

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
            else
                MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isActive) 6.dp else 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = op.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Редактировать", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Удалить", modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            InfoRow("Начало", sdf.format(Date(op.startEpoch)), leadingIcon = Icons.Outlined.AccessTime)
            InfoRow("Окончание", op.stopEpoch?.let { sdf.format(Date(it)) } ?: "—", leadingIcon = Icons.Outlined.AccessTime)
            InfoRow("Продолжительность", durationFormatted, leadingIcon = Icons.Outlined.Refresh)

            Spacer(Modifier.height(4.dp))
            InfoRow("Исполнители", op.workers.ifBlank { "—" }, leadingIcon = Icons.Outlined.People)
            InfoRow("Количество человек", op.people.toString(), leadingIcon = Icons.Outlined.Workspaces)
            InfoRow("Инструменты", op.tools.ifBlank { "—" })
            InfoRow("Техника", op.equipment.split(", ").joinToString(", ") { it.substringBefore("=") }.ifBlank { "—" })
            InfoRow("Материалы", op.materials.ifBlank { "—" })
            InfoRow("Заметки", op.notes.ifBlank { "—" })

            if (op.stopEpoch == null) {
                Row {
                    Button(
                        onClick = onStop,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Outlined.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Остановить")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onSplit,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Завершить и новая")
                    }
                }
            } else {
                Row {
                    Button(
                        onClick = onRepeat,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Повторить операцию")
                    }
                }
            }
        }
    }
}
