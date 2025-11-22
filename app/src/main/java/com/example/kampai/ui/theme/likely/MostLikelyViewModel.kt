package com.example.kampai.ui.theme.likely

import androidx.lifecycle.ViewModel
import com.example.kampai.data.GameContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MostLikelyViewModel @Inject constructor() : ViewModel() {

    private val _question = MutableStateFlow(GameContent.mostLikely.random())
    val question = _question.asStateFlow()

    fun nextQuestion() {
        _question.value = GameContent.mostLikely.random()
    }
}