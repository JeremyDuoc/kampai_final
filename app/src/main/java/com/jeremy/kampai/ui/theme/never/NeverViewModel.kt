package com.jeremy.kampai.ui.theme.never

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jeremy.kampai.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/** The only game state is the shuffled statement. No individual-player turns. */
data class NeverUiState(
    val question: String?,
    val number: Int,
    val availableCount: Int
) {
    val canAdvance: Boolean get() = question != null
}

@HiltViewModel
class NeverViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Keep the user's existing catalog and original text, including punctuation.
    private val engine = NeverDeckEngine(
        context.resources.getStringArray(R.array.never_questions_list).toList()
    )

    init {
        val savedOrder = savedStateHandle.get<IntArray>("never_order")
        if (savedOrder != null) {
            engine.restore(
                NeverDeckEngine.Snapshot(
                    order = savedOrder,
                    position = savedStateHandle.get<Int>("never_position") ?: 0,
                    number = savedStateHandle.get<Int>("never_number") ?: 1
                )
            )
        }
        // Save even the first question, before any button is pressed.
        val snapshot = engine.snapshot()
        savedStateHandle["never_order"] = snapshot.order
        savedStateHandle["never_position"] = snapshot.position
        savedStateHandle["never_number"] = snapshot.number
    }

    private val _uiState = MutableStateFlow(engine.current().toUi())
    val uiState: StateFlow<NeverUiState> = _uiState.asStateFlow()

    // Synchronous and idempotent with respect to UI animations; each legitimate
    // invocation advances once. The UI disables its action during transitions.
    fun nextQuestion() {
        if (!_uiState.value.canAdvance) return
        _uiState.value = engine.next().toUi()
        persist()
    }

    fun reset() {
        _uiState.value = engine.restart().toUi()
        persist()
    }

    private fun persist() {
        val snapshot = engine.snapshot()
        savedStateHandle["never_order"] = snapshot.order
        savedStateHandle["never_position"] = snapshot.position
        savedStateHandle["never_number"] = snapshot.number
    }

    private fun NeverDeckEngine.State.toUi() = NeverUiState(question, number, size)
}