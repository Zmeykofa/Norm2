package com.example.normirovshikapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        DayEntity::class,
        OperationEntity::class,
        StaffEntity::class,
        ToolEntity::class,
        EquipmentEntity::class,
        MaterialEntity::class
    ],
    version = 7,
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

        // Миграция с версии 5 на 6 — создаём отдельные таблицы справочников
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS StaffEntity (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        position TEXT NOT NULL DEFAULT '',
                        grade TEXT NOT NULL DEFAULT '',
                        sortOrder INTEGER NOT NULL DEFAULT 0
                    )"""
                )
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS ToolEntity (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL DEFAULT 0
                    )"""
                )
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS EquipmentEntity (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        position TEXT NOT NULL DEFAULT '',
                        grade TEXT NOT NULL DEFAULT '',
                        machinist TEXT NOT NULL DEFAULT '',
                        sortOrder INTEGER NOT NULL DEFAULT 0
                    )"""
                )
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS MaterialEntity (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL DEFAULT 0
                    )"""
                )
            }
        }

        // Миграция с версии 6 на 7 — добавляем dayId в таблицы справочников для разделения по дням
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE StaffEntity ADD COLUMN dayId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE ToolEntity ADD COLUMN dayId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE EquipmentEntity ADD COLUMN dayId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE MaterialEntity ADD COLUMN dayId TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "normirovshik.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
