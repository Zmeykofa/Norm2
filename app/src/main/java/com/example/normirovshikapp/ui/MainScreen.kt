package com.example.normirovshikapp.ui

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.normirovshikapp.data.DayEntity
import com.example.normirovshikapp.data.OperationEntity
import com.example.normirovshikapp.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    // Состояния для управления днями
    var showAddDayDialog by remember { mutableStateOf(false) }
    var newDayName by remember { mutableStateOf("") }

    var editingDay by remember { mutableStateOf<DayEntity?>(null) }
    var editDayName by remember { mutableStateOf("") }
    var showEditDayDialog by remember { mutableStateOf(false) }

    var dayToDelete by remember { mutableStateOf<DayEntity?>(null) }
    var showDeleteDayDialog by remember { mutableStateOf(false) }

    // Паспорт дня
    val isFirstDayCreated by viewModel.isFirstDayCreated.collectAsState()
    var showEditDayInfoDialog by remember { mutableStateOf(false) }

    // Операции
    val days by viewModel.days.collectAsState()
    val currentDay by viewModel.currentDay.collectAsState()
    val operations by viewModel.operations.collectAsState()
    var editingOperation by remember { mutableStateOf<OperationEntity?>(null) }
    var showEditOperationDialog by remember { mutableStateOf(false) }

    // Удаление операции
    var operationToDelete by remember { mutableStateOf<OperationEntity?>(null) }
    var showDeleteOperationDialog by remember { mutableStateOf(false) }

    // --- Незавершённые операции ---
    val unfinished by viewModel.unfinishedOperations.collectAsState()
    val currentIndex by viewModel.currentUnfinishedIndex.collectAsState()
    val listState = rememberLazyListState()

    // ⚡ корректируем индекс при изменении списка
    LaunchedEffect(unfinished.size) {
        viewModel.adjustUnfinishedIndex()
    }

    // Автооткрытие паспорта дня при создании первого
    LaunchedEffect(isFirstDayCreated) {
        if (isFirstDayCreated) {
            showEditDayInfoDialog = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("📅 Список дней", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                LazyColumn {
                    items(days, key = { it.id }) { day: DayEntity ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = day.name,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.selectDay(day)
                                        scope.launch { drawerState.close() }
                                    }
                            )
                            Text("✏️", modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable {
                                    editingDay = day
                                    editDayName = day.name
                                    showEditDayDialog = true
                                })
                            Text("🗑️", modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable {
                                    dayToDelete = day
                                    showDeleteDayDialog = true
                                })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showAddDayDialog = true },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("➕ Добавить день")
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(currentDay?.name ?: "Загрузка...") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) { Text("📂") }
                    },
                    actions = {
                        if (unfinished.isNotEmpty()) {
                            Button(
                                onClick = {
                                    viewModel.nextUnfinished()
                                    val idx = viewModel.currentUnfinishedIndex.value
                                    if (idx in unfinished.indices) {
                                        val op = unfinished[idx]
                                        val indexInAll = operations.indexOfFirst { it.id == op.id }
                                        if (indexInAll >= 0) {
                                            scope.launch {
                                                listState.animateScrollToItem(indexInAll)
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp), // закруглённые углы
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .size(48.dp) // квадратная кнопка
                                    .padding(end = 8.dp)
                            ) {
                                Text("${currentIndex + 1}/${unfinished.size}")
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                if (currentDay != null) {
                    FloatingActionButton(
                        onClick = { viewModel.addOperation("Новая операция") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text("+")
                    }
                }
            }

        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                if (currentDay == null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Загружаем последний открытый день…")
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        DayInfoCard(
                            day = currentDay!!,
                            operations = operations,
                            onEdit = { showEditDayInfoDialog = true },
                            snackbarHostState = snackbarHostState,
                            scope = scope
                        )
                        Spacer(Modifier.height(8.dp))

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(operations, key = { it.id }) { op: OperationEntity ->
                                OperationItem(
                                    op = op,
                                    onStop = { viewModel.stopOperation(op) },
                                    onEdit = {
                                        editingOperation = op
                                        showEditOperationDialog = true
                                    },
                                    onSplit = { viewModel.splitOperation(op) },
                                    onDelete = {
                                        operationToDelete = op
                                        showDeleteOperationDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        // --- Диалоги ---
    if (showEditDayInfoDialog && currentDay != null) {
        EditDayInfoDialog(
            day = currentDay!!,
            onDismiss = { showEditDayInfoDialog = false },
            onSave = { updatedDay ->
                viewModel.updateDay(updatedDay)
                showEditDayInfoDialog = false
            }
        )
    }
    if (showEditOperationDialog && editingOperation != null) {
        EditOperationDialog(
            operation = editingOperation!!,
            onDismiss = { showEditOperationDialog = false },
            onSave = { updatedOp ->
                viewModel.updateOperation(updatedOp)
                showEditOperationDialog = false
            },
            viewModel = viewModel
        )
    }
    if (showDeleteOperationDialog && operationToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteOperationDialog = false
                operationToDelete = null
            },
            title = { Text("Удалить операцию") },
            text = { Text("Вы уверены, что хотите удалить операцию \"${operationToDelete!!.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteOperation(operationToDelete!!)
                    showDeleteOperationDialog = false
                    operationToDelete = null
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteOperationDialog = false
                    operationToDelete = null
                }) { Text("Отмена") }
            }
        )
    }
    if (showAddDayDialog) {
        AlertDialog(
            onDismissRequest = { showAddDayDialog = false },
            title = { Text("Новый день") },
            text = {
                OutlinedTextField(
                    value = newDayName,
                    onValueChange = { newDayName = it },
                    label = { Text("Название дня") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newDayName.isNotBlank()) {
                        viewModel.addDay(newDayName.trim())
                        newDayName = ""
                        showAddDayDialog = false
                        scope.launch { drawerState.close() }
                    }
                }) { Text("Добавить") }
            },
            dismissButton = { TextButton(onClick = { showAddDayDialog = false }) { Text("Отмена") } }
        )
    }
    if (showEditDayDialog && editingDay != null) {
        AlertDialog(
            onDismissRequest = { showEditDayDialog = false },
            title = { Text("Переименовать день") },
            text = {
                OutlinedTextField(
                    value = editDayName,
                    onValueChange = { editDayName = it },
                    label = { Text("Название дня") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val d = editingDay
                    if (d != null && editDayName.isNotBlank()) {
                        viewModel.updateDay(d.copy(name = editDayName.trim()))
                        showEditDayDialog = false
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDayDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showDeleteDayDialog && dayToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDayDialog = false },
            title = { Text("Удалить день") },
            text = { Text("Вы уверены, что хотите удалить день \"${dayToDelete!!.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteDay(dayToDelete!!)
                    showDeleteDayDialog = false
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDayDialog = false }) { Text("Отмена") }
            }
        )
    }
}


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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать операцию") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") }
                )
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

                Button(onClick = { showWorkersDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Исполнители: ${if (selectedWorkers.isEmpty()) "—" else selectedWorkers.joinToString()}")
                }
                Button(onClick = { showToolsDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Инструменты: ${if (selectedTools.isEmpty()) "—" else selectedTools.joinToString()}")
                }
                Button(onClick = { showEquipmentDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Техника: ${if (selectedEquipment.isEmpty()) "—" else selectedEquipment.joinToString()}")
                }
                Button(onClick = { showMaterialsDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Материалы: ${if (selectedMaterials.isEmpty()) "—" else selectedMaterials.joinToString()}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    operation.copy(
                        name = name,
                        people = people.toIntOrNull() ?: 0,
                        workers = selectedWorkers.joinToString(", "),
                        tools = selectedTools.joinToString(", "),
                        equipment = selectedEquipment.joinToString(", "),
                        materials = selectedMaterials.joinToString(", "),
                        notes = notes
                    )
                )
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
            onRemove = { viewModel.removeWorker(it) }
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
            onRemove = { viewModel.removeTool(it) }
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
            onRemove = { viewModel.removeEquipment(it) }
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
            onRemove = { viewModel.removeMaterial(it) }
        )
    }
}
@Composable
fun MultiSelectDialog(
    title: String,
    options: List<String>,
    selected: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit,
    onAddNew: (String) -> Unit,
    onUpdate: (old: String, new: String) -> Unit,
    onRemove: (String) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selected) }
    var newItem by remember { mutableStateOf("") }

    // Для редактирования
    var editingItem by remember { mutableStateOf<String?>(null) }
    var editingText by remember { mutableStateOf("") }

    // Для подтверждения удаления
    var deletingItem by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                // Список элементов
                options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(4.dp)
                    ) {
                        Checkbox(
                            checked = tempSelected.contains(option),
                            onCheckedChange = {
                                tempSelected = if (it) tempSelected + option else tempSelected - option
                            }
                        )
                        Text(option, modifier = Modifier.weight(1f))

                        IconButton(onClick = {
                            editingItem = option
                            editingText = option
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                        IconButton(onClick = { deletingItem = option }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Поле для добавления нового
                OutlinedTextField(
                    value = newItem,
                    onValueChange = { newItem = it },
                    label = { Text("Добавить новый") },
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (newItem.isNotBlank()) {
                            onAddNew(newItem.trim())
                            tempSelected = tempSelected + newItem.trim()
                            newItem = ""
                        }
                    },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("Добавить")
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
                OutlinedTextField(
                    value = editingText,
                    onValueChange = { editingText = it },
                    label = { Text("Новое значение") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editingText.isNotBlank()) {
                        onUpdate(editingItem!!, editingText.trim())

                        // обновляем выбранные
                        tempSelected = tempSelected
                            .minus(editingItem!!)   // убираем старое
                            .plus(editingText.trim()) // добавляем новое

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


@Composable
fun DayInfoCard(
    day: DayEntity,
    operations: List<OperationEntity>,
    onEdit: () -> Unit,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,     // фон карточки
            contentColor = MaterialTheme.colorScheme.onSurface      // текст/иконки
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Общая информация", style = MaterialTheme.typography.titleMedium)
                Row {
                    Text("📋", modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable {
                            val textToCopy = buildString {
                                // Паспорт дня
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
                                // Операции
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
                                        appendLine("Техника: ${if (op.equipment.isNotBlank()) op.equipment else "—"}")
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
                        }
                    )
                    Text("✏️", modifier = Modifier.clickable { onEdit() })
                }
            }
            // Отображение паспорта дня в UI
            Text("Место проведения: ${day.location.ifBlank { "—" }}")
            Text("Наименование объекта: ${day.objectName.ifBlank { "—" }}")
            Text("Строительная организация: ${day.organization.ifBlank { "—" }}")
            Text("Вид работ: ${day.workType.ifBlank { "—" }}")
            Text("Наименование техпроцесса: ${day.processName.ifBlank { "—" }}")
            Text("Документы: ${day.docsInfo.ifBlank { "—" }}")
            Text("Бригада №${day.brigadeNumber.ifBlank { "—" }}")
            Text("Бригадир: ${day.brigadeLeader.ifBlank { "—" }}")
        }
    }
}




@Composable
fun EditDayInfoDialog(day: DayEntity, onDismiss: () -> Unit, onSave: (DayEntity) -> Unit) {
    var location by remember { mutableStateOf(day.location) }
    var objectName by remember { mutableStateOf(day.objectName) }
    var organization by remember { mutableStateOf(day.organization) }
    var workType by remember { mutableStateOf(day.workType) }
    var processName by remember { mutableStateOf(day.processName) }
    var docsInfo by remember { mutableStateOf(day.docsInfo) }
    var brigadeNumber by remember { mutableStateOf(day.brigadeNumber) }
    var brigadeLeader by remember { mutableStateOf(day.brigadeLeader) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать общую информацию") },
        text = {
            Column {
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Место проведения") })
                OutlinedTextField(value = objectName, onValueChange = { objectName = it }, label = { Text("Наименование объекта") })
                OutlinedTextField(value = organization, onValueChange = { organization = it }, label = { Text("Строительная организация") })
                OutlinedTextField(value = workType, onValueChange = { workType = it }, label = { Text("Вид работ") })
                OutlinedTextField(value = processName, onValueChange = { processName = it }, label = { Text("Наименование техпроцесса") })
                OutlinedTextField(value = docsInfo, onValueChange = { docsInfo = it }, label = { Text("Документы") })
                OutlinedTextField(value = brigadeNumber, onValueChange = { brigadeNumber = it }, label = { Text("Бригада №") })
                OutlinedTextField(value = brigadeLeader, onValueChange = { brigadeLeader = it }, label = { Text("Бригадир") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    day.copy(
                        location = location,
                        objectName = objectName,
                        organization = organization,
                        workType = workType,
                        processName = processName,
                        docsInfo = docsInfo,
                        brigadeNumber = brigadeNumber,
                        brigadeLeader = brigadeLeader
                    )
                )
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
fun OperationItem(
    op: OperationEntity,
    onStop: () -> Unit,
    onEdit: () -> Unit,
    onSplit: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(op.stopEpoch) {
        if (op.stopEpoch == null) {
            while (true) {
                now = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    val durationMillis = (op.stopEpoch ?: now) - op.startEpoch
    val durationSeconds = durationMillis / 1000
    val hours = durationSeconds / 3600
    val minutes = (durationSeconds % 3600) / 60
    val seconds = durationSeconds % 60
    val durationFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,     // ⚡ фон карточки
            contentColor = MaterialTheme.colorScheme.onSurface      // ⚡ текст/иконки
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Заголовок с названием и иконками
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Операция: ${op.name}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "✏️",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { onEdit() }
                    )
                    Text(
                        "🗑️",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { onDelete() }
                    )
                }
            }

            // Остальная информация
            Text("Начало: ${sdf.format(Date(op.startEpoch))}")
            Text("Окончание: ${op.stopEpoch?.let { sdf.format(Date(it)) } ?: "—"}")
            Text("Продолжительность: $durationFormatted")
            Text("Исполнители: ${if (op.workers.isNotBlank()) op.workers else "—"}")
            Text("Количество человек: ${op.people}")
            Text("Инструменты: ${if (op.tools.isNotBlank()) op.tools else "—"}")
            Text("Техника: ${if (op.equipment.isNotBlank()) op.equipment else "—"}")
            Text("Материалы: ${if (op.materials.isNotBlank()) op.materials else "—"}")
            Text("Заметки: ${if (op.notes.isNotBlank()) op.notes else "—"}")

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
                        Text("⏹ Остановить")
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
                        Text("🔄 Завершить и новая")
                    }
                }
            }
        }
    }
}
