package com.example.kampai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.kampai.data.local.dao.CustomCardDao
import com.example.kampai.data.local.entities.CustomCardEntity
import com.example.kampai.domain.models.HotIntensity
import com.example.kampai.domain.models.HotTarget

// 1. CONVERTIDORES DE TIPO (Para guardar Enums como Texto)
class Converters {
    @TypeConverter
    fun fromIntensity(value: HotIntensity): String = value.name

    @TypeConverter
    fun toIntensity(value: String): HotIntensity = HotIntensity.valueOf(value)

    @TypeConverter
    fun fromTarget(value: HotTarget): String = value.name

    @TypeConverter
    fun toTarget(value: String): HotTarget = HotTarget.valueOf(value)
}

// 2. LA BASE DE DATOS
@Database(entities = [CustomCardEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customCardDao(): CustomCardDao
}