package com.dailyrounds.quizapp.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ModuleEntity::class], version = 2, exportSchema = false)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun moduleDao(): ModuleDao

    companion object {
        @Volatile
        private var INSTANCE: QuizDatabase? = null

        fun getInstance(context: android.content.Context): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "database_quiz"
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}