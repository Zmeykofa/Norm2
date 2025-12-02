package com.zmey.normirovshik.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "days")
data class DayEntity(
    @PrimaryKey val id: String,
    val name: String,
    val dateEpoch: Long
)

@Entity(tableName = "operations")
data class OperationEntity(
    @PrimaryKey val id: Long,
    val dayId: String,
    val name: String,
    val startEpoch: Long,
    val stopEpoch: Long?,
    val people: Int,
    val materials: String,
    val tools: String,
    val workers: String,
    val equipment: String,
    val notes: String
)

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val position: String,
    val grade: String
)
