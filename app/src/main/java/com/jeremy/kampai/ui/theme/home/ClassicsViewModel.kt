package com.jeremy.kampai.ui.theme.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeremy.kampai.data.GameRepository
import com.jeremy.kampai.domain.models.GameModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassicsViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _classicGames = MutableStateFlow<List<GameModel>>(emptyList())
    val classicGames: StateFlow<List<GameModel>> = _classicGames.asStateFlow()

    init {
        viewModelScope.launch {
            _classicGames.value = repository.getClassicGames()
        }
    }
}