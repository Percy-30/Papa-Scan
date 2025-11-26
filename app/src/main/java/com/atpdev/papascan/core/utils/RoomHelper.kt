package com.atpdev.papascan.core.utils

import android.app.Application
import androidx.room.Room
import com.atpdev.papascan.data.local.db.HistoryDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomHelper {
    // Esta variable es para asegurar que la base de datos se crea solo una vez
    @Volatile
    private var INSTANCE: HistoryDb? = null
    @Provides
    @Singleton
    fun provideDatabase(app: Application): HistoryDb {
        return Room.databaseBuilder(
            app,
            HistoryDb::class.java,
            "History_database"
        )
            .fallbackToDestructiveMigration() //.addMigrations(MIGRATION_1_2, MIGRATION_2_3)  // Añade ambas migraciones
            .build()
    // Si no necesitas migración, usa esto
    // para borrar la base de datos vieja.

    }

}