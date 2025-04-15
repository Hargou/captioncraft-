package com.example.captioncraft.domain.mapper

import java.text.SimpleDateFormat
import java.util.*

// Version that returns a nullable Date for use with models that can have null Dates
fun parseIsoDate(dateString: String?): Date? {
    if (dateString == null) return null
    
    // List of possible date formats to try
    val formats = listOf(
        "yyyy-MM-dd HH:mm:ss",           // SQLite default format
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // ISO format with milliseconds
        "yyyy-MM-dd'T'HH:mm:ss'Z'",      // ISO format without milliseconds
        "yyyy-MM-dd"                      // Simple date
    )
    
    for (format in formats) {
        try {
            return SimpleDateFormat(format, Locale.getDefault())
                .apply { timeZone = TimeZone.getTimeZone("UTC") }
                .parse(dateString)
        } catch (e: Exception) {
            // Try the next format
        }
    }
    
    // If all formats fail, return null
    return null
}

// Version that returns a non-null Date, using current date as fallback
fun parseIsoDateNonNull(dateString: String?): Date {
    return parseIsoDate(dateString) ?: Date()
} 