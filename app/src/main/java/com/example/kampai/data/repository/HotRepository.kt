package com.example.kampai.data.repository

import android.content.Context
import com.example.kampai.R
import com.example.kampai.data.local.dao.CustomCardDao
import com.example.kampai.domain.models.HotChallenge
import com.example.kampai.domain.models.HotIntensity
import com.example.kampai.domain.models.HotTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.kampai.data.local.entities.CustomCardEntity
import javax.inject.Inject
import java.util.UUID

class HotRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val customCardDao: CustomCardDao
) {

    // Obtiene el mazo mezclado (Sistema + Usuario) reactivamente
    fun getChallengesFlow(intensity: HotIntensity): Flow<List<HotChallenge>> {
        return customCardDao.getAllCustomCards().map { customEntities ->

            // 1. Convertir cartas de la BD a nuestro modelo unificado
            val customCards = customEntities
                .filter { it.intensity == intensity } // Filtramos por la intensidad actual
                .map { entity ->
                    HotChallenge(
                        id = "custom_${entity.id}",
                        textString = entity.text,
                        intensity = entity.intensity,
                        target = entity.target,
                        isCustom = true
                    )
                }

            // 2. Obtener cartas del sistema (XML)
            val systemCards = getSystemCards(intensity)

            // 3. Mezclar y barajar
            (systemCards + customCards).shuffled()
        }
    }

    // Carga los arrays del XML según la intensidad
    private fun getSystemCards(intensity: HotIntensity): List<HotChallenge> {
        val arrayResId = when (intensity) {
            HotIntensity.SOFT -> R.array.hot_soft_challenges
            HotIntensity.MEDIUM -> R.array.hot_medium_challenges
            HotIntensity.HOT -> R.array.hot_hard_challenges
            HotIntensity.EXTREME -> null // No hay extreme por defecto en el sistema
        } ?: return emptyList()

        // OJO: Aquí cargamos el array solo para contar cuántos hay y generar IDs,
        // pero NO guardamos el String. Guardamos el ID del array y el índice.
        // Para simplificar en este modelo híbrido, usaremos una estrategia de texto directo
        // O mejor: Extraemos los strings aquí. Si quieres cambio de idioma en tiempo real sin reiniciar,
        // la estrategia cambia, pero para un juego de beber, cargar strings al inicio de la partida está bien.

        val texts = context.resources.getStringArray(arrayResId)

        return texts.map { text ->
            HotChallenge(
                id = UUID.randomUUID().toString(),
                textString = text, // Guardamos el texto cargado del idioma actual
                intensity = intensity,
                target = HotTarget.GROUP // Por defecto sistema es Grupo
            )
        }
    }
    suspend fun saveCustomCard(card: CustomCardEntity) = customCardDao.insertCard(card)
}