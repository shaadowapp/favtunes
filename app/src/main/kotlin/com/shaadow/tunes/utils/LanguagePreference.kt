package com.shaadow.tunes.utils

import android.content.Context

object LanguagePreference {
    fun getLanguageCode(context: Context): String {
        val sharedPrefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val savedLanguage = sharedPrefs.getString("selected_language", null)
        
        return if (savedLanguage != null) {
            savedLanguage
        } else {
            // Fallback to country-based detection
            val countryCode = CountryDetector.getCountryCode(context)
            CountryDetector.getLanguageForCountry(countryCode)
        }
    }
    
    fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "hi" -> "Hindi"
            "en" -> "English"
            "es" -> "Spanish"
            "fr" -> "French"
            "de" -> "German"
            "it" -> "Italian"
            "pt" -> "Portuguese"
            "ru" -> "Russian"
            "ja" -> "Japanese"
            "ko" -> "Korean"
            "zh" -> "Chinese"
            else -> "English"
        }
    }
    
    fun getSupportedLanguages(): List<Pair<String, String>> {
        return listOf(
            "en" to "English",
            "hi" to "Hindi",
            "es" to "Spanish",
            "fr" to "French",
            "de" to "German",
            "it" to "Italian",
            "pt" to "Portuguese",
            "ru" to "Russian",
            "ja" to "Japanese",
            "ko" to "Korean",
            "zh" to "Chinese"
        )
    }
}