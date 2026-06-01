package com.example.normirovshikapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.normirovshikapp.data.AppDao
import com.example.normirovshikapp.data.DayEntity
import com.example.normirovshikapp.data.EquipmentEntity
import com.example.normirovshikapp.data.MaterialEntity
import com.example.normirovshikapp.data.OperationEntity
import com.example.normirovshikapp.data.StaffEntity
import com.example.normirovshikapp.data.ToolEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(
    private val dao: AppDao,
    private val context: Context
) : ViewModel() {

    private val _days = MutableStateFlow<List<DayEntity>>(emptyList())
    val days: StateFlow<List<DayEntity>> = _days.asStateFlow()

    private val _currentDay = MutableStateFlow<DayEntity?>(null)
    val currentDay: StateFlow<DayEntity?> = _currentDay.asStateFlow()

    private val _operations = MutableStateFlow<List<OperationEntity>>(emptyList())
    val operations: StateFlow<List<OperationEntity>> = _operations.asStateFlow()

    private val _isFirstDayCreated = MutableStateFlow(false)
    val isFirstDayCreated: StateFlow<Boolean> = _isFirstDayCreated.asStateFlow()

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    private var operationsJob: Job? = null

    // --- Справочники по дням (из отдельных таблиц Room с привязкой к dayId) ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val staffList: StateFlow<List<StaffEntity>> = currentDay
        .flatMapLatest { day ->
            if (day != null) dao.getStaffForDayFlow(day.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val toolsList: StateFlow<List<ToolEntity>> = currentDay
        .flatMapLatest { day ->
            if (day != null) dao.getToolsForDayFlow(day.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val equipmentList: StateFlow<List<EquipmentEntity>> = currentDay
        .flatMapLatest { day ->
            if (day != null) dao.getEquipmentForDayFlow(day.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val materialsList: StateFlow<List<MaterialEntity>> = currentDay
        .flatMapLatest { day ->
            if (day != null) dao.getMaterialsForDayFlow(day.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Шаблоны операций остаются в DayEntity (только для текущего дня)
    private val _operationTemplatesList = MutableStateFlow<List<String>>(emptyList())
    val operationTemplatesList: StateFlow<List<String>> = _operationTemplatesList.asStateFlow()

    // --- CRUD: Исполнители ---
    fun addStaff(name: String, position: String = "", grade: String = "") {
        val day = _currentDay.value ?: return
        if (position.isBlank()) return
        viewModelScope.launch {
            val maxOrder = dao.getMaxStaffSortOrder(day.id) ?: -1
            dao.insertStaff(
                StaffEntity(
                    name = name,
                    position = position,
                    grade = grade,
                    dayId = day.id,
                    sortOrder = maxOrder + 1
                )
            )
        }
    }

    fun updateStaff(staff: StaffEntity) {
        viewModelScope.launch {
            val oldStaff = staffList.value.find { it.id == staff.id }
            dao.updateStaff(staff)
            if (oldStaff != null) {
                val oldDisplayName = oldStaff.displayName()
                val newDisplayName = staff.displayName()
                if (oldDisplayName != newDisplayName) {
                    val day = _currentDay.value
                    if (day != null) {
                        val operations = dao.getOperationsForDayOnce(day.id)
                        operations.forEach { op ->
                            if (op.workers.isNotBlank()) {
                                val oldParts = op.workers.split(Regex(",(?![^(]*\\))")).map { it.trim() }
                                if (oldParts.contains(oldDisplayName) || oldParts.contains(oldStaff.name) || (oldStaff.name.isBlank() && oldParts.contains(oldStaff.position))) {
                                    val newWorkers = oldParts.map { part ->
                                        if (part == oldDisplayName || part == oldStaff.name || (oldStaff.name.isBlank() && part == oldStaff.position)) {
                                            newDisplayName
                                        } else {
                                            part
                                        }
                                    }.joinToString(", ")
                                    dao.updateOperation(op.copy(workers = newWorkers))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun deleteStaff(staff: StaffEntity) {
        viewModelScope.launch {
            dao.deleteStaff(staff)
            val displayName = staff.displayName()
            val day = _currentDay.value
            if (day != null) {
                val operations = dao.getOperationsForDayOnce(day.id)
                operations.forEach { op ->
                    if (op.workers.isNotBlank()) {
                        val oldParts = op.workers.split(Regex(",(?![^(]*\\))")).map { it.trim() }
                        if (oldParts.contains(displayName) || oldParts.contains(staff.name) || (staff.name.isBlank() && oldParts.contains(staff.position))) {
                            val newWorkers = oldParts.filter { it != displayName && it != staff.name && !(staff.name.isBlank() && it == staff.position) }.joinToString(", ")
                            dao.updateOperation(op.copy(workers = newWorkers))
                        }
                    }
                }
            }
        }
    }

    fun moveStaff(staff: StaffEntity, direction: Int) {
        viewModelScope.launch {
            val list = staffList.value.toMutableList()
            val index = list.indexOfFirst { it.id == staff.id }
            if (index == -1) return@launch
            val newIndex = (index + direction).coerceIn(0, list.size - 1)
            if (newIndex == index) return@launch
            // Swap sortOrder values
            val swapWith = list[newIndex]
            dao.updateStaff(staff.copy(sortOrder = swapWith.sortOrder))
            dao.updateStaff(swapWith.copy(sortOrder = staff.sortOrder))
        }
    }

    // --- CRUD: Инструменты ---
    fun addTool(name: String) {
        val day = _currentDay.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            val maxOrder = dao.getMaxToolSortOrder(day.id) ?: -1
            dao.insertTool(ToolEntity(name = name, dayId = day.id, sortOrder = maxOrder + 1))
        }
    }

    fun updateTool(tool: ToolEntity) {
        viewModelScope.launch {
            val oldTool = toolsList.value.find { it.id == tool.id }
            dao.updateTool(tool)
            if (oldTool != null && oldTool.name != tool.name) {
                val day = _currentDay.value
                if (day != null) {
                    val operations = dao.getOperationsForDayOnce(day.id)
                    operations.forEach { op ->
                        if (op.tools.isNotBlank()) {
                            val oldParts = op.tools.split(", ").map { it.trim() }
                            if (oldParts.contains(oldTool.name)) {
                                val newTools = oldParts.map { part ->
                                    if (part == oldTool.name) tool.name else part
                                }.joinToString(", ")
                                dao.updateOperation(op.copy(tools = newTools))
                            }
                        }
                    }
                }
            }
        }
    }

    fun deleteTool(tool: ToolEntity) {
        viewModelScope.launch {
            dao.deleteTool(tool)
            val day = _currentDay.value
            if (day != null) {
                val operations = dao.getOperationsForDayOnce(day.id)
                operations.forEach { op ->
                    if (op.tools.isNotBlank()) {
                        val oldParts = op.tools.split(", ").map { it.trim() }
                        if (oldParts.contains(tool.name)) {
                            val newTools = oldParts.filter { it != tool.name }.joinToString(", ")
                            dao.updateOperation(op.copy(tools = newTools))
                        }
                    }
                }
            }
        }
    }

    fun moveTool(tool: ToolEntity, direction: Int) {
        viewModelScope.launch {
            val list = toolsList.value.toMutableList()
            val index = list.indexOfFirst { it.id == tool.id }
            if (index == -1) return@launch
            val newIndex = (index + direction).coerceIn(0, list.size - 1)
            if (newIndex == index) return@launch
            val swapWith = list[newIndex]
            dao.updateTool(tool.copy(sortOrder = swapWith.sortOrder))
            dao.updateTool(swapWith.copy(sortOrder = tool.sortOrder))
        }
    }

    // --- CRUD: Техника ---
    fun addEquipment(name: String, position: String = "", grade: String = "", machinist: String = "") {
        val day = _currentDay.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            val maxOrder = dao.getMaxEquipmentSortOrder(day.id) ?: -1
            dao.insertEquipment(
                EquipmentEntity(
                    name = name, position = position, grade = grade,
                    machinist = machinist, dayId = day.id, sortOrder = maxOrder + 1
                )
            )
        }
    }

    fun updateEquipment(equipment: EquipmentEntity) {
        viewModelScope.launch {
            val oldEq = equipmentList.value.find { it.id == equipment.id }
            dao.updateEquipment(equipment)
            if (oldEq != null) {
                val oldDisplayName = oldEq.displayName()
                val newDisplayName = equipment.displayName()
                if (oldDisplayName != newDisplayName) {
                    val day = _currentDay.value
                    if (day != null) {
                        val operations = dao.getOperationsForDayOnce(day.id)
                        operations.forEach { op ->
                            if (op.equipment.isNotBlank()) {
                                val oldParts = op.equipment.split(Regex(",(?![^\\[]*\\])")).map { it.trim() }
                                if (oldParts.contains(oldDisplayName) || oldParts.contains(oldEq.name)) {
                                    val newEq = oldParts.map { part ->
                                        if (part == oldDisplayName || part == oldEq.name) {
                                            newDisplayName
                                        } else {
                                            part
                                        }
                                    }.joinToString(", ")
                                    dao.updateOperation(op.copy(equipment = newEq))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun deleteEquipment(equipment: EquipmentEntity) {
        viewModelScope.launch {
            dao.deleteEquipment(equipment)
            val displayName = equipment.displayName()
            val day = _currentDay.value
            if (day != null) {
                val operations = dao.getOperationsForDayOnce(day.id)
                operations.forEach { op ->
                    if (op.equipment.isNotBlank()) {
                        val oldParts = op.equipment.split(Regex(",(?![^\\[]*\\])")).map { it.trim() }
                        if (oldParts.contains(displayName) || oldParts.contains(equipment.name)) {
                            val newEq = oldParts.filter { it != displayName && it != equipment.name }.joinToString(", ")
                            dao.updateOperation(op.copy(equipment = newEq))
                        }
                    }
                }
            }
        }
    }

    fun moveEquipment(equipment: EquipmentEntity, direction: Int) {
        viewModelScope.launch {
            val list = equipmentList.value.toMutableList()
            val index = list.indexOfFirst { it.id == equipment.id }
            if (index == -1) return@launch
            val newIndex = (index + direction).coerceIn(0, list.size - 1)
            if (newIndex == index) return@launch
            val swapWith = list[newIndex]
            dao.updateEquipment(equipment.copy(sortOrder = swapWith.sortOrder))
            dao.updateEquipment(swapWith.copy(sortOrder = equipment.sortOrder))
        }
    }

    // --- CRUD: Материалы ---
    fun addMaterial(name: String) {
        val day = _currentDay.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            val maxOrder = dao.getMaxMaterialSortOrder(day.id) ?: -1
            dao.insertMaterial(MaterialEntity(name = name, dayId = day.id, sortOrder = maxOrder + 1))
        }
    }

    fun updateMaterial(material: MaterialEntity) {
        viewModelScope.launch {
            val oldMat = materialsList.value.find { it.id == material.id }
            dao.updateMaterial(material)
            if (oldMat != null && oldMat.name != material.name) {
                val day = _currentDay.value
                if (day != null) {
                    val operations = dao.getOperationsForDayOnce(day.id)
                    operations.forEach { op ->
                        if (op.materials.isNotBlank()) {
                            val oldParts = op.materials.split(", ").map { it.trim() }
                            if (oldParts.contains(oldMat.name)) {
                                val newMaterials = oldParts.map { part ->
                                    if (part == oldMat.name) material.name else part
                                }.joinToString(", ")
                                dao.updateOperation(op.copy(materials = newMaterials))
                            }
                        }
                    }
                }
            }
        }
    }

    fun deleteMaterial(material: MaterialEntity) {
        viewModelScope.launch {
            dao.deleteMaterial(material)
            val day = _currentDay.value
            if (day != null) {
                val operations = dao.getOperationsForDayOnce(day.id)
                operations.forEach { op ->
                    if (op.materials.isNotBlank()) {
                        val oldParts = op.materials.split(", ").map { it.trim() }
                        if (oldParts.contains(material.name)) {
                            val newMaterials = oldParts.filter { it != material.name }.joinToString(", ")
                            dao.updateOperation(op.copy(materials = newMaterials))
                        }
                    }
                }
            }
        }
    }

    fun moveMaterial(material: MaterialEntity, direction: Int) {
        viewModelScope.launch {
            val list = materialsList.value.toMutableList()
            val index = list.indexOfFirst { it.id == material.id }
            if (index == -1) return@launch
            val newIndex = (index + direction).coerceIn(0, list.size - 1)
            if (newIndex == index) return@launch
            val swapWith = list[newIndex]
            dao.updateMaterial(material.copy(sortOrder = swapWith.sortOrder))
            dao.updateMaterial(swapWith.copy(sortOrder = material.sortOrder))
        }
    }

    // --- Шаблоны операций (остаются в DayEntity) ---
    fun addOperationTemplate(newTemp: String) {
        if (newTemp.isBlank() || _operationTemplatesList.value.contains(newTemp)) return
        _operationTemplatesList.value = listOf(newTemp) + _operationTemplatesList.value
        saveTemplatesToCurrentDay()
    }

    fun updateOperationTemplate(old: String, new: String) {
        _operationTemplatesList.value = _operationTemplatesList.value.map { if (it == old) new else it }
        saveTemplatesToCurrentDay()
    }

    fun removeOperationTemplate(item: String) {
        _operationTemplatesList.value = _operationTemplatesList.value - item
        saveTemplatesToCurrentDay()
    }

    fun moveOperationTemplate(item: String, direction: Int) {
        val list = _operationTemplatesList.value.toMutableList()
        val index = list.indexOf(item)
        if (index == -1) return
        val newIndex = index + direction
        if (newIndex in 0 until list.size) {
            list.removeAt(index)
            list.add(newIndex, item)
            _operationTemplatesList.value = list
            saveTemplatesToCurrentDay()
        }
    }

    private fun saveTemplatesToCurrentDay() {
        val day = _currentDay.value ?: return
        val updated = day.copy(operationTemplatesList = _operationTemplatesList.value.joinToString(","))
        updateDay(updated)
    }

    private fun loadTemplatesFromDay(day: DayEntity) {
        _operationTemplatesList.value = day.operationTemplatesList.split(",").filter { it.isNotBlank() }
    }

    // --- Инициализация ---
    init {
        viewModelScope.launch {
            dao.getAllDaysFlow().collect { list ->
                _days.value = list
            }
        }

        viewModelScope.launch {
            while (true) {
                _currentTime.value = System.currentTimeMillis()
                delay(1000)
            }
        }

        restoreLastDay()
    }

    private fun restoreLastDay() {
        viewModelScope.launch {
            val allDays = dao.getAllDaysOnce()
            if (allDays.isEmpty()) {
                val newDay = DayEntity(
                    id = UUID.randomUUID().toString(),
                    name = "Новый день",
                    createdAt = System.currentTimeMillis()
                )
                dao.insertDay(newDay)
                saveLastDayIdToPrefs(newDay.id)
                _currentDay.value = newDay
                _operations.value = emptyList()
                _isFirstDayCreated.value = true
            } else {
                // 1. Перенос глобальных записей из версии 6 (с пустой dayId = "") во все существующие дни
                val globalStaff = dao.getStaffForDayOnce("")
                val globalTools = dao.getToolsForDayOnce("")
                val globalEquipment = dao.getEquipmentForDayOnce("")
                val globalMaterials = dao.getMaterialsForDayOnce("")

                if (globalStaff.isNotEmpty() || globalTools.isNotEmpty() || globalEquipment.isNotEmpty() || globalMaterials.isNotEmpty()) {
                    allDays.forEach { day ->
                        val dayId = day.id
                        if (dao.getStaffForDayOnce(dayId).isEmpty()) {
                            globalStaff.forEach { dao.insertStaff(it.copy(id = 0, dayId = dayId)) }
                        }
                        if (dao.getToolsForDayOnce(dayId).isEmpty()) {
                            globalTools.forEach { dao.insertTool(it.copy(id = 0, dayId = dayId)) }
                        }
                        if (dao.getEquipmentForDayOnce(dayId).isEmpty()) {
                            globalEquipment.forEach { dao.insertEquipment(it.copy(id = 0, dayId = dayId)) }
                        }
                        if (dao.getMaterialsForDayOnce(dayId).isEmpty()) {
                            globalMaterials.forEach { dao.insertMaterial(it.copy(id = 0, dayId = dayId)) }
                        }
                    }

                    // Удаляем глобальные записи для чистоты
                    dao.deleteStaffForDay("")
                    dao.deleteToolsForDay("")
                    dao.deleteEquipmentForDay("")
                    dao.deleteMaterialsForDay("")
                }

                // 2. Перенос старых списков из DayEntity (версии 5 и ниже), если в новых таблицах для этого дня пусто
                allDays.forEach { day ->
                    migrateOldDayListsIfEmpty(day)
                }

                val lastDayId = loadLastDayIdFromPrefs()
                val day = if (lastDayId != null) dao.getDayById(lastDayId) else allDays.first()
                if (day != null) {
                    selectDay(day)
                }
            }
        }
    }

    private suspend fun migrateOldDayListsIfEmpty(day: DayEntity) {
        val dayId = day.id

        // 1. Исполнители (workersList)
        if (day.workersList.isNotBlank()) {
            val currentStaff = dao.getStaffForDayOnce(dayId)
            if (currentStaff.isEmpty()) {
                val workerStrings = day.workersList.split(Regex(",(?![^(]*\\))"))
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                
                workerStrings.forEachIndexed { index, w ->
                    var name = ""
                    var position = ""
                    var grade = ""

                    val regexParens = Regex("\\(([^)]+)\\)")
                    val matchResult = regexParens.find(w)
                    if (matchResult != null) {
                        name = w.substringBefore("(").trim()
                        val inner = matchResult.groupValues[1]
                        val details = inner.split(",").map { it.trim() }
                        if (details.size >= 2) {
                            position = details[0]
                            grade = details[1]
                        } else if (details.isNotEmpty()) {
                            val valStr = details[0]
                            if (valStr.any { it.isDigit() }) {
                                grade = valStr
                            } else {
                                position = valStr
                            }
                        }
                    } else {
                        if (w.contains(",")) {
                            val details = w.split(",").map { it.trim() }
                            position = details[0]
                            if (details.size > 1) {
                                grade = details[1]
                            }
                        } else {
                            val trimmed = w.trim()
                            if (trimmed.any { it.isDigit() }) {
                                grade = trimmed
                            } else if (trimmed.contains(" ") || trimmed.length > 15) {
                                name = trimmed
                            } else {
                                position = trimmed
                            }
                        }
                    }

                    dao.insertStaff(
                        StaffEntity(
                            name = name,
                            position = position,
                            grade = grade,
                            dayId = dayId,
                            sortOrder = index
                        )
                    )
                }
            }
        }

        // 2. Инструменты (toolsList)
        if (day.toolsList.isNotBlank()) {
            val currentTools = dao.getToolsForDayOnce(dayId)
            if (currentTools.isEmpty()) {
                val toolStrings = day.toolsList.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                
                toolStrings.forEachIndexed { index, t ->
                    dao.insertTool(
                        ToolEntity(
                            name = t,
                            dayId = dayId,
                            sortOrder = index
                        )
                    )
                }
            }
        }

        // 3. Техника (equipmentList)
        if (day.equipmentList.isNotBlank()) {
            val currentEq = dao.getEquipmentForDayOnce(dayId)
            if (currentEq.isEmpty()) {
                val eqStrings = day.equipmentList.split(Regex(",(?![^\\[]*\\])"))
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                
                eqStrings.forEachIndexed { index, e ->
                    val name = e.substringBefore(" [").substringBefore("=").trim()
                    var machinist = ""
                    var position = ""
                    var grade = ""

                    val regexParens = Regex("\\[([^\\]]+)\\]")
                    val matchResult = regexParens.find(e)
                    if (matchResult != null) {
                        val inner = matchResult.groupValues[1]
                        val details = inner.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        
                        val isFio = { str: String ->
                            str.contains(Regex("[А-ЯA-Z]\\.[А-ЯA-Z]?\\.")) || (str.split(" ").size > 1 && str.any { it.isUpperCase() })
                        }

                        if (details.size >= 3) {
                            machinist = details[0]
                            position = details[1]
                            grade = details[2]
                        } else if (details.size == 2) {
                            val hasDigit0 = details[0].any { it.isDigit() }
                            val hasDigit1 = details[1].any { it.isDigit() }
                            if (hasDigit0) {
                                grade = details[0]
                                if (isFio(details[1])) machinist = details[1] else position = details[1]
                            } else if (hasDigit1) {
                                grade = details[1]
                                if (isFio(details[0])) machinist = details[0] else position = details[0]
                            } else {
                                if (isFio(details[0])) machinist = details[0] else position = details[0]
                                if (isFio(details[1])) machinist = details[1] else position = details[1]
                            }
                        } else if (details.size == 1) {
                            val valStr = details[0]
                            if (valStr.any { it.isDigit() }) {
                                grade = valStr
                            } else if (isFio(valStr)) {
                                machinist = valStr
                            } else {
                                position = valStr
                            }
                        }
                    }

                    dao.insertEquipment(
                        EquipmentEntity(
                            name = name,
                            position = position,
                            grade = grade,
                            machinist = machinist,
                            dayId = dayId,
                            sortOrder = index
                        )
                    )
                }
            }
        }

        // 4. Материалы (materialsList)
        if (day.materialsList.isNotBlank()) {
            val currentMats = dao.getMaterialsForDayOnce(dayId)
            if (currentMats.isEmpty()) {
                val matStrings = day.materialsList.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                
                matStrings.forEachIndexed { index, m ->
                    dao.insertMaterial(
                        MaterialEntity(
                            name = m,
                            dayId = dayId,
                            sortOrder = index
                        )
                    )
                }
            }
        }
    }

    fun selectDay(day: DayEntity) {
        _currentDay.value = day
        saveLastDayIdToPrefs(day.id)
        loadTemplatesFromDay(day)

        operationsJob?.cancel()
        operationsJob = viewModelScope.launch {
            dao.getOperationsForDayFlow(day.id).collect { list ->
                _operations.value = list
            }
        }
    }

    fun addDay(name: String, sourceDayId: String? = null) {
        viewModelScope.launch {
            val sourceDay = if (sourceDayId != null) dao.getDayById(sourceDayId) else null
            val newDayId = UUID.randomUUID().toString()
            val newDay = DayEntity(
                id = newDayId,
                name = name,
                createdAt = System.currentTimeMillis(),
                operationTemplatesList = sourceDay?.operationTemplatesList ?: ""
            )
            dao.insertDay(newDay)

            // Копируем списки справочников, если выбран исходный день
            if (sourceDayId != null) {
                val staffToCopy = dao.getStaffForDayOnce(sourceDayId)
                staffToCopy.forEach {
                    dao.insertStaff(it.copy(id = 0, dayId = newDayId))
                }

                val toolsToCopy = dao.getToolsForDayOnce(sourceDayId)
                toolsToCopy.forEach {
                    dao.insertTool(it.copy(id = 0, dayId = newDayId))
                }

                val eqToCopy = dao.getEquipmentForDayOnce(sourceDayId)
                eqToCopy.forEach {
                    dao.insertEquipment(it.copy(id = 0, dayId = newDayId))
                }

                val matsToCopy = dao.getMaterialsForDayOnce(sourceDayId)
                matsToCopy.forEach {
                    dao.insertMaterial(it.copy(id = 0, dayId = newDayId))
                }
            }

            selectDay(newDay)
        }
    }

    fun updateDay(updated: DayEntity) {
        viewModelScope.launch {
            dao.updateDay(updated)
            _currentDay.value = updated
        }
    }

    fun deleteDay(day: DayEntity) {
        viewModelScope.launch {
            dao.deleteDay(day)

            // Удаляем списки справочников этого дня
            dao.deleteStaffForDay(day.id)
            dao.deleteToolsForDay(day.id)
            dao.deleteEquipmentForDay(day.id)
            dao.deleteMaterialsForDay(day.id)

            if (_currentDay.value?.id == day.id) {
                _currentDay.value = null
                _operations.value = emptyList()
            }
        }
    }

    fun addOperation(name: String) {
        val day = _currentDay.value ?: return
        viewModelScope.launch {
            val op = OperationEntity(
                id = UUID.randomUUID().toString(),
                dayId = day.id,
                name = name,
                startEpoch = System.currentTimeMillis(),
                stopEpoch = null,
                people = 0,
                materials = "",
                tools = "",
                equipment = "",
                workers = "",
                machinists = "",
                notes = ""
            )
            dao.insertOperation(op)
        }
    }

    // Незавершённые операции
    val unfinishedOperations: StateFlow<List<OperationEntity>> =
        operations.map { list: List<OperationEntity> ->
            list.filter { it.stopEpoch == null }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _currentUnfinishedIndex = MutableStateFlow(0)
    val currentUnfinishedIndex: StateFlow<Int> = _currentUnfinishedIndex.asStateFlow()

    fun nextUnfinished() {
        val total = unfinishedOperations.value.size
        if (total > 0) {
            _currentUnfinishedIndex.value = (_currentUnfinishedIndex.value + 1) % total
        }
    }

    fun adjustUnfinishedIndex() {
        val total = unfinishedOperations.value.size
        if (_currentUnfinishedIndex.value >= total) {
            _currentUnfinishedIndex.value = if (total == 0) 0 else total - 1
        }
    }

    fun resetUnfinishedIndex() {
        _currentUnfinishedIndex.value = 0
    }

    fun updateOperation(updated: OperationEntity) {
        viewModelScope.launch {
            dao.updateOperation(updated)
        }
    }

    fun deleteOperation(op: OperationEntity) {
        viewModelScope.launch {
            dao.deleteOperation(op)
        }
    }

    fun stopOperation(op: OperationEntity) {
        viewModelScope.launch {
            val stopped = op.copy(stopEpoch = System.currentTimeMillis())
            dao.updateOperation(stopped)
        }
    }

    fun splitOperation(op: OperationEntity) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val stopped = op.copy(stopEpoch = now)
            dao.updateOperation(stopped)
            val newOp = op.copy(
                id = UUID.randomUUID().toString(),
                startEpoch = now,
                stopEpoch = null
            )
            dao.insertOperation(newOp)
        }
    }

    fun repeatOperation(op: OperationEntity) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newOp = op.copy(
                id = UUID.randomUUID().toString(),
                startEpoch = now,
                stopEpoch = null
            )
            dao.insertOperation(newOp)
        }
    }

    private fun saveLastDayIdToPrefs(id: String) {
        val prefs = context.getSharedPreferences("main_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("last_day_id", id).apply()
    }

    private fun loadLastDayIdFromPrefs(): String? {
        val prefs = context.getSharedPreferences("main_prefs", Context.MODE_PRIVATE)
        return prefs.getString("last_day_id", null)
    }

    class Factory(
        private val dao: AppDao,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(dao, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
