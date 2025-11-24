package com.example.kampai.ui.theme.partymanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.data.PartyRepository
import com.example.kampai.domain.models.Attraction
import com.example.kampai.domain.models.AvatarEmojis
import com.example.kampai.domain.models.Gender
import com.example.kampai.domain.models.PlayerModel
import com.example.kampai.domain.models.Vibe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PartyManagerViewModel @Inject constructor(
    private val repository: PartyRepository
) : ViewModel() {

    private val _players = MutableStateFlow<List<PlayerModel>>(emptyList())
    val players: StateFlow<List<PlayerModel>> = _players.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    init {
        loadSavedPlayers()
    }

    private fun loadSavedPlayers() {
        viewModelScope.launch {
            try {
                val savedPlayers = repository.getPlayers()
                // Migración simple si faltan datos
                val migratedPlayers = savedPlayers.map { player ->
                    if (player.avatarEmoji.isEmpty()) {
                        player.copy(avatarEmoji = AvatarEmojis.getRandomEmoji())
                    } else {
                        player
                    }
                }
                _players.value = migratedPlayers
                if (migratedPlayers != savedPlayers) repository.savePlayers(migratedPlayers)
            } catch (e: Exception) {
                e.printStackTrace()
                _players.value = emptyList()
            }
        }
    }

    fun toggleAddDialog() {
        _showAddDialog.update { !it }
    }

    // FUNCIÓN ACTUALIZADA: Ahora recibe Attraction y Vibe
    fun addPlayer(
        name: String,
        gender: Gender,
        avatarEmoji: String,
        attraction: Attraction,
        vibe: Vibe
    ) {
        viewModelScope.launch {
            try {
                val trimmedName = name.trim()
                if (trimmedName.isBlank()) return@launch

                val nextColorIndex = _players.value.size

                val newPlayer = PlayerModel(
                    id = UUID.randomUUID().toString(),
                    name = trimmedName,
                    gender = gender,
                    colorIndex = nextColorIndex,
                    avatarEmoji = avatarEmoji,
                    attraction = attraction, // Nuevo
                    vibe = vibe            // Nuevo
                )

                val updatedList = _players.value + newPlayer
                _players.value = updatedList
                repository.savePlayers(updatedList)
                toggleAddDialog()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removePlayer(id: String) {
        viewModelScope.launch {
            try {
                val updatedList = _players.value.filter { it.id != id }
                _players.value = updatedList
                repository.savePlayers(updatedList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearAllPlayers() {
        viewModelScope.launch {
            val empty = emptyList<PlayerModel>()
            _players.value = empty
            repository.savePlayers(empty)
        }
    }
}