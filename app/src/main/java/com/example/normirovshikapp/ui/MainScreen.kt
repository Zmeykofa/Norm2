package com.example.normirovshikapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.normirovshikapp.data.DayEntity
import com.example.normirovshikapp.data.OperationEntity
import com.example.normirovshikapp.viewmodel.MainViewModel
import com.example.normirovshikapp.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material.icons.outlined.Workspaces
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.outlined.SaveAlt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

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
    val currentTime by viewModel.currentTime.collectAsState()
    var editingOperation by remember { mutableStateOf<OperationEntity?>(null) }
    var showEditOperationDialog by remember { mutableStateOf(false) }
    var isDayInfoExpanded by remember { mutableStateOf(true) }

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
                Text(
                    "📅 Список дней",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                Divider()
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(days, key = { it.id }) { day: DayEntity ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = day.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        viewModel.selectDay(day)
                                        scope.launch { drawerState.close() }
                                    }
                            )
                            IconButton(onClick = {
                                editingDay = day
                                editDayName = day.name
                                showEditDayDialog = true
                            }) { Icon(Icons.Outlined.Edit, contentDescription = "Переименовать день", modifier = Modifier.size(20.dp)) }
                            IconButton(onClick = {
                                dayToDelete = day
                                showDeleteDayDialog = true
                            }) { Icon(Icons.Outlined.Delete, contentDescription = "Удалить день", modifier = Modifier.size(20.dp)) }
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
                // Лаунчер для сохранения файла
                val exportLauncher = rememberLauncherForActivityResult(
                     contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                ) { uri ->
                     if (uri != null && currentDay != null) {
                         scope.launch {
                              try {
                                 val workbook = com.example.normirovshikapp.utils.ExcelExporter().generateExcelWorkbook(currentDay!!, operations)
                                 
                                 // Записываем в файл через ContentResolver
                                 
                                     val outputStream = context.contentResolver.openOutputStream(uri)
                                     outputStream?.use { workbook.write(it) }
                                     workbook.close()
                                     snackbarHostState.showSnackbar("Файл успешно сохранен")
                                 
                             } catch (e: Throwable) {
                                 e.printStackTrace()
                                 snackbarHostState.showSnackbar("Ошибка при сохранении: ${e.localizedMessage}")
                             }
                         }
                     }
                }
                


                // Переопределяем launcher внутри TopAppBar действия, но это некрасиво.
                // Лучше вынести exportLauncher наверх MainScreen, а здесь вызывать launch.
                // Но так как replace_file_content ограничен, сделаем хитро:
                // Мы не можем вынести launcher выше Scaffold без изменения всего файла.
                // ПОЭТОМУ: Я напишу launcher выше в следующем шаге, а здесь только кнопку
                
                TopAppBar(
                    title = { Text(currentDay?.name ?: "Загрузка...") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) { Icon(Icons.Outlined.Menu, contentDescription = "Меню дней", modifier = Modifier.size(22.dp)) }
                    },
                    actions = {
                        // Кнопка экспорта
                         if (currentDay != null) {
                             IconButton(onClick = {
                                 val dateStr = java.text.SimpleDateFormat("dd_MM_yyyy", java.util.Locale.getDefault()).format(java.util.Date(currentDay!!.createdAt))
                                 val fileName = "Otchet_${currentDay!!.name.replace(" ", "_")}_$dateStr.xlsx"
                                 exportLauncher.launch(fileName)
                             }) {
                                 Icon(Icons.Outlined.SaveAlt, contentDescription = "Сохранить в Excel")
                             }
                         }
                    
                        if (unfinished.isNotEmpty()) {
                            AssistChip(
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
                                label = { Text("${currentIndex + 1}/${unfinished.size}") },
                                leadingIcon = {
                                    Icon(Icons.Outlined.NavigateNext, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                )
            },
            floatingActionButton = {
                if (currentDay != null) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.addOperation("Новая операция") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Операция")
                    }
                }
            }

        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
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
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        DayInfoCard(
                            day = currentDay!!,
                            operations = operations,
                            onEdit = { showEditDayInfoDialog = true },
                            snackbarHostState = snackbarHostState,
                            scope = scope,
                            expanded = isDayInfoExpanded,
                            onToggleExpand = { isDayInfoExpanded = !isDayInfoExpanded }
                        )
                        Spacer(Modifier.height(12.dp))

                        if (operations.isEmpty()) {
                            EmptyOperationsState(onAdd = { viewModel.addOperation("Новая операция") })
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 96.dp)
                            ) {
                                items(operations, key = { it.id }) { op: OperationEntity ->
                                    OperationItem(
                                        op = op,
                                        currentTime = currentTime,
                                        onStop = { viewModel.stopOperation(op) },
                                        onEdit = {
                                            editingOperation = op
                                            showEditOperationDialog = true
                                        },
                                        onSplit = { viewModel.splitOperation(op) },
                                        onDelete = {
                                            operationToDelete = op
                                            showDeleteOperationDialog = true
                                        },
                                        onRepeat = { viewModel.repeatOperation(op) }
                                    )
                                }
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
        // Состояние для выбранного дня-источника (null значит "пустой")
        var selectedSourceDayId by remember { mutableStateOf<String?>(null) }
        var isDropdownExpanded by remember { mutableStateOf(false) }

        // Список дней для копирования (исключая текущий создаваемый, естественно)
        // Сортируем по убыванию даты, чтобы свежие были сверху
        val sourceDays = days.sortedByDescending { it.createdAt }

        AlertDialog(
            onDismissRequest = { showAddDayDialog = false },
            title = { Text("Новый день") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newDayName,
                        onValueChange = { newDayName = it },
                        label = { Text("Название дня") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Скопировать списки из:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = isDropdownExpanded,
                        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = sourceDays.find { it.id == selectedSourceDayId }?.name ?: "Не копировать (пустой)",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Не копировать (пустой)") },
                                onClick = {
                                    selectedSourceDayId = null
                                    isDropdownExpanded = false
                                }
                            )
                            sourceDays.forEach { day ->
                                DropdownMenuItem(
                                    text = { Text(day.name) },
                                    onClick = {
                                        selectedSourceDayId = day.id
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newDayName.isNotBlank()) {
                        viewModel.addDay(newDayName.trim(), selectedSourceDayId)
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
