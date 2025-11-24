package com.example.kampai.ui.theme.culture

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.kampai.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CultureViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val categories = context.resources.getStringArray(R.array.culture_categories_list).toList()

    private val _currentCategory = MutableStateFlow(categories.random())
    val currentCategory: StateFlow<String> = _currentCategory.asStateFlow()

    fun nextCategory() {
        _currentCategory.value = categories.random()
    }
}