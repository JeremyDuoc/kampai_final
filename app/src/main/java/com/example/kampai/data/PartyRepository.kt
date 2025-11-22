package com.example.kampai.data

import android.content.Context
import android.content.SharedPreferences
import com.example.kampai.domain.models.PlayerModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartyRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("kampai_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun savePlayers(players: List<PlayerModel>) {
        try {
            val json = gson.toJson(players)
            sharedPreferences.edit().putString("saved_players", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPlayers(): List<PlayerModel> {
        return try {
            val json = sharedPreferences.getString("saved_players", null)
            if (json.isNullOrEmpty()) {
                emptyList()
            } else {
                val type = object : TypeToken<List<PlayerModel>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Si hay error al deserializar, limpiar y devolver lista vacía
            sharedPreferences.edit().remove("saved_players").apply()
            emptyList()
        }
    }
}