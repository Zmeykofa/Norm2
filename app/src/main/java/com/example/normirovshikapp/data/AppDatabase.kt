package com.example.normirovshikapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DayEntity::class, OperationEntity::class],
    version = 5,
    exportSchema = false
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

        // Миграция с версии 2 на 3 — добавляем списки ресурсов
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE DayEntity ADD COLUMN workersList TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE DayEntity ADD COLUMN toolsList TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE DayEntity ADD COLUMN equipmentList TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE DayEntity ADD COLUMN materialsList TEXT NOT NULL DEFAULT ''")
            }
        }

        // Миграция с версии 3 на 4 — добавляем шаблоны операций
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE DayEntity ADD COLUMN operationTemplatesList TEXT NOT NULL DEFAULT ''")
            }
        }

        // Миграция с версии 4 на 5 — добавляем поле machinists в OperationEntity
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE OperationEntity ADD COLUMN machinists TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "normirovshik.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
