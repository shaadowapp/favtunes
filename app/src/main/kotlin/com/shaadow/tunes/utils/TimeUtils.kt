package com.shaadow.tunes.utils

import java.util.Calendar

object TimeUtils {
    
    fun getTimeBasedGreeting(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
    }
    
    fun getGreetingEmoji(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 5..11 -> "🌅"
            in 12..16 -> "☀️"
            in 17..20 -> "🌆"
            else -> "🌙"
        }
    }
}