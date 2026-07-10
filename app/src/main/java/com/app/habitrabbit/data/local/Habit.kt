package com.app.habitrabbit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#6750A4", // color por defecto, para identificar el hábito visualmente
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)