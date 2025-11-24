package com.example.kampai.ui.theme.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kampai.ui.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeManager: ThemeManager
) : ViewModel() {

    enum class Language(val code: String) {
        SPANISH("es"),
        ENGLISH("en"),
        PORTUGUESE("pt");

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

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _showSuggestionsDialog = MutableStateFlow(false)
    val showSuggestionsDialog: StateFlow<Boolean> = _showSuggestionsDialog.asStateFlow()

    private val _showBugReportDialog = MutableStateFlow(false)
    val showBugReportDialog: StateFlow<Boolean> = _showBugReportDialog.asStateFlow()

    private val _showLanguageDialog = MutableStateFlow(false)
    val showLanguageDialog: StateFlow<Boolean> = _showLanguageDialog.asStateFlow()

    init {
        viewModelScope.launch {
            themeManager.isDarkMode.collect { isDark ->
                _isDarkMode.value = isDark
            }
        }

        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (!currentLocales.isEmpty) {
            val tag = currentLocales[0]?.language
            _language.value = when (tag) {
                "en" -> Language.ENGLISH
                "pt" -> Language.PORTUGUESE
                else -> Language.SPANISH
            }
        }
    }

    fun changeLanguage(newLanguage: Language) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(newLanguage.code)
        AppCompatDelegate.setApplicationLocales(appLocale)
        _language.value = newLanguage
        hideLanguageDialog()
    }

    fun showLanguageDialog() { _showLanguageDialog.value = true }
    fun hideLanguageDialog() { _showLanguageDialog.value = false }

    fun toggleSound() { _soundEnabled.value = !_soundEnabled.value }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkMode(isDark)
            _isDarkMode.value = isDark
        }
    }

    fun showSuggestionsDialog() { _showSuggestionsDialog.value = true }
    fun hideSuggestionsDialog() { _showSuggestionsDialog.value = false }

    fun showBugReportDialog() { _showBugReportDialog.value = true }
    fun hideBugReportDialog() { _showBugReportDialog.value = false }

    // --- LÓGICA DE EMAIL (SUGERENCIAS) ---
    fun sendSuggestion(suggestion: String) {
        viewModelScope.launch {
            sendEmail(
                subject = "Sugerencia Kampai - Android",
                body = """
                    💡 Sugerencia:
                    $suggestion
                    
                    ---
                    Idioma: ${_language.value.code}
                    Versión App: 1.0.0
                """.trimIndent()
            )
        }
        hideSuggestionsDialog()
    }

    // --- LÓGICA DE EMAIL (REPORTAR ERROR) ---
    fun sendBugReport(bugDescription: String) {
        viewModelScope.launch {
            sendEmail(
                subject = "Reporte de Error Kampai - Android",
                body = """
                    🐛 Descripción del error:
                    $bugDescription
                    
                    ---
                    Dispositivo: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
                    Android SDK: ${android.os.Build.VERSION.SDK_INT}
                    Idioma: ${_language.value.code}
                """.trimIndent()
            )
        }
        hideBugReportDialog()
    }

    private fun sendEmail(subject: String, body: String) {
        val email = "game.kampai@gmail.com"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            val chooser = Intent.createChooser(intent, "Enviar email...")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- LÓGICA DE GOOGLE PLAY (CALIFICAR) ---
    fun openPlayStore() {
        val packageName = context.packageName
        try {
            // Intenta abrir la app de Play Store directamente
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Si falla (ej. emulador), abre en el navegador
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    // --- LÓGICA DE LEGAL ---

    fun openPrivacyPolicy() {
        // Prueba primero con Google.com para descartar problemas de tu documento
        openUrl("https://docs.google.com/document/d/e/2PACX-1vQiV8arDJFrRFH5kqN5B6NZi6DJBGe2d80hQVqo2a6QaP5efZtm9koSmdF0wK11VjBWcT8YDUoHnk3-/pub")
    }

    fun openTermsOfService() {
        openUrl("https://docs.google.com/document/d/e/2PACX-1vSaQ6bBOKcAhlV36i5QM_BaLKmIrcdqwqdUztcaETOVL61jXepWGk3Ia7JN7hSheeOhzKaC2eX34uvT/pub")
    }

    private fun openUrl(rawUrl: String) { //No funcionan las URL
        try {
            val cleanUrl = rawUrl.replace("\\s".toRegex(), "")

            android.util.Log.d("KAMPAI_DEBUG", "Intentando abrir URL: '$cleanUrl'")

            val uri = Uri.parse(cleanUrl)

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("KAMPAI_ERROR", "Error abriendo URL: ${e.message}")
        }
    }
}