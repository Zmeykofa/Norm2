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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import com.example.normirovshikapp.data.DayEntity
import com.example.normirovshikapp.data.OperationEntity
import com.example.normirovshikapp.viewmodel.MainViewModel
import com.example.normirovshikapp.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.background
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
    // Офлайн проверка даты для блокировки (31.08.2026)
    val isBlocked = remember {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) // 0-indexed, август = 7
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        currentYear > 2026 || 
        (currentYear == 2026 && currentMonth > Calendar.AUGUST) || 
        (currentYear == 2026 && currentMonth == Calendar.AUGUST && currentDay >= 31)
    }

    if (isBlocked) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.StopCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Срок действия версии истек",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Эта версия приложения была заблокирована 31.08.2026. Пожалуйста, обратитесь к администратору или обновите приложение до актуальной версии.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Код ошибки: EXP-20260831",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }
        }
        return
    }

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

    var showDirectoriesDialog by remember { mutableStateOf(false) }
    var activeDirectoryType by remember { mutableStateOf<String?>(null) } // "workers", "tools", "equipment", "materials", "templates"

    // Паспорт дня
    val isFirstDayCreated by viewModel.isFirstDayCreated.collectAsState()
    var showEditDayInfoDialog by remember { mutableStateOf(false) }

    // Операции
    val days by viewModel.days.collectAsState()
    val currentDay by viewModel.currentDay.collectAsState()
    val operations by viewModel.operations.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val staffList by viewModel.staffList.collectAsState()
    val toolsList by viewModel.toolsList.collectAsState()
    val equipmentList by viewModel.equipmentList.collectAsState()
    val materialsList by viewModel.materialsList.collectAsState()
    val templatesList by viewModel.operationTemplatesList.collectAsState()
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
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
                        Text(
                            "ХРОНОМЕТРАЖ РАБОТ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        )
                        Text(
                            "НОРМИРОВЩИК",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Система оперативного учёта v7.1",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "📅 Список рабочих дней",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(Modifier.height(6.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(days, key = { it.id }) { day: DayEntity ->
                        val isSelected = currentDay?.id == day.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                else
                                    Color.Transparent
                            ),
                            border = if (isSelected)
                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            else
                                null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectDay(day)
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "📅",
                                        modifier = Modifier.padding(end = 10.dp),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = day.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    IconButton(
                                        onClick = {
                                            editingDay = day
                                            editDayName = day.name
                                            showEditDayDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Edit,
                                            contentDescription = "Переименовать день",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            dayToDelete = day
                                            showDeleteDayDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = "Удалить день",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showAddDayDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    Text("➕ Добавить день")
                }
                OutlinedButton(
                    onClick = { showDirectoriesDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        .fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Workspaces, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("🗂️ Справочники БД")
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
                                  val workbook = com.example.normirovshikapp.utils.ExcelExporter().generateExcelWorkbook(
                                      day = currentDay!!,
                                      operations = operations,
                                      staffList = staffList,
                                      toolsList = toolsList,
                                      equipmentList = equipmentList,
                                      materialsList = materialsList
                                  )
                                 
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
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp, pressedElevation = 12.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Новая операция")
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
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = newDayName,
                        onValueChange = { newDayName = it },
                        label = { Text("Название дня") },
                        colors = gazTextFieldColors(),
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
                            colors = gazTextFieldColors(),
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
                    label = { Text("Название дня") },
                    colors = gazTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
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

    if (showDirectoriesDialog) {
        AlertDialog(
            onDismissRequest = { showDirectoriesDialog = false },
            title = { Text("🗂️ Справочники базы данных") },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text("Выберите справочник для редактирования:")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { activeDirectoryType = "workers" }, modifier = Modifier.fillMaxWidth()) {
                        Text("👥 Исполнители")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { activeDirectoryType = "tools" }, modifier = Modifier.fillMaxWidth()) {
                        Text("🔧 Инструменты")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { activeDirectoryType = "equipment" }, modifier = Modifier.fillMaxWidth()) {
                        Text("🚜 Спецтехника")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { activeDirectoryType = "materials" }, modifier = Modifier.fillMaxWidth()) {
                        Text("🧱 Материалы")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { activeDirectoryType = "templates" }, modifier = Modifier.fillMaxWidth()) {
                        Text("📋 Шаблоны операций")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDirectoriesDialog = false }) { Text("Закрыть") }
            }
        )
    }


    if (activeDirectoryType == "workers") {
        StaffMultiSelectDialog(
            title = "Справочник: Исполнители",
            staff = staffList,
            selected = emptySet(),
            onDismiss = { activeDirectoryType = null },
            onConfirm = { activeDirectoryType = null },
            onAdd = { n, p, g -> viewModel.addStaff(n, p, g) },
            onUpdate = { viewModel.updateStaff(it) },
            onDelete = { viewModel.deleteStaff(it) },
            onMove = { s, d -> viewModel.moveStaff(s, d) }
        )
    }
    if (activeDirectoryType == "tools") {
        ToolMultiSelectDialog(
            title = "Справочник: Инструменты",
            tools = toolsList,
            selected = emptySet(),
            onDismiss = { activeDirectoryType = null },
            onConfirm = { activeDirectoryType = null },
            onAdd = { viewModel.addTool(it) },
            onUpdate = { viewModel.updateTool(it) },
            onDelete = { viewModel.deleteTool(it) },
            onMove = { t, d -> viewModel.moveTool(t, d) }
        )
    }
    if (activeDirectoryType == "equipment") {
        EquipmentMultiSelectDialog(
            title = "Справочник: Спецтехника",
            equipment = equipmentList,
            selected = emptySet(),
            onDismiss = { activeDirectoryType = null },
            onConfirm = { activeDirectoryType = null },
            onAdd = { n, p, g, m -> viewModel.addEquipment(n, p, g, m) },
            onUpdate = { viewModel.updateEquipment(it) },
            onDelete = { viewModel.deleteEquipment(it) },
            onMove = { e, d -> viewModel.moveEquipment(e, d) }
        )
    }
    if (activeDirectoryType == "materials") {
        MaterialMultiSelectDialog(
            title = "Справочник: Материалы",
            materials = materialsList,
            selected = emptySet(),
            onDismiss = { activeDirectoryType = null },
            onConfirm = { activeDirectoryType = null },
            onAdd = { viewModel.addMaterial(it) },
            onUpdate = { viewModel.updateMaterial(it) },
            onDelete = { viewModel.deleteMaterial(it) },
            onMove = { m, d -> viewModel.moveMaterial(m, d) }
        )
    }
    if (activeDirectoryType == "templates") {
        MultiSelectDialog(
            title = "Справочник: Шаблоны операций",
            options = templatesList,
            selected = emptySet(),
            onDismiss = { activeDirectoryType = null },
            onConfirm = { activeDirectoryType = null },
            onAddNew = { viewModel.addOperationTemplate(it) },
            onUpdate = { old, new -> viewModel.updateOperationTemplate(old, new) },
            onRemove = { viewModel.removeOperationTemplate(it) },
            onMove = { item, dir -> viewModel.moveOperationTemplate(item, dir) }
        )
    }
}
