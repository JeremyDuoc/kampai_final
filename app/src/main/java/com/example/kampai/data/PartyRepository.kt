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
        val json = gson.toJson(players)
        sharedPreferences.edit().putString("saved_players", json).apply()
    }

    fun getPlayers(): List<PlayerModel> {
        val json = sharedPreferences.getString("saved_players", null) ?: return emptyList()

        val type = object : TypeToken<List<PlayerModel>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}