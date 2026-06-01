package com.example.normirovshikapp.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.normirovshikapp.data.EquipmentEntity
import com.example.normirovshikapp.data.MaterialEntity
import com.example.normirovshikapp.data.StaffEntity
import com.example.normirovshikapp.data.ToolEntity

// ─────────────────────────────────────────────────────────────────────────────
// Диалог выбора/управления Исполнителями (StaffEntity)
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StaffMultiSelectDialog(
    title: String,
    staff: List<StaffEntity>,
    selected: Set<Int>,               // Set<StaffEntity.id>
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit,
    onAdd: (name: String, position: String, grade: String) -> Unit,
    onUpdate: (StaffEntity) -> Unit,
    onDelete: (StaffEntity) -> Unit,
    onMove: (StaffEntity, Int) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selected) }
    var newName by remember { mutableStateOf("") }
    var newPosition by remember { mutableStateOf("") }
    var newGrade by remember { mutableStateOf("") }
    var editingStaff by remember { mutableStateOf<StaffEntity?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPosition by remember { mutableStateOf("") }
    var editGrade by remember { mutableStateOf("") }
    var deletingStaff by remember { mutableStateOf<StaffEntity?>(null) }
    var expandedMenuId by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                ) {
                    items(staff, key = { it.id }) { person ->
                        val index = staff.indexOf(person)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onLongClick = { expandedMenuId = person.id },
                                        onClick = {
                                            tempSelected = if (tempSelected.contains(person.id))
                                                tempSelected - person.id
                                            else
                                                tempSelected + person.id
                                        }
                                    )
                                    .padding(4.dp)
                            ) {
                                Checkbox(
                                    checked = tempSelected.contains(person.id),
                                    onCheckedChange = {
                                        tempSelected = if (it) tempSelected + person.id else tempSelected - person.id
                                    }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    val mainText = if (person.name.isNotBlank()) person.name else person.position
                                    val subText = if (person.name.isNotBlank()) {
                                        listOfNotNull(
                                            person.position.takeIf { it.isNotBlank() },
                                            person.grade.takeIf { it.isNotBlank() }
                                        ).joinToString(", ")
                                    } else {
                                        person.grade
                                    }
                                    Text(mainText, style = MaterialTheme.typography.bodyMedium)
                                    if (subText.isNotBlank()) {
                                        Text(subText, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                IconButton(onClick = {
                                    editingStaff = person
                                    editName = person.name
                                    editPosition = person.position
                                    editGrade = person.grade
                                }) {
                                    Icon(Icons.Outlined.Edit, contentDescription = "Редактировать", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { deletingStaff = person }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Удалить", modifier = Modifier.size(18.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = expandedMenuId == person.id,
                                onDismissRequest = { expandedMenuId = null }
                            ) {
                                if (index > 0) {
                                    DropdownMenuItem(
                                        text = { Text("Переместить выше") },
                                        onClick = { onMove(person, -1); expandedMenuId = null },
                                        leadingIcon = { Icon(Icons.Outlined.ArrowUpward, null, modifier = Modifier.size(20.dp)) }
                                    )
                                }
                                if (index < staff.size - 1) {
                                    DropdownMenuItem(
                                        text = { Text("Переместить ниже") },
                                        onClick = { onMove(person, 1); expandedMenuId = null },
                                        leadingIcon = { Icon(Icons.Outlined.ArrowDownward, null, modifier = Modifier.size(20.dp)) }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                // Форма добавления
                OutlinedTextField(
                    value = newPosition,
                    onValueChange = { newPosition = it },
                    label = { Text("Должность") },
                    singleLine = true,
                    colors = gazTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newGrade,
                    onValueChange = { newGrade = it },
                    label = { Text("Разряд") },
                    singleLine = true,
                    colors = gazTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("ФИО / Имя") },
                    singleLine = true,
                    colors = gazTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
                Button(
                    onClick = {
                        if (newPosition.isNotBlank()) {
                            onAdd(newName.trim(), newPosition.trim(), newGrade.trim())
                            newName = ""; newPosition = ""; newGrade = ""
                        }
                    },
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
                ) { Text("Добавить") }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(tempSelected) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )

    // Диалог редактирования
    if (editingStaff != null) {
        AlertDialog(
            onDismissRequest = { editingStaff = null },
            title = { Text("Редактировать исполнителя") },
            text = {
                Column {
                    OutlinedTextField(value = editPosition, onValueChange = { editPosition = it }, label = { Text("Должность") }, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editGrade, onValueChange = { editGrade = it }, label = { Text("Разряд") }, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("ФИО / Имя") }, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editPosition.isNotBlank()) {
                        onUpdate(editingStaff!!.copy(name = editName.trim(), position = editPosition.trim(), grade = editGrade.trim()))
                        editingStaff = null
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { editingStaff = null }) { Text("Отмена") } }
        )
    }

    // Диалог удаления
    if (deletingStaff != null) {
        AlertDialog(
            onDismissRequest = { deletingStaff = null },
            title = { Text("Удалить исполнителя") },
            text = { Text("Вы уверены, что хотите удалить «${deletingStaff!!.displayName()}»?") },
            confirmButton = {
                TextButton(onClick = {
                    tempSelected = tempSelected - deletingStaff!!.id
                    onDelete(deletingStaff!!)
                    deletingStaff = null
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { deletingStaff = null }) { Text("Отмена") } }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Диалог выбора/управления Техникой (EquipmentEntity)
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EquipmentMultiSelectDialog(
    title: String,
    equipment: List<EquipmentEntity>,
    selected: Set<Int>,               // Set<EquipmentEntity.id>
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit,
    onAdd: (name: String, position: String, grade: String, machinist: String) -> Unit,
    onUpdate: (EquipmentEntity) -> Unit,
    onDelete: (EquipmentEntity) -> Unit,
    onMove: (EquipmentEntity, Int) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selected) }
    var newName by remember { mutableStateOf("") }
    var newPosition by remember { mutableStateOf("") }
    var newGrade by remember { mutableStateOf("") }
    var newMachinist by remember { mutableStateOf("") }
    var editingEq by remember { mutableStateOf<EquipmentEntity?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPosition by remember { mutableStateOf("") }
    var editGrade by remember { mutableStateOf("") }
    var editMachinist by remember { mutableStateOf("") }
    var deletingEq by remember { mutableStateOf<EquipmentEntity?>(null) }
    var expandedMenuId by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                ) {
                    items(equipment, key = { it.id }) { eq ->
                        val index = equipment.indexOf(eq)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onLongClick = { expandedMenuId = eq.id },
                                        onClick = {
                                            tempSelected = if (tempSelected.contains(eq.id))
                                                tempSelected - eq.id
                                            else
                                                tempSelected + eq.id
                                        }
                                    )
                                    .padding(4.dp)
                            ) {
                                Checkbox(
                                    checked = tempSelected.contains(eq.id),
                                    onCheckedChange = {
                                        tempSelected = if (it) tempSelected + eq.id else tempSelected - eq.id
                                    }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(eq.name, style = MaterialTheme.typography.bodyMedium)
                                    val details = listOfNotNull(
                                        eq.machinist.takeIf { it.isNotBlank() },
                                        eq.position.takeIf { it.isNotBlank() },
                                        eq.grade.takeIf { it.isNotBlank() }
                                    ).joinToString(", ")
                                    if (details.isNotBlank()) {
                                        Text(details, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                IconButton(onClick = {
                                    editingEq = eq
                                    editName = eq.name
                                    editPosition = eq.position
                                    editGrade = eq.grade
                                    editMachinist = eq.machinist
                                }) {
                                    Icon(Icons.Outlined.Edit, contentDescription = "Редактировать", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { deletingEq = eq }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Удалить", modifier = Modifier.size(18.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = expandedMenuId == eq.id,
                                onDismissRequest = { expandedMenuId = null }
                            ) {
                                if (index > 0) {
                                    DropdownMenuItem(
                                        text = { Text("Переместить выше") },
                                        onClick = { onMove(eq, -1); expandedMenuId = null },
                                        leadingIcon = { Icon(Icons.Outlined.ArrowUpward, null, modifier = Modifier.size(20.dp)) }
                                    )
                                }
                                if (index < equipment.size - 1) {
                                    DropdownMenuItem(
                                        text = { Text("Переместить ниже") },
                                        onClick = { onMove(eq, 1); expandedMenuId = null },
                                        leadingIcon = { Icon(Icons.Outlined.ArrowDownward, null, modifier = Modifier.size(20.dp)) }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Название техники") }, singleLine = true, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = newMachinist, onValueChange = { newMachinist = it }, label = { Text("Машинист (ФИО)") }, singleLine = true, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                OutlinedTextField(value = newPosition, onValueChange = { newPosition = it }, label = { Text("Должность машиниста") }, singleLine = true, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                OutlinedTextField(value = newGrade, onValueChange = { newGrade = it }, label = { Text("Разряд машиниста") }, singleLine = true, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onAdd(newName.trim(), newPosition.trim(), newGrade.trim(), newMachinist.trim())
                            newName = ""; newPosition = ""; newGrade = ""; newMachinist = ""
                        }
                    },
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
                ) { Text("Добавить") }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(tempSelected) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )

    if (editingEq != null) {
        AlertDialog(
            onDismissRequest = { editingEq = null },
            title = { Text("Редактировать технику") },
            text = {
                Column {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Название техники") }, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editMachinist, onValueChange = { editMachinist = it }, label = { Text("Машинист (ФИО)") }, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                    OutlinedTextField(value = editPosition, onValueChange = { editPosition = it }, label = { Text("Должность машиниста") }, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                    OutlinedTextField(value = editGrade, onValueChange = { editGrade = it }, label = { Text("Разряд машиниста") }, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editName.isNotBlank()) {
                        onUpdate(editingEq!!.copy(name = editName.trim(), position = editPosition.trim(), grade = editGrade.trim(), machinist = editMachinist.trim()))
                        editingEq = null
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { editingEq = null }) { Text("Отмена") } }
        )
    }

    if (deletingEq != null) {
        AlertDialog(
            onDismissRequest = { deletingEq = null },
            title = { Text("Удалить технику") },
            text = { Text("Вы уверены, что хотите удалить «${deletingEq!!.displayName()}»?") },
            confirmButton = {
                TextButton(onClick = {
                    tempSelected = tempSelected - deletingEq!!.id
                    onDelete(deletingEq!!)
                    deletingEq = null
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { deletingEq = null }) { Text("Отмена") } }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Простой диалог для Инструментов (ToolEntity)
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolMultiSelectDialog(
    title: String,
    tools: List<ToolEntity>,
    selected: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit,
    onAdd: (name: String) -> Unit,
    onUpdate: (ToolEntity) -> Unit,
    onDelete: (ToolEntity) -> Unit,
    onMove: (ToolEntity, Int) -> Unit
) {
    SimpleEntityDialog(
        title = title,
        items = tools,
        selectedIds = selected,
        getId = { it.id },
        getDisplayName = { it.name },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onAdd = { name -> onAdd(name) },
        onUpdate = { tool, name -> onUpdate(tool.copy(name = name)) },
        onDelete = onDelete,
        onMove = onMove
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Простой диалог для Материалов (MaterialEntity)
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MaterialMultiSelectDialog(
    title: String,
    materials: List<MaterialEntity>,
    selected: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit,
    onAdd: (name: String) -> Unit,
    onUpdate: (MaterialEntity) -> Unit,
    onDelete: (MaterialEntity) -> Unit,
    onMove: (MaterialEntity, Int) -> Unit
) {
    SimpleEntityDialog(
        title = title,
        items = materials,
        selectedIds = selected,
        getId = { it.id },
        getDisplayName = { it.name },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onAdd = { name -> onAdd(name) },
        onUpdate = { mat, name -> onUpdate(mat.copy(name = name)) },
        onDelete = onDelete,
        onMove = onMove
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Внутренний обобщённый диалог для простых Entity (Tool, Material)
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> SimpleEntityDialog(
    title: String,
    items: List<T>,
    selectedIds: Set<Int>,
    getId: (T) -> Int,
    getDisplayName: (T) -> String,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit,
    onAdd: (String) -> Unit,
    onUpdate: (T, String) -> Unit,
    onDelete: (T) -> Unit,
    onMove: (T, Int) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedIds) }
    var newText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<T?>(null) }
    var editText by remember { mutableStateOf("") }
    var deletingItem by remember { mutableStateOf<T?>(null) }
    var expandedMenuId by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false).fillMaxWidth()
                ) {
                    items(items.size) { index ->
                        val item = items[index]
                        val id = getId(item)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onLongClick = { expandedMenuId = id },
                                        onClick = {
                                            tempSelected = if (tempSelected.contains(id))
                                                tempSelected - id else tempSelected + id
                                        }
                                    )
                                    .padding(4.dp)
                            ) {
                                Checkbox(
                                    checked = tempSelected.contains(id),
                                    onCheckedChange = {
                                        tempSelected = if (it) tempSelected + id else tempSelected - id
                                    }
                                )
                                Text(getDisplayName(item), modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    editingItem = item
                                    editText = getDisplayName(item)
                                }) {
                                    Icon(Icons.Outlined.Edit, contentDescription = "Редактировать", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { deletingItem = item }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Удалить", modifier = Modifier.size(18.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = expandedMenuId == id,
                                onDismissRequest = { expandedMenuId = null }
                            ) {
                                if (index > 0) {
                                    DropdownMenuItem(
                                        text = { Text("Переместить выше") },
                                        onClick = { onMove(item, -1); expandedMenuId = null },
                                        leadingIcon = { Icon(Icons.Outlined.ArrowUpward, null, modifier = Modifier.size(20.dp)) }
                                    )
                                }
                                if (index < items.size - 1) {
                                    DropdownMenuItem(
                                        text = { Text("Переместить ниже") },
                                        onClick = { onMove(item, 1); expandedMenuId = null },
                                        leadingIcon = { Icon(Icons.Outlined.ArrowDownward, null, modifier = Modifier.size(20.dp)) }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newText,
                    onValueChange = { newText = it },
                    label = { Text("Добавить новый") },
                    singleLine = true,
                    colors = gazTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (newText.isNotBlank()) {
                            onAdd(newText.trim())
                            newText = ""
                        }
                    },
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
                ) { Text("Добавить") }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(tempSelected) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )

    if (editingItem != null) {
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Редактировать") },
            text = {
                OutlinedTextField(value = editText, onValueChange = { editText = it }, label = { Text("Новое значение") }, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editText.isNotBlank()) {
                        onUpdate(editingItem!!, editText.trim())
                        editingItem = null
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { editingItem = null }) { Text("Отмена") } }
        )
    }

    if (deletingItem != null) {
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text("Удалить элемент") },
            text = { Text("Вы уверены, что хотите удалить «${getDisplayName(deletingItem!!)}»?") },
            confirmButton = {
                TextButton(onClick = {
                    tempSelected = tempSelected - getId(deletingItem!!)
                    onDelete(deletingItem!!)
                    deletingItem = null
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { deletingItem = null }) { Text("Отмена") } }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Старый MultiSelectDialog — оставляем для совместимости с шаблонами операций
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MultiSelectDialog(
    title: String,
    options: List<String>,
    selected: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
    onAddNew: (String) -> Unit,
    onUpdate: (old: String, new: String) -> Unit,
    onRemove: (String) -> Unit,
    onMove: ((String, Int) -> Unit)? = null
) {
    var tempSelected by remember { mutableStateOf(selected) }
    var newItemText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }
    var deletingItem by remember { mutableStateOf<String?>(null) }
    var expandedMenuIndex by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                LazyColumn(modifier = Modifier.weight(1f, fill = false).fillMaxWidth()) {
                    items(options.size) { index ->
                        val option = options[index]
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onLongClick = { expandedMenuIndex = index },
                                        onClick = {
                                            tempSelected = if (tempSelected.contains(option))
                                                tempSelected - option else tempSelected + option
                                        }
                                    )
                                    .padding(4.dp)
                            ) {
                                Checkbox(
                                    checked = tempSelected.contains(option),
                                    onCheckedChange = {
                                        tempSelected = if (it) tempSelected + option else tempSelected - option
                                    }
                                )
                                Text(option, modifier = Modifier.weight(1f))
                                IconButton(onClick = { editingItem = option; editingText = option }) {
                                    Icon(Icons.Outlined.Edit, contentDescription = "Редактировать", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { deletingItem = option }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Удалить", modifier = Modifier.size(18.dp))
                                }
                            }
                            if (onMove != null) {
                                DropdownMenu(expanded = expandedMenuIndex == index, onDismissRequest = { expandedMenuIndex = null }) {
                                    if (index > 0) DropdownMenuItem(text = { Text("Переместить выше") }, onClick = { onMove(option, -1); expandedMenuIndex = null }, leadingIcon = { Icon(Icons.Outlined.ArrowUpward, null, modifier = Modifier.size(20.dp)) })
                                    if (index < options.size - 1) DropdownMenuItem(text = { Text("Переместить ниже") }, onClick = { onMove(option, 1); expandedMenuIndex = null }, leadingIcon = { Icon(Icons.Outlined.ArrowDownward, null, modifier = Modifier.size(20.dp)) })
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = newItemText, onValueChange = { newItemText = it }, label = { Text("Добавить новый") }, singleLine = true, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth())
                Button(onClick = { if (newItemText.isNotBlank()) { onAddNew(newItemText.trim()); tempSelected = tempSelected + newItemText.trim(); newItemText = "" } }, modifier = Modifier.padding(top = 4.dp).fillMaxWidth()) { Text("Добавить") }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(tempSelected) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )

    if (editingItem != null) {
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Редактировать") },
            text = { OutlinedTextField(value = editingText, onValueChange = { editingText = it }, label = { Text("Новое значение") }, colors = gazTextFieldColors(), modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(onClick = { if (editingText.isNotBlank()) { onUpdate(editingItem!!, editingText.trim()); editingItem = null } }) { Text("Сохранить") } },
            dismissButton = { TextButton(onClick = { editingItem = null }) { Text("Отмена") } }
        )
    }

    if (deletingItem != null) {
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text("Удалить элемент") },
            text = { Text("Вы уверены, что хотите удалить «${deletingItem}»?") },
            confirmButton = { TextButton(onClick = { onRemove(deletingItem!!); tempSelected = tempSelected - deletingItem!!; deletingItem = null }) { Text("Удалить") } },
            dismissButton = { TextButton(onClick = { deletingItem = null }) { Text("Отмена") } }
        )
    }
}
