package com.example.normirovshikapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    onMove: ((String, Int) -> Unit)? = null,
    secondaryLabel: String? = null,
    parseItem: (String) -> Pair<String, String> = { it to "" },
    formatItem: (String, String) -> String = { a, b -> if (b.isEmpty()) a else "$a=$b" },
    displayItem: (String) -> String = { it }
) {
    var tempSelected by remember { mutableStateOf(selected) }
    var newItemPrimary by remember { mutableStateOf("") }
    var newItemSecondary by remember { mutableStateOf("") }

    // Для редактирования
    var editingItem by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }
    var editingSecondary by remember { mutableStateOf("") }

    // Для подтверждения удаления
    var deletingItem by remember { mutableStateOf<String?>(null) }

    // Для меню сортировки (перемещение)
    var expandedMenuIndex by remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                // Список элементов (скроллится, занимает доступное место)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false) // fill=false важно, чтобы диалог не растягивался на весь экран, если элементов мало
                        .fillMaxWidth()
                ) {
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
                                            tempSelected = if (tempSelected.contains(option)) tempSelected - option else tempSelected + option
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
                                Text(displayItem(option), modifier = Modifier.weight(1f))

                                IconButton(onClick = {
                                    editingItem = option
                                    val parsed = parseItem(option)
                                    editingText = parsed.first
                                    editingSecondary = parsed.second
                                }) {
                                    Icon(Icons.Outlined.Edit, contentDescription = "Редактировать", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { deletingItem = option }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Удалить", modifier = Modifier.size(18.dp))
                                }
                            }

                            if (onMove != null) {
                                DropdownMenu(
                                    expanded = expandedMenuIndex == index,
                                    onDismissRequest = { expandedMenuIndex = null }
                                ) {
                                    if (index > 0) {
                                        DropdownMenuItem(
                                            text = { Text("Переместить выше") },
                                            onClick = {
                                                onMove(option, -1)
                                                expandedMenuIndex = null
                                            },
                                            leadingIcon = { Icon(Icons.Outlined.ArrowUpward, contentDescription = null, modifier = Modifier.size(20.dp)) }
                                        )
                                    }
                                    if (index < options.size - 1) {
                                        DropdownMenuItem(
                                            text = { Text("Переместить ниже") },
                                            onClick = {
                                                onMove(option, 1)
                                                expandedMenuIndex = null
                                            },
                                            leadingIcon = { Icon(Icons.Outlined.ArrowDownward, contentDescription = null, modifier = Modifier.size(20.dp)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))

                // Поле для добавления нового (всегда видно внизу)
                Column {
                    OutlinedTextField(
                        value = newItemPrimary,
                        onValueChange = { newItemPrimary = it },
                        label = { Text("Добавить новый") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (secondaryLabel != null) {
                        OutlinedTextField(
                            value = newItemSecondary,
                            onValueChange = { newItemSecondary = it },
                            label = { Text(secondaryLabel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        )
                    }
                    Button(
                        onClick = {
                            if (newItemPrimary.isNotBlank()) {
                                val formatted = formatItem(newItemPrimary.trim(), newItemSecondary.trim())
                                onAddNew(formatted)
                                tempSelected = tempSelected + formatted
                                newItemPrimary = ""
                                newItemSecondary = ""
                            }
                        },
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Text("Добавить")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(tempSelected) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )

    // Диалог редактирования
    if (editingItem != null) {
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Редактировать") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editingText,
                        onValueChange = { editingText = it },
                        label = { Text("Новое значение") }
                    )
                    if (secondaryLabel != null) {
                        OutlinedTextField(
                            value = editingSecondary,
                            onValueChange = { editingSecondary = it },
                            label = { Text(secondaryLabel) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editingText.isNotBlank()) {
                        val formatted = formatItem(editingText.trim(), editingSecondary.trim())
                        onUpdate(editingItem!!, formatted)

                        // обновляем выбранные
                        tempSelected = tempSelected
                            .minus(editingItem!!)   // убираем старое
                            .plus(formatted)        // добавляем новое

                        editingItem = null
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) { Text("Отмена") }
            }
        )
    }

    /// Диалог подтверждения удаления
    if (deletingItem != null) {
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text("Удалить элемент") },
            text = { Text("Вы уверены, что хотите удалить «${deletingItem}»?") },
            confirmButton = {
                TextButton(onClick = {
                    onRemove(deletingItem!!)
                    tempSelected = tempSelected - deletingItem!!
                    deletingItem = null
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) { Text("Отмена") }
            }
        )
    }
}
