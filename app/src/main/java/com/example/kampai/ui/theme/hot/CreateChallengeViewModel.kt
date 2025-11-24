package com.example.kampai.ui.theme.hot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.data.local.entities.CustomCardEntity
import com.example.kampai.data.repository.HotRepository
import com.example.kampai.domain.models.HotIntensity
import com.example.kampai.domain.models.HotTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateChallengeViewModel @Inject constructor(
    private val repository: HotRepository
) : ViewModel() {

    private val _text = MutableStateFlow("")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _intensity = MutableStateFlow(HotIntensity.MEDIUM)
    val intensity: StateFlow<HotIntensity> = _intensity.asStateFlow()

    private val _target = MutableStateFlow(HotTarget.COUPLE)
    val target: StateFlow<HotTarget> = _target.asStateFlow()

    // Para eventos de un solo disparo (Toast de éxito)
    private val _saveEvent = MutableSharedFlow<Unit>()
    val saveEvent: SharedFlow<Unit> = _saveEvent.asSharedFlow()

    fun onTextChanged(newText: String) {
        _text.value = newText
    }

    fun onIntensityChanged(newIntensity: HotIntensity) {
        _intensity.value = newIntensity
    }

    fun onTargetChanged(newTarget: HotTarget) {
        _target.value = newTarget
    }

    fun saveCard() {
        if (_text.value.isBlank()) return

        viewModelScope.launch {
            val newCard = CustomCardEntity(
                text = _text.value.trim(),
                intensity = _intensity.value,
                target = _target.value
            )

            repository.saveCustomCard(newCard)

            // Resetear formulario y notificar éxito
            _text.value = ""
            _saveEvent.emit(Unit)
        }
    }
}