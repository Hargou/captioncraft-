package com.example.captioncraft.util

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to temporarily store sensitive authentication information for the current session.
 * This should ONLY be used for features that require authentication with the server.
 */
@Singleton
class UserSessionManager @Inject constructor() {
    private var currentUserPassword: String = ""

    fun setUserPassword(password: String) {
        currentUserPassword = password
    }

    fun getUserPassword(): String {
        return currentUserPassword
    }

    fun clearSession() {
        currentUserPassword = ""
    }
} 