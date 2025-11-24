package com.example.kampai.ui.theme.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.data.GameRepository
import com.example.kampai.domain.models.GameModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GameRepository,
    @ApplicationContext private val context: Context // Inyectamos contexto para guardar datos
) : ViewModel() {

    private val _mostPlayedGames = MutableStateFlow<List<GameModel>>(emptyList())
    val mostPlayedGames: StateFlow<List<GameModel>> = _mostPlayedGames.asStateFlow()

    // Estado para controlar si mostramos el aviso legal
    private val _showDisclaimer = MutableStateFlow(false)
    val showDisclaimer: StateFlow<Boolean> = _showDisclaimer.asStateFlow()

    init {
        checkDisclaimerStatus()
        loadGames()
    }

    private fun checkDisclaimerStatus() {
        // Leemos las preferencias guardadas
        val prefs = context.getSharedPreferences("kampai_prefs", Context.MODE_PRIVATE)
        val isAccepted = prefs.getBoolean("disclaimer_accepted", false)

        // Si NO ha sido aceptado, mostramos el diálogo
        _showDisclaimer.value = !isAccepted
    }

    fun acceptDisclaimer() {
        // Guardamos que el usuario aceptó
        val prefs = context.getSharedPreferences("kampai_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("disclaimer_accepted", true).apply()

        // Ocultamos el diálogo
        _showDisclaimer.value = false
    }

    private fun loadGames() {
        viewModelScope.launch {
            _mostPlayedGames.value = repository.getMostPlayedGames()
        }
    }
}