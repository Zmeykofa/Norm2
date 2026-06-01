package com.example.normirovshikapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import com.example.normirovshikapp.data.OperationEntity
import com.example.normirovshikapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOperationDialog(
    operation: OperationEntity,
    onDismiss: () -> Unit,
    onSave: (OperationEntity) -> Unit,
    viewModel: MainViewModel
) {
    // Справочники из ViewModel
    val workersList by viewModel.workersList.collectAsState()
    val toolsList by viewModel.toolsList.collectAsState()
    val equipmentList by viewModel.equipmentList.collectAsState()
    val materialsList by viewModel.materialsList.collectAsState()
    val operations by viewModel.operations.collectAsState()
    val templatesList by viewModel.operationTemplatesList.collectAsState()

    var showWarningDialog by remember { mutableStateOf(false) }
    var conflictingMsg by remember { mutableStateOf("") }
    var pendingOp by remember { mutableStateOf<OperationEntity?>(null) }

    // Состояния для редактируемой операции
    var name by remember { mutableStateOf(operation.name) }
    var people by remember { mutableStateOf(operation.people.toString()) }
    var notes by remember { mutableStateOf(operation.notes) }

    var selectedWorkers by remember { mutableStateOf(operation.workers.split(", ").filter { it.isNotBlank() }.toSet()) }
    var selectedTools by remember { mutableStateOf(operation.tools.split(", ").filter { it.isNotBlank() }.toSet()) }
    var selectedEquipment by remember { mutableStateOf(operation.equipment.split(", ").filter { it.isNotBlank() }.toSet()) }
    var selectedMaterials by remember { mutableStateOf(operation.materials.split(", ").filter { it.isNotBlank() }.toSet()) }

    // Флаги для открытия диалогов
    var showWorkersDialog by remember { mutableStateOf(false) }
    var showToolsDialog by remember { mutableStateOf(false) }
    var showEquipmentDialog by remember { mutableStateOf(false) }
    var showMaterialsDialog by remember { mutableStateOf(false) }
    var showTemplatesDialog by remember { mutableStateOf(false) }
    var isTemplatesDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать операцию") },
        text = {
            LazyColumn {
                item {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = isTemplatesDropdownExpanded,
                            onExpandedChange = { isTemplatesDropdownExpanded = !isTemplatesDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Название (или шаблон)") },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTemplatesDropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            if (templatesList.isNotEmpty()) {
                                ExposedDropdownMenu(
                                    expanded = isTemplatesDropdownExpanded,
                                    onDismissRequest = { isTemplatesDropdownExpanded = false }
                                ) {
                                    templatesList.forEach { template ->
                                        DropdownMenuItem(
                                            text = { Text(template) },
                                            onClick = {
                                                name = template
                                                isTemplatesDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        IconButton(onClick = { showTemplatesDialog = true }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Шаблоны операций")
                        }
                    }
                    OutlinedTextField(
                        value = people,
                        onValueChange = { people = it },
                        label = { Text("Количество человек") }
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Заметки") }
                    )

                    Spacer(Modifier.height(8.dp))

// TextOverflow import needed
                    Button(onClick = { showWorkersDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Исполнители: ${if (selectedWorkers.isEmpty()) "—" else selectedWorkers.joinToString()}",
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Button(onClick = { showToolsDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Инструменты: ${if (selectedTools.isEmpty()) "—" else selectedTools.joinToString()}",
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Button(onClick = { showEquipmentDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Техника: ${if (selectedEquipment.isEmpty()) "—" else selectedEquipment.joinToString { it.substringBefore("=") }}",
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Button(onClick = { showMaterialsDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Материалы: ${if (selectedMaterials.isEmpty()) "—" else selectedMaterials.joinToString()}",
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val updatedOp = operation.copy(
                    name = name,
                    people = people.toIntOrNull() ?: 0,
                    workers = selectedWorkers.joinToString(", "),
                    tools = selectedTools.joinToString(", "),
                    equipment = selectedEquipment.joinToString(", "),
                    materials = selectedMaterials.joinToString(", "),
                    machinists = "",
                    notes = notes
                )
                
                val start1 = operation.startEpoch
                val stop1 = operation.stopEpoch ?: Long.MAX_VALUE
                
                val overlaps = operations.filter { 
                    it.id != operation.id && it.startEpoch < stop1 && start1 < (it.stopEpoch ?: Long.MAX_VALUE) 
                }
                
                val conflicts = mutableListOf<String>()
                for (w in selectedWorkers) {
                    val busyIn = overlaps.filter { it.workers.split(",").map { s -> s.trim() }.contains(w.trim()) }
                    if (busyIn.isNotEmpty()) {
                        conflicts.add("\n- $w (в: ${busyIn.joinToString { it.name }})")
                    }
                }
                
                if (conflicts.isNotEmpty()) {
                    conflictingMsg = "Следующие работники уже заняты в это время в других операциях:${conflicts.joinToString("")}\n\nВсё равно сохранить?"
                    pendingOp = updatedOp
                    showWarningDialog = true
                } else {
                    onSave(updatedOp)
                }
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )

    // Диалоги выбора
    if (showWorkersDialog) {
        MultiSelectDialog(
            title = "Исполнители",
            options = workersList,
            selected = selectedWorkers,
            onDismiss = { showWorkersDialog = false },
            onConfirm = {
                selectedWorkers = it
                showWorkersDialog = false
            },
            onAddNew = { viewModel.addWorker(it) },
            onUpdate = { old, new -> viewModel.updateWorker(old, new) },
            onRemove = { viewModel.removeWorker(it) },
            onMove = { item, dir -> viewModel.moveWorker(item, dir) }
        )
    }
    if (showToolsDialog) {
        MultiSelectDialog(
            title = "Инструменты",
            options = toolsList,
            selected = selectedTools,
            onDismiss = { showToolsDialog = false },
            onConfirm = {
                selectedTools = it
                showToolsDialog = false
            },
            onAddNew = { viewModel.addTool(it) },
            onUpdate = { old, new -> viewModel.updateTool(old, new) },
            onRemove = { viewModel.removeTool(it) },
            onMove = { item, dir -> viewModel.moveTool(item, dir) }
        )
    }
    if (showEquipmentDialog) {
        MultiSelectDialog(
            title = "Техника",
            options = equipmentList,
            selected = selectedEquipment,
            onDismiss = { showEquipmentDialog = false },
            onConfirm = {
                selectedEquipment = it
                showEquipmentDialog = false
            },
            onAddNew = { viewModel.addEquipment(it) },
            onUpdate = { old, new -> viewModel.updateEquipment(old, new) },
            onRemove = { viewModel.removeEquipment(it) },
            onMove = { item, dir -> viewModel.moveEquipment(item, dir) },
            secondaryLabel = "Разряд / Имя машиниста",
            parseItem = { 
                val parts = it.split("=")
                parts[0] to (parts.getOrNull(1) ?: "")
            },
            formatItem = { a, b -> if (b.isBlank()) a else "$a=$b" },
            displayItem = { 
                val parts = it.split("=")
                if (parts.size > 1 && parts[1].isNotBlank()) "${parts[0]} (${parts[1]})" else parts[0]
            }
        )
    }
    if (showMaterialsDialog) {
        MultiSelectDialog(
            title = "Материалы",
            options = materialsList,
            selected = selectedMaterials,
            onDismiss = { showMaterialsDialog = false },
            onConfirm = {
                selectedMaterials = it
                showMaterialsDialog = false
            },
            onAddNew = { viewModel.addMaterial(it) },
            onUpdate = { old, new -> viewModel.updateMaterial(old, new) },
            onRemove = { viewModel.removeMaterial(it) },
            onMove = { item, dir -> viewModel.moveMaterial(item, dir) }
        )
    }

    if (showWarningDialog && pendingOp != null) {
        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            title = { Text("Конфликт работников") },
            text = { Text(conflictingMsg) },
            confirmButton = {
                TextButton(onClick = {
                    onSave(pendingOp!!)
                    showWarningDialog = false
                    pendingOp = null
                }) { Text("Всё равно сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { showWarningDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showTemplatesDialog) {
        MultiSelectDialog(
            title = "Шаблоны операций",
            options = templatesList,
            selected = emptySet(),
            onDismiss = { showTemplatesDialog = false },
            onConfirm = { showTemplatesDialog = false },
            onAddNew = { viewModel.addOperationTemplate(it) },
            onUpdate = { old, new -> viewModel.updateOperationTemplate(old, new) },
            onRemove = { viewModel.removeOperationTemplate(it) },
            onMove = { item, dir -> viewModel.moveOperationTemplate(item, dir) }
        )
    }
}
