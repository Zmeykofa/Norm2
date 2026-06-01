package com.example.normirovshikapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ToolEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sortOrder: Int = 0,
    val dayId: String = ""
)
