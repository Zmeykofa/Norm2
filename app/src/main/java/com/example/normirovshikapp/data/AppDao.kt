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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: OperationEntity)

    @Update
    suspend fun updateOperation(operation: OperationEntity)

    @Delete
    suspend fun deleteOperation(operation: OperationEntity)
}
