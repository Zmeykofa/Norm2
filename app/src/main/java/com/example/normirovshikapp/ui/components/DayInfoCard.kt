package com.example.normirovshikapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

    val isSystemDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (isSystemDark)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleExpand,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = if (expanded) "Свернуть" else "Развернуть",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Паспорт текущего дня",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
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
                                snackbarHostState.showSnackbar("Паспорт дня скопирован в буфер обмена")
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = "Скопировать",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Редактировать",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = spring()),
                exit = shrinkVertically(animationSpec = spring())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Группа 1: Объект и Локация
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("📍", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Локация и Объект",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Объект",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        day.objectName.ifBlank { "—" },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Место проведения",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        day.location.ifBlank { "—" },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Строительная организация",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                day.organization.ifBlank { "—" },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                            )
                        }
                    }

                    // Группа 2: Технологический процесс
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("⚙️", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Техпроцесс и Работы",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Вид работ",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                day.workType.ifBlank { "—" },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Наименование техпроцесса",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                day.processName.ifBlank { "—" },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Нормативные документы / Инструкции",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                day.docsInfo.ifBlank { "—" },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                            )
                        }
                    }

                    // Группа 3: Команда / Бригада
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("👥", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Бригада и Ответственные",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(
                                        "Производитель работ (Бригадир)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        day.brigadeLeader.ifBlank { "—" },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Бригада №",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        day.brigadeNumber.ifBlank { "—" },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
