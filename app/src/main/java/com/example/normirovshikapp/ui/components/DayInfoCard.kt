package com.example.normirovshikapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.normirovshikapp.data.DayEntity
import com.example.normirovshikapp.data.OperationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DayInfoCard(
    day: DayEntity,
    operations: List<OperationEntity>,
    onEdit: () -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = if (expanded) "Свернуть" else "Развернуть"
                        )
                    }
                    Text("Общая информация", style = MaterialTheme.typography.titleMedium)
                }
                Row {
                    IconButton(onClick = {
                        val textToCopy = buildString {
                            appendLine("=== Паспорт дня ===")
                            appendLine("Место проведения: ${day.location.ifBlank { "—" }}")
                            appendLine("Наименование объекта: ${day.objectName.ifBlank { "—" }}")
                            appendLine("Строительная организация: ${day.organization.ifBlank { "—" }}")
                            appendLine("Вид работ: ${day.workType.ifBlank { "—" }}")
                            appendLine("Наименование техпроцесса: ${day.processName.ifBlank { "—" }}")
                            appendLine("Документы: ${day.docsInfo.ifBlank { "—" }}")
                            appendLine("Бригада №${day.brigadeNumber.ifBlank { "—" }}")
                            appendLine("Бригадир: ${day.brigadeLeader.ifBlank { "—" }}")
                            appendLine()
                            appendLine("=== Операции ===")
                            if (operations.isEmpty()) {
                                appendLine("Нет операций")
                            } else {
                                operations.forEachIndexed { index, op ->
                                    appendLine("${index + 1}. ${op.name}")
                                    appendLine("Начало: ${SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date(op.startEpoch))}")
                                    appendLine("Окончание: ${op.stopEpoch?.let { SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date(it)) } ?: "—"}")
                                    val durationMillis = (op.stopEpoch ?: System.currentTimeMillis()) - op.startEpoch
                                    val hours = durationMillis / 1000 / 3600
                                    val minutes = (durationMillis / 1000 % 3600) / 60
                                    val seconds = (durationMillis / 1000) % 60
                                    appendLine("Продолжительность: %02d:%02d:%02d".format(hours, minutes, seconds))
                                    appendLine("Исполнители: ${if (op.workers.isNotBlank()) op.workers else "—"}")
                                    appendLine("Количество человек: ${op.people}")
                                    appendLine("Инструменты: ${if (op.tools.isNotBlank()) op.tools else "—"}")
                                    val eqFormatted = op.equipment.split(", ").joinToString(", ") { it.substringBefore("=") }
                                    appendLine("Техника: ${if (op.equipment.isNotBlank()) eqFormatted else "—"}")
                                    appendLine("Материалы: ${if (op.materials.isNotBlank()) op.materials else "—"}")
                                    appendLine("Заметки: ${if (op.notes.isNotBlank()) op.notes else "—"}")
                                    appendLine()
                                }
                            }
                        }
                        clipboardManager.setText(AnnotatedString(textToCopy))
                        scope.launch {
                            snackbarHostState.showSnackbar("Скопировано в буфер обмена")
                        }
                    }) { Icon(Icons.Outlined.ContentCopy, contentDescription = "Скопировать", modifier = Modifier.size(20.dp)) }
                    IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = "Редактировать", modifier = Modifier.size(20.dp)) }
                }
            }
            Spacer(Modifier.height(6.dp))
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = spring()),
                exit = shrinkVertically(animationSpec = spring())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    InfoRow("Место проведения", day.location)
                    InfoRow("Наименование объекта", day.objectName)
                    InfoRow("Строительная организация", day.organization)
                    InfoRow("Вид работ", day.workType)
                    InfoRow("Наименование техпроцесса", day.processName)
                    InfoRow("Документы", day.docsInfo)
                    InfoRow("Бригада №", day.brigadeNumber)
                    InfoRow("Бригадир", day.brigadeLeader)
                }
            }
        }
    }
}
