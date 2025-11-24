package com.example.kampai.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kampai.data.local.entities.CustomCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomCardDao {

    // Obtener todas las cartas personalizadas (se actualiza solo con Flow)
    @Query("SELECT * FROM custom_cards ORDER BY createdDate DESC")
    fun getAllCustomCards(): Flow<List<CustomCardEntity>>

    // Insertar una nueva carta
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CustomCardEntity)

    // Borrar una carta específica
    @Delete
    suspend fun deleteCard(card: CustomCardEntity)

    // Borrar todas (útil para resetear)
    @Query("DELETE FROM custom_cards")
    suspend fun clearAll()
}