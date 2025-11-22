package com.example.kampai.ui.theme.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    enum class Language {
        SPANISH, ENGLISH, PORTUGUESE;

        fun getDisplayName(): String = when (this) {
            SPANISH -> "Español 🇪🇸"
            ENGLISH -> "English 🇺🇸"
            PORTUGUESE -> "Português 🇧🇷"
        }
    }

    private val _language = MutableStateFlow(Language.SPANISH)
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _showSuggestionsDialog = MutableStateFlow(false)
    val showSuggestionsDialog: StateFlow<Boolean> = _showSuggestionsDialog.asStateFlow()

    private val _showBugReportDialog = MutableStateFlow(false)
    val showBugReportDialog: StateFlow<Boolean> = _showBugReportDialog.asStateFlow()

    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
    }

    fun showLanguageDialog() {
        // Aquí podrías mostrar un diálogo de selección de idioma
    }

    fun showSuggestionsDialog() {
        _showSuggestionsDialog.value = true
    }

    fun hideSuggestionsDialog() {
        _showSuggestionsDialog.value = false
    }

    fun showBugReportDialog() {
        _showBugReportDialog.value = true
    }

    fun sendSuggestion(suggestion: String) {
        viewModelScope.launch {
            // Opción 1: Enviar por EMAIL
            sendEmailSuggestion(suggestion)

            // Opción 2 (Alternativa): Enviar por Firebase/API
            // sendToBackend(suggestion)
        }
    }

    private fun sendEmailSuggestion(suggestion: String) {
        val email = "kampai.drinks@gmail.com"
        val subject = "Sugerencia de Kampai - v1.0.0"
        val body = """
            📱 Sugerencia de Usuario:
            
            $suggestion
            
            ---
            Idioma: ${_language.value.getDisplayName()}
            Dispositivo: Android
            Sonido: ${if (_soundEnabled.value) "Habilitado" else "Deshabilitado"}
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Enviar sugerencia"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendBugReport(bugDescription: String) {
        viewModelScope.launch {
            val email = "game.kampai@gmail.com"
            val subject = "🐛 Reporte de Error - Kampai v1.0.0"
            val body = """
                🐛 Reporte de Error:
                
                $bugDescription
                
                ---
                Dispositivo: Android
                Versión: 1.0.0
                Idioma: ${_language.value.getDisplayName()}
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }

            try {
                context.startActivity(Intent.createChooser(intent, "Reportar error"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openPrivacyPolicy() {
        openUrl("https://ejemplo.com/privacy")
    }

    fun openTermsOfService() {
        openUrl("https://ejemplo.com/terms")
    }

    fun openPlayStore() {
        val playStoreUrl = "https://play.google.com/store/apps/details?id=com.example.kampai"
        openUrl(playStoreUrl)
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}