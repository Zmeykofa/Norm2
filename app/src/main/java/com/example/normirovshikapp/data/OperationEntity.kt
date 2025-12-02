package com.example.normirovshikapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OperationEntity(
    @PrimaryKey val id: String,
    val dayId: String,
    val name: String,
    val startEpoch: Long,
    val stopEpoch: Long?,
    val people: Int,
    val materials: String,
    val tools: String,
    val equipment: String,
    val workers: String,
    val notes: String
)
