package com.example.normirovshikapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DayEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long,

    // Поля паспорта дня
    val location: String = "",
    val objectName: String = "",
    val organization: String = "",
    val workType: String = "",
    val processName: String = "",
    val docsInfo: String = "",
    val brigadeNumber: String = "",
    val brigadeLeader: String = ""
)
