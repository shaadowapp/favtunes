package com.shaadow.tunes.utils

import android.content.Context
import android.telephony.TelephonyManager
import java.util.*

object CountryDetector {
    
    fun getCountryCode(context: Context): String {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val countryCode = telephonyManager.networkCountryIso?.uppercase()
            
            if (!countryCode.isNullOrEmpty()) {
                countryCode
            } else {
                // Fallback to locale
                Locale.getDefault().country
            }
        } catch (e: Exception) {
            // Final fallback
            "US"
        }
    }
    
    fun getLanguageForCountry(countryCode: String): String {
        return when (countryCode) {
            "IN" -> "hi" // Hindi for India
            "US", "GB", "AU", "CA" -> "en" // English
            "ES", "MX", "AR" -> "es" // Spanish
            "FR" -> "fr" // French
            "DE" -> "de" // German
            "IT" -> "it" // Italian
            "BR" -> "pt" // Portuguese
            "RU" -> "ru" // Russian
            "JP" -> "ja" // Japanese
            "KR" -> "ko" // Korean
            "CN" -> "zh" // Chinese
            else -> "en" // Default to English
        }
    }
    
    fun getCountryName(countryCode: String): String {
        return when (countryCode) {
            "IN" -> "India"
            "US" -> "United States"
            "GB" -> "United Kingdom"
            "AU" -> "Australia"
            "CA" -> "Canada"
            "ES" -> "Spain"
            "MX" -> "Mexico"
            "AR" -> "Argentina"
            "FR" -> "France"
            "DE" -> "Germany"
            "IT" -> "Italy"
            "BR" -> "Brazil"
            "RU" -> "Russia"
            "JP" -> "Japan"
            "KR" -> "South Korea"
            "CN" -> "China"
            else -> "Unknown"
        }
    }
}