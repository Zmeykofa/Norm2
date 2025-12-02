package com.example.normirovshikapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val position: String,
    val grade: String
)
