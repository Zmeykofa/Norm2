package com.example.normirovshikapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.normirovshikapp.data.AppDao
import com.example.normirovshikapp.data.DayEntity
import com.example.normirovshikapp.data.OperationEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted


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

    // --- Справочники для выбора исполнителей, инструментов, техники и материалов ---
    private val _workersList = MutableStateFlow<List<String>>(emptyList())
    val workersList: StateFlow<List<String>> = _workersList.asStateFlow()

    private val _toolsList = MutableStateFlow<List<String>>(emptyList())
    val toolsList: StateFlow<List<String>> = _toolsList.asStateFlow()

    private val _equipmentList = MutableStateFlow<List<String>>(emptyList())
    val equipmentList: StateFlow<List<String>> = _equipmentList.asStateFlow()

    private val _materialsList = MutableStateFlow<List<String>>(emptyList())
    val materialsList: StateFlow<List<String>> = _materialsList.asStateFlow()

    private val _operationTemplatesList = MutableStateFlow<List<String>>(emptyList())
    val operationTemplatesList: StateFlow<List<String>> = _operationTemplatesList.asStateFlow()

    // Методы добавления новых значений в справочники
    private fun saveResourcesToCurrentDay() {
        val day = _currentDay.value ?: return
        val updated = day.copy(
            workersList = _workersList.value.joinToString(","),
            toolsList = _toolsList.value.joinToString(","),
            equipmentList = _equipmentList.value.joinToString(","),
            materialsList = _materialsList.value.joinToString(","),
            operationTemplatesList = _operationTemplatesList.value.joinToString(",")
        )
        updateDay(updated)
    }

    private fun loadResourcesFromDay(day: DayEntity) {
        _workersList.value = day.workersList.split(",").filter { it.isNotBlank() }
        _toolsList.value = day.toolsList.split(",").filter { it.isNotBlank() }
        _equipmentList.value = day.equipmentList.split(",").filter { it.isNotBlank() }
        _materialsList.value = day.materialsList.split(",").filter { it.isNotBlank() }
        _operationTemplatesList.value = day.operationTemplatesList.split(",").filter { it.isNotBlank() }
    }

    fun addWorker(newWorker: String) {
        if (newWorker.isNotBlank() && !_workersList.value.contains(newWorker)) {
            _workersList.value = listOf(newWorker) + _workersList.value
            saveResourcesToCurrentDay()
        }
    }

    fun addTool(newTool: String) {
        if (newTool.isNotBlank() && !_toolsList.value.contains(newTool)) {
            _toolsList.value = listOf(newTool) + _toolsList.value
            saveResourcesToCurrentDay()
        }
    }

    fun addEquipment(newEq: String) {
        if (newEq.isNotBlank() && !_equipmentList.value.contains(newEq)) {
            _equipmentList.value = listOf(newEq) + _equipmentList.value
            saveResourcesToCurrentDay()
        }
    }

    // --- Обновление и удаление исполнителей ---
    fun updateWorker(old: String, new: String) {
        _workersList.value = _workersList.value.map { if (it == old) new else it }
        saveResourcesToCurrentDay()
    }

    fun removeWorker(item: String) {
        _workersList.value = _workersList.value - item
        saveResourcesToCurrentDay()
    }

    fun moveWorker(item: String, direction: Int) {
        val list = _workersList.value.toMutableList()
        val index = list.indexOf(item)
        if (index == -1) return
        val newIndex = index + direction
        if (newIndex in 0 until list.size) {
            list.removeAt(index)
            list.add(newIndex, item)
            _workersList.value = list
            saveResourcesToCurrentDay()
        }
    }

    // --- Инструменты ---
    fun updateTool(old: String, new: String) {
        _toolsList.value = _toolsList.value.map { if (it == old) new else it }
        saveResourcesToCurrentDay()
    }

    fun removeTool(item: String) {
        _toolsList.value = _toolsList.value - item
        saveResourcesToCurrentDay()
    }

    fun moveTool(item: String, direction: Int) {
        val list = _toolsList.value.toMutableList()
        val index = list.indexOf(item)
        if (index == -1) return
        val newIndex = index + direction
        if (newIndex in 0 until list.size) {
            list.removeAt(index)
            list.add(newIndex, item)
            _toolsList.value = list
            saveResourcesToCurrentDay()
        }
    }

    // --- Техника ---
    fun updateEquipment(old: String, new: String) {
        _equipmentList.value = _equipmentList.value.map { if (it == old) new else it }
        saveResourcesToCurrentDay()
    }

    fun removeEquipment(item: String) {
        _equipmentList.value = _equipmentList.value - item
        saveResourcesToCurrentDay()
    }

    fun moveEquipment(item: String, direction: Int) {
        val list = _equipmentList.value.toMutableList()
        val index = list.indexOf(item)
        if (index == -1) return
        val newIndex = index + direction
        if (newIndex in 0 until list.size) {
            list.removeAt(index)
            list.add(newIndex, item)
            _equipmentList.value = list
            saveResourcesToCurrentDay()
        }
    }

    // --- Материалы ---
    fun updateMaterial(old: String, new: String) {
        _materialsList.value = _materialsList.value.map { if (it == old) new else it }
        saveResourcesToCurrentDay()
    }

    fun removeMaterial(item: String) {
        _materialsList.value = _materialsList.value - item
        saveResourcesToCurrentDay()
    }

    fun moveMaterial(item: String, direction: Int) {
        val list = _materialsList.value.toMutableList()
        val index = list.indexOf(item)
        if (index == -1) return
        val newIndex = index + direction
        if (newIndex in 0 until list.size) {
            list.removeAt(index)
            list.add(newIndex, item)
            _materialsList.value = list
            saveResourcesToCurrentDay()
        }
    }

    fun addMaterial(newMat: String) {
        if (newMat.isNotBlank() && !_materialsList.value.contains(newMat)) {
            _materialsList.value = listOf(newMat) + _materialsList.value
            saveResourcesToCurrentDay()
        }
    }

    // --- Шаблоны операций ---
    fun updateOperationTemplate(old: String, new: String) {
        _operationTemplatesList.value = _operationTemplatesList.value.map { if (it == old) new else it }
        saveResourcesToCurrentDay()
    }

    fun removeOperationTemplate(item: String) {
        _operationTemplatesList.value = _operationTemplatesList.value - item
        saveResourcesToCurrentDay()
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
            saveResourcesToCurrentDay()
        }
    }

    fun addOperationTemplate(newTemp: String) {
        if (newTemp.isNotBlank() && !_operationTemplatesList.value.contains(newTemp)) {
            _operationTemplatesList.value = listOf(newTemp) + _operationTemplatesList.value
            saveResourcesToCurrentDay()
        }
    }

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
                val lastDayId = loadLastDayIdFromPrefs()
                val day = if (lastDayId != null) dao.getDayById(lastDayId) else allDays.first()
                if (day != null) {
                    selectDay(day)
                }
            }
        }
    }

    fun selectDay(day: DayEntity) {
        _currentDay.value = day
        saveLastDayIdToPrefs(day.id)

        // Загружаем списки
        loadResourcesFromDay(day)

        operationsJob?.cancel()
        operationsJob = viewModelScope.launch {
            dao.getOperationsForDayFlow(day.id).collect { list ->
                _operations.value = list
            }
        }
    }

    fun addDay(name: String, sourceDayId: String? = null) {
        viewModelScope.launch {
            // Если есть sourceDayId, пытаемся найти его, чтобы скопировать списки
            val sourceDay = if (sourceDayId != null) dao.getDayById(sourceDayId) else null

            val newDay = DayEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                createdAt = System.currentTimeMillis(),
                // Копируем списки, если день найден
                workersList = sourceDay?.workersList ?: "",
                toolsList = sourceDay?.toolsList ?: "",
                equipmentList = sourceDay?.equipmentList ?: "",
                materialsList = sourceDay?.materialsList ?: "",
                operationTemplatesList = sourceDay?.operationTemplatesList ?: ""
            )
            dao.insertDay(newDay)

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

    // ⚡ новый метод для коррекции индекса
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
