package com.example.normirovshikapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Дни ---
    @Query("SELECT * FROM DayEntity ORDER BY createdAt DESC")
    fun getAllDaysFlow(): Flow<List<DayEntity>>

    @Query("SELECT * FROM DayEntity ORDER BY createdAt DESC")
    suspend fun getAllDaysOnce(): List<DayEntity>

    @Query("SELECT * FROM DayEntity WHERE id = :dayId LIMIT 1")
    suspend fun getDayById(dayId: String): DayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: DayEntity)

    @Update
    suspend fun updateDay(day: DayEntity)

    @Delete
    suspend fun deleteDay(day: DayEntity)

    // --- Операции ---
    @Query("SELECT * FROM OperationEntity WHERE dayId = :dayId ORDER BY startEpoch ASC")
    fun getOperationsForDayFlow(dayId: String): Flow<List<OperationEntity>>

    @Query("SELECT * FROM OperationEntity WHERE dayId = :dayId")
    suspend fun getOperationsForDayOnce(dayId: String): List<OperationEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: OperationEntity)

    @Update
    suspend fun updateOperation(operation: OperationEntity)

    @Delete
    suspend fun deleteOperation(operation: OperationEntity)

    // --- Справочник: Исполнители ---
    @Query("SELECT * FROM StaffEntity WHERE dayId = :dayId ORDER BY sortOrder ASC, id ASC")
    fun getStaffForDayFlow(dayId: String): Flow<List<StaffEntity>>

    @Query("SELECT * FROM StaffEntity WHERE dayId = :dayId ORDER BY sortOrder ASC, id ASC")
    suspend fun getStaffForDayOnce(dayId: String): List<StaffEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity)

    @Update
    suspend fun updateStaff(staff: StaffEntity)

    @Delete
    suspend fun deleteStaff(staff: StaffEntity)

    @Query("SELECT MAX(sortOrder) FROM StaffEntity WHERE dayId = :dayId")
    suspend fun getMaxStaffSortOrder(dayId: String): Int?

    @Query("DELETE FROM StaffEntity WHERE dayId = :dayId")
    suspend fun deleteStaffForDay(dayId: String)

    // --- Справочник: Инструменты ---
    @Query("SELECT * FROM ToolEntity WHERE dayId = :dayId ORDER BY sortOrder ASC, id ASC")
    fun getToolsForDayFlow(dayId: String): Flow<List<ToolEntity>>

    @Query("SELECT * FROM ToolEntity WHERE dayId = :dayId ORDER BY sortOrder ASC, id ASC")
    suspend fun getToolsForDayOnce(dayId: String): List<ToolEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTool(tool: ToolEntity)

    @Update
    suspend fun updateTool(tool: ToolEntity)

    @Delete
    suspend fun deleteTool(tool: ToolEntity)

    @Query("SELECT MAX(sortOrder) FROM ToolEntity WHERE dayId = :dayId")
    suspend fun getMaxToolSortOrder(dayId: String): Int?

    @Query("DELETE FROM ToolEntity WHERE dayId = :dayId")
    suspend fun deleteToolsForDay(dayId: String)

    // --- Справочник: Техника ---
    @Query("SELECT * FROM EquipmentEntity WHERE dayId = :dayId ORDER BY sortOrder ASC, id ASC")
    fun getEquipmentForDayFlow(dayId: String): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM EquipmentEntity WHERE dayId = :dayId ORDER BY sortOrder ASC, id ASC")
    suspend fun getEquipmentForDayOnce(dayId: String): List<EquipmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: EquipmentEntity)

    @Update
    suspend fun updateEquipment(equipment: EquipmentEntity)

    @Delete
    suspend fun deleteEquipment(equipment: EquipmentEntity)

    @Query("SELECT MAX(sortOrder) FROM EquipmentEntity WHERE dayId = :dayId")
    suspend fun getMaxEquipmentSortOrder(dayId: String): Int?

    @Query("DELETE FROM EquipmentEntity WHERE dayId = :dayId")
    suspend fun deleteEquipmentForDay(dayId: String)

    // --- Справочник: Материалы ---
    @Query("SELECT * FROM MaterialEntity WHERE dayId = :dayId ORDER BY sortOrder ASC, id ASC")
    fun getMaterialsForDayFlow(dayId: String): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM MaterialEntity WHERE dayId = :dayId ORDER BY sortOrder ASC, id ASC")
    suspend fun getMaterialsForDayOnce(dayId: String): List<MaterialEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity)

    @Update
    suspend fun updateMaterial(material: MaterialEntity)

    @Delete
    suspend fun deleteMaterial(material: MaterialEntity)

    @Query("SELECT MAX(sortOrder) FROM MaterialEntity WHERE dayId = :dayId")
    suspend fun getMaxMaterialSortOrder(dayId: String): Int?

    @Query("DELETE FROM MaterialEntity WHERE dayId = :dayId")
    suspend fun deleteMaterialsForDay(dayId: String)
}
