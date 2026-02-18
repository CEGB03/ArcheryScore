package com.cegb03.archeryscore.data.local.training

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TrainingEntity::class,
        SeriesEntity::class,
        TrainingEndEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trainingDao(): TrainingDao
}

// Migración de versión 4 a 5: Introducción de Series
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Crear tabla de series
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS training_series (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                trainingId INTEGER NOT NULL,
                seriesNumber INTEGER NOT NULL,
                distanceMeters INTEGER NOT NULL,
                category TEXT NOT NULL,
                targetType TEXT NOT NULL,
                arrowsPerEnd INTEGER NOT NULL,
                endsCount INTEGER NOT NULL,
                targetZoneCount INTEGER NOT NULL DEFAULT 10,
                puntajeSystem TEXT NOT NULL DEFAULT 'X_TO_M',
                temperature REAL,
                windSpeed REAL,
                windSpeedUnit TEXT,
                windDirectionDegrees INTEGER,
                skyCondition TEXT,
                locationLat REAL,
                locationLon REAL,
                weatherSource TEXT,
                recordedAt INTEGER,
                FOREIGN KEY(trainingId) REFERENCES trainings(id) ON DELETE CASCADE
            )
        """.trimIndent())
        
        database.execSQL("CREATE INDEX IF NOT EXISTS index_training_series_trainingId ON training_series(trainingId)")
        
        // 2. Migrar datos existentes: Cada training antiguo se convierte en 1 serie
        database.execSQL("""
            INSERT INTO training_series (
                trainingId, seriesNumber, distanceMeters, category, targetType,
                arrowsPerEnd, endsCount, targetZoneCount, puntajeSystem,
                temperature, windSpeed, windSpeedUnit, windDirectionDegrees,
                skyCondition, locationLat, locationLon, weatherSource, recordedAt
            )
            SELECT 
                id, 1, distanceMeters, category, targetType,
                arrowsPerEnd, endsCount, targetZoneCount, puntajeSystem,
                NULL, windSpeed, windSpeedUnit, windDirectionDegrees,
                skyCondition, locationLat, locationLon, weatherSource, createdAt
            FROM trainings
        """.trimIndent())
        
        // 3. Crear tabla temporal para training_ends con nueva estructura
        database.execSQL("""
            CREATE TABLE training_ends_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                seriesId INTEGER NOT NULL,
                endNumber INTEGER NOT NULL,
                confirmedAt INTEGER,
                scoresText TEXT,
                totalScore INTEGER,
                FOREIGN KEY(seriesId) REFERENCES training_series(id) ON DELETE CASCADE
            )
        """.trimIndent())
        
        // 4. Migrar ends: Vincularlos a la serie correspondiente
        database.execSQL("""
            INSERT INTO training_ends_new (id, seriesId, endNumber, confirmedAt, scoresText, totalScore)
            SELECT e.id, s.id, e.endNumber, e.confirmedAt, e.scoresText, e.totalScore
            FROM training_ends e
            INNER JOIN training_series s ON e.trainingId = s.trainingId
        """.trimIndent())
        
        // 5. Reemplazar tabla antigua con la nueva
        database.execSQL("DROP TABLE training_ends")
        database.execSQL("ALTER TABLE training_ends_new RENAME TO training_ends")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_training_ends_seriesId ON training_ends(seriesId)")
        
        // 6. Crear tabla temporal para trainings simplificada
        database.execSQL("""
            CREATE TABLE trainings_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                createdAt INTEGER NOT NULL,
                archerName TEXT
            )
        """.trimIndent())
        
        // 7. Migrar solo campos globales
        database.execSQL("""
            INSERT INTO trainings_new (id, createdAt, archerName)
            SELECT id, createdAt, archerName
            FROM trainings
        """.trimIndent())
        
        // 8. Reemplazar tabla trainings
        database.execSQL("DROP TABLE trainings")
        database.execSQL("ALTER TABLE trainings_new RENAME TO trainings")
    }
}

// Migracion de version 5 a 6: tipo de registro y modalidad
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE trainings ADD COLUMN trainingType TEXT NOT NULL DEFAULT 'TRAINING'")
        database.execSQL("ALTER TABLE trainings ADD COLUMN isGroup INTEGER NOT NULL DEFAULT 0")
    }
}
