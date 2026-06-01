package com.example.normirovshikapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val position: String = "",
    val grade: String = "",
    val sortOrder: Int = 0,
    val dayId: String = ""
) {
    /** Отображаемое имя как в веб-версии: "Иванов И.И. (Монтажник, 5 разряд)" */
    fun displayName(): String {
        val parts = mutableListOf<String>()
        if (name.isNotBlank()) parts.add(name)
        val details = listOfNotNull(
            position.takeIf { it.isNotBlank() },
            grade.takeIf { it.isNotBlank() }
        )
        return when {
            parts.isEmpty() && details.isEmpty() -> "Сотрудник"
            parts.isEmpty() -> details.joinToString(", ")
            details.isEmpty() -> name
            else -> "$name (${details.joinToString(", ")})"
        }
    }
}
