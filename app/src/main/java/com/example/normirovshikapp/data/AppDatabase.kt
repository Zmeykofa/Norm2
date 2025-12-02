package com.example.normirovshikapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DayEntity::class, OperationEntity::class],
    version = 2, // увеличили версию
    exportSchema = false // отключили экспорт схемы, чтобы не было ошибки
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Миграция с версии 1 на 2 — добавляем поля паспорта дня
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE DayEntity ADD COLUMN location TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE DayEntity ADD COLUMN objectName TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE DayEntity ADD COLUMN organization TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE DayEntity ADD COLUMN workType TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE DayEntity ADD COLUMN processName TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE DayEntity ADD COLUMN docsInfo TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE DayEntity ADD COLUMN brigadeNumber TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE DayEntity ADD COLUMN brigadeLeader TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "normirovshik.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
