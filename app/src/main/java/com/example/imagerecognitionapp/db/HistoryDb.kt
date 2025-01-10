package com.example.imagerecognitionapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.imagerecognitionapp.dao.HistoryDao
import com.example.imagerecognitionapp.data.model.History

// Definimos la migración de la versión 1 a la 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Aquí agregamos la nueva columna a la tabla 'History'
        // Cambiar el esquema de la tabla, eliminando la columna `section`
        database.execSQL("CREATE TABLE IF NOT EXISTS history_new (id INTEGER PRIMARY KEY AUTOINCREMENT, diseaseName TEXT, description TEXT, prevention TEXT, treatment TEXT, imagePath TEXT)")
        database.execSQL("INSERT INTO history_new (id, diseaseName, description, prevention, treatment, imagePath) SELECT id, diseaseName, description, prevention, treatment, imagePath FROM history")
        database.execSQL("DROP TABLE History_database")
        database.execSQL("ALTER TABLE history_new RENAME TO history")
    }
}

@Database(
    entities    = [History::class],
    version     = 2,
    exportSchema = false // Allows us to modify the database schema without needing to recreate the database each time.
)
abstract class HistoryDb:RoomDatabase() {
    abstract fun HistoryDao(): HistoryDao
}