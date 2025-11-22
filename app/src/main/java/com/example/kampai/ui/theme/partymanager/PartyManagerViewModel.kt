package com.example.kampai.ui.theme.partymanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.data.PartyRepository
import com.example.kampai.domain.models.Gender
import com.example.kampai.domain.models.PlayerModel
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
    private val repository: PartyRepository // <-- Inyectamos el repositorio aquí
) : ViewModel() {

    // Estado observable
    private val _players = MutableStateFlow<List<PlayerModel>>(emptyList())
    val players: StateFlow<List<PlayerModel>> = _players.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    // Bloque de inicialización: Se ejecuta cuando se crea el ViewModel
    init {
        loadSavedPlayers()
    }

    private fun loadSavedPlayers() {
        viewModelScope.launch {
            // Recuperamos los jugadores guardados en el disco
            val savedPlayers = repository.getPlayers()
            _players.value = savedPlayers
        }
    }

    fun toggleAddDialog() {
        _showAddDialog.update { !it }
    }

    fun addPlayer(name: String, gender: Gender) {
        viewModelScope.launch {
            val trimmedName = name.trim()
            if (trimmedName.isBlank()) return@launch

            val nextColorIndex = _players.value.size

            val newPlayer = PlayerModel(
                id = UUID.randomUUID().toString(),
                name = trimmedName,
                gender = gender,
                colorIndex = nextColorIndex
            )

            // Actualizamos la lista en memoria
            val updatedList = _players.value + newPlayer
            _players.value = updatedList

            // GUARDAMOS EN DISCO
            repository.savePlayers(updatedList)

            toggleAddDialog()
        }
    }

    fun removePlayer(id: String) {
        viewModelScope.launch {
            // Filtramos y creamos la nueva lista
            val updatedList = _players.value.filter { it.id != id }

            // Actualizamos memoria
            _players.value = updatedList

            // GUARDAMOS EN DISCO
            repository.savePlayers(updatedList)
        }
    }

    fun clearAllPlayers() {
        viewModelScope.launch {
            val empty = emptyList<PlayerModel>()
            _players.value = empty

            // GUARDAMOS (Limpiamos) EN DISCO
            repository.savePlayers(empty)
        }
    }
}