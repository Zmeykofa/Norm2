package com.example.normirovshikapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val position: String = "",
    val grade: String = "",
    val machinist: String = "",
    val sortOrder: Int = 0,
    val dayId: String = ""
) {
    /** Отображаемое имя как в веб-версии: "Экскаватор ЭО-5122 [Петров, 5 разряд]" */
    fun displayName(): String {
        val details = listOfNotNull(
            machinist.takeIf { it.isNotBlank() },
            position.takeIf { it.isNotBlank() },
            grade.takeIf { it.isNotBlank() }
        )
        return if (details.isEmpty()) name else "$name [${details.joinToString(", ")}]"
    }
}
