package com.example.normirovshikapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.normirovshikapp.data.AppDao
import com.example.normirovshikapp.data.DayEntity
import com.example.normirovshikapp.data.OperationEntity
import kotlinx.coroutines.Job
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

    // Методы добавления новых значений в справочники
    fun addWorker(newWorker: String) {
        if (newWorker.isNotBlank() && !_workersList.value.contains(newWorker)) {
            _workersList.value = _workersList.value + newWorker
        }
    }

    fun addTool(newTool: String) {
        if (newTool.isNotBlank() && !_toolsList.value.contains(newTool)) {
            _toolsList.value = _toolsList.value + newTool
        }
    }

    fun addEquipment(newEq: String) {
        if (newEq.isNotBlank() && !_equipmentList.value.contains(newEq)) {
            _equipmentList.value = _equipmentList.value + newEq
        }
    }

    // --- Обновление и удаление исполнителей ---
    fun updateWorker(old: String, new: String) {
        _workersList.value = _workersList.value.map { if (it == old) new else it }
    }

    fun removeWorker(item: String) {
        _workersList.value = _workersList.value - item
    }

    // --- Инструменты ---
    fun updateTool(old: String, new: String) {
        _toolsList.value = _toolsList.value.map { if (it == old) new else it }
    }

    fun removeTool(item: String) {
        _toolsList.value = _toolsList.value - item
    }

    // --- Техника ---
    fun updateEquipment(old: String, new: String) {
        _equipmentList.value = _equipmentList.value.map { if (it == old) new else it }
    }

    fun removeEquipment(item: String) {
        _equipmentList.value = _equipmentList.value - item
    }

    // --- Материалы ---
    fun updateMaterial(old: String, new: String) {
        _materialsList.value = _materialsList.value.map { if (it == old) new else it }
    }

    fun removeMaterial(item: String) {
        _materialsList.value = _materialsList.value - item
    }

    fun addMaterial(newMat: String) {
        if (newMat.isNotBlank() && !_materialsList.value.contains(newMat)) {
            _materialsList.value = _materialsList.value + newMat
        }
    }

    init {
        viewModelScope.launch {
            dao.getAllDaysFlow().collect { list ->
                _days.value = list
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

        operationsJob?.cancel()
        operationsJob = viewModelScope.launch {
            dao.getOperationsForDayFlow(day.id).collect { list ->
                _operations.value = list
            }
        }
    }

    fun addDay(name: String) {
        viewModelScope.launch {
            val newDay = DayEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                createdAt = System.currentTimeMillis()
            )
            dao.insertDay(newDay)

            // сбрасываем справочники
            _workersList.value = emptyList()
            _toolsList.value = emptyList()
            _equipmentList.value = emptyList()
            _materialsList.value = emptyList()

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
