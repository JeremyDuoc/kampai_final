package com.example.kampai.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kampai.domain.models.HotIntensity
import com.example.kampai.domain.models.HotTarget

@Entity(tableName = "custom_cards")
data class CustomCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,           // El reto que escribe el usuario
    val intensity: HotIntensity, // El nivel que el usuario eligió
    val target: HotTarget,       // Si es para pareja o grupo
    val createdDate: Long = System.currentTimeMillis()
)