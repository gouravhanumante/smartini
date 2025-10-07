package com.dailyrounds.quizapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "modules_table")
data class ModuleEntity(
    @PrimaryKey val moduleId: String,
    val previousScore: Int = 0,
    val totalQuestions: Int = 0,
    val highestStreak: Int = 0,
    val skippedQuestions: Int = 0,
    val isCompleted: Boolean = false,
)

