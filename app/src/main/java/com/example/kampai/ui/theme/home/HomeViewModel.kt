package com.example.kampai.ui.theme.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.data.GameRepository
import com.example.kampai.domain.models.GameModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    // Solo cargamos los Top 3 aquí
    private val _mostPlayedGames = MutableStateFlow<List<GameModel>>(emptyList())
    val mostPlayedGames: StateFlow<List<GameModel>> = _mostPlayedGames.asStateFlow()

    init {
        loadGames()
    }

    private fun loadGames() {
        viewModelScope.launch {
            _mostPlayedGames.value = repository.getMostPlayedGames()
        }
    }
}