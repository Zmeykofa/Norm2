package com.example.normirovshikapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    // Справочники из ViewModel (теперь типизированные Entity)
    val staffList by viewModel.staffList.collectAsState()
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

    // Выбранные исполнители по ID (восстанавливаем из строки workers)
    var selectedWorkerIds by remember {
        mutableStateOf(
            run {
                val names = operation.workers.split(Regex(",(?![^(]*\\))")).map { it.trim() }.filter { it.isNotBlank() }
                staffList.filter { s -> names.any { it == s.displayName() || it == s.name } }.map { it.id }.toSet()
            }
        )
    }

    // Выбранные инструменты по ID
    var selectedToolIds by remember {
        mutableStateOf(
            run {
                val names = operation.tools.split(", ").map { it.trim() }.filter { it.isNotBlank() }
                toolsList.filter { t -> names.contains(t.name) }.map { it.id }.toSet()
            }
        )
    }

    // Выбранная техника по ID
    var selectedEquipmentIds by remember {
        mutableStateOf(
            run {
                val names = operation.equipment.split(Regex(",(?![^\\[]*\\])")).map { it.trim() }.filter { it.isNotBlank() }
                equipmentList.filter { e -> names.any { it == e.displayName() || it == e.name } }.map { it.id }.toSet()
            }
        )
    }

    // Выбранные материалы по ID
    var selectedMaterialIds by remember {
        mutableStateOf(
            run {
                val names = operation.materials.split(", ").map { it.trim() }.filter { it.isNotBlank() }
                materialsList.filter { m -> names.contains(m.name) }.map { it.id }.toSet()
            }
        )
    }

    // Флаги для открытия диалогов
    var showWorkersDialog by remember { mutableStateOf(false) }
    var showToolsDialog by remember { mutableStateOf(false) }
    var showEquipmentDialog by remember { mutableStateOf(false) }
    var showMaterialsDialog by remember { mutableStateOf(false) }
    var showTemplatesDialog by remember { mutableStateOf(false) }
    var isTemplatesDropdownExpanded by remember { mutableStateOf(false) }

    // Отображаемые строки для кнопок
    val selectedWorkersText = staffList.filter { selectedWorkerIds.contains(it.id) }.joinToString { it.displayName() }
    val selectedToolsText = toolsList.filter { selectedToolIds.contains(it.id) }.joinToString { it.name }
    val selectedEquipmentText = equipmentList.filter { selectedEquipmentIds.contains(it.id) }.joinToString { it.displayName() }
    val selectedMaterialsText = materialsList.filter { selectedMaterialIds.contains(it.id) }.joinToString { it.name }

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
                                colors = gazTextFieldColors()
                            )
                            if (templatesList.isNotEmpty()) {
                                ExposedDropdownMenu(
                                    expanded = isTemplatesDropdownExpanded,
                                    onDismissRequest = { isTemplatesDropdownExpanded = false }
                                ) {
                                    templatesList.forEach { template ->
                                        DropdownMenuItem(
                                            text = { Text(template) },
                                            onClick = { name = template; isTemplatesDropdownExpanded = false }
                                        )
                                    }
                                }
                            }
                        }
                        IconButton(onClick = { showTemplatesDialog = true }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Шаблоны операций")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = people,
                        onValueChange = { people = it },
                        label = { Text("Количество человек") },
                        colors = gazTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Заметки") },
                        colors = gazTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(onClick = { showWorkersDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Исполнители: ${if (selectedWorkersText.isBlank()) "—" else selectedWorkersText}",
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Button(onClick = { showToolsDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Инструменты: ${if (selectedToolsText.isBlank()) "—" else selectedToolsText}",
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Button(onClick = { showEquipmentDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Техника: ${if (selectedEquipmentText.isBlank()) "—" else selectedEquipmentText}",
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Button(onClick = { showMaterialsDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Материалы: ${if (selectedMaterialsText.isBlank()) "—" else selectedMaterialsText}",
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val workersStr = staffList.filter { selectedWorkerIds.contains(it.id) }.joinToString(", ") { it.displayName() }
                val toolsStr = toolsList.filter { selectedToolIds.contains(it.id) }.joinToString(", ") { it.name }
                val equipmentStr = equipmentList.filter { selectedEquipmentIds.contains(it.id) }.joinToString(", ") { it.displayName() }
                val materialsStr = materialsList.filter { selectedMaterialIds.contains(it.id) }.joinToString(", ") { it.name }

                val updatedOp = operation.copy(
                    name = name,
                    people = people.toIntOrNull() ?: 0,
                    workers = workersStr,
                    tools = toolsStr,
                    equipment = equipmentStr,
                    materials = materialsStr,
                    machinists = "",
                    notes = notes
                )

                // Проверка конфликтов работников
                val start1 = operation.startEpoch
                val stop1 = operation.stopEpoch ?: Long.MAX_VALUE
                val overlaps = operations.filter {
                    it.id != operation.id && it.startEpoch < stop1 && start1 < (it.stopEpoch ?: Long.MAX_VALUE)
                }
                val selectedNames = staffList.filter { selectedWorkerIds.contains(it.id) }.map { it.displayName() }
                val conflicts = mutableListOf<String>()
                for (workerName in selectedNames) {
                    val busyIn = overlaps.filter { it.workers.split(Regex(",(?![^(]*\\))")).map { s -> s.trim() }.contains(workerName) }
                    if (busyIn.isNotEmpty()) {
                        conflicts.add("\n- $workerName (в: ${busyIn.joinToString { it.name }})")
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

    // Диалог выбора исполнителей
    if (showWorkersDialog) {
        StaffMultiSelectDialog(
            title = "Исполнители",
            staff = staffList,
            selected = selectedWorkerIds,
            onDismiss = { showWorkersDialog = false },
            onConfirm = { selectedWorkerIds = it; people = it.size.toString(); showWorkersDialog = false },
            onAdd = { n, p, g -> viewModel.addStaff(n, p, g) },
            onUpdate = { viewModel.updateStaff(it) },
            onDelete = { viewModel.deleteStaff(it) },
            onMove = { s, d -> viewModel.moveStaff(s, d) }
        )
    }

    // Диалог выбора инструментов
    if (showToolsDialog) {
        ToolMultiSelectDialog(
            title = "Инструменты",
            tools = toolsList,
            selected = selectedToolIds,
            onDismiss = { showToolsDialog = false },
            onConfirm = { selectedToolIds = it; showToolsDialog = false },
            onAdd = { viewModel.addTool(it) },
            onUpdate = { viewModel.updateTool(it) },
            onDelete = { viewModel.deleteTool(it) },
            onMove = { t, d -> viewModel.moveTool(t, d) }
        )
    }

    // Диалог выбора техники
    if (showEquipmentDialog) {
        EquipmentMultiSelectDialog(
            title = "Техника",
            equipment = equipmentList,
            selected = selectedEquipmentIds,
            onDismiss = { showEquipmentDialog = false },
            onConfirm = { selectedEquipmentIds = it; showEquipmentDialog = false },
            onAdd = { n, p, g, m -> viewModel.addEquipment(n, p, g, m) },
            onUpdate = { viewModel.updateEquipment(it) },
            onDelete = { viewModel.deleteEquipment(it) },
            onMove = { e, d -> viewModel.moveEquipment(e, d) }
        )
    }

    // Диалог выбора материалов
    if (showMaterialsDialog) {
        MaterialMultiSelectDialog(
            title = "Материалы",
            materials = materialsList,
            selected = selectedMaterialIds,
            onDismiss = { showMaterialsDialog = false },
            onConfirm = { selectedMaterialIds = it; showMaterialsDialog = false },
            onAdd = { viewModel.addMaterial(it) },
            onUpdate = { viewModel.updateMaterial(it) },
            onDelete = { viewModel.deleteMaterial(it) },
            onMove = { m, d -> viewModel.moveMaterial(m, d) }
        )
    }

    // Диалог предупреждения о конфликте
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
            dismissButton = { TextButton(onClick = { showWarningDialog = false }) { Text("Отмена") } }
        )
    }

    // Диалог шаблонов операций
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
