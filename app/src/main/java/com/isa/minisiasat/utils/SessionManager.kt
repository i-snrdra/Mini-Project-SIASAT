package com.isa.minisiasat.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "MiniSIASAT_Session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
    }
    
    fun createSession(userId: String, userName: String, userRole: String) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, userName)
        editor.putString(KEY_USER_ROLE, userRole)
        editor.apply()
    }
    
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun getCurrentUserId(): String {
        return prefs.getString(KEY_USER_ID, "") ?: ""
    }
    
    fun getCurrentUserName(): String {
        return prefs.getString(KEY_USER_NAME, "") ?: ""
    }
    
    fun getCurrentUserRole(): String {
        return prefs.getString(KEY_USER_ROLE, "") ?: ""
    }
    
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
    
    fun getSessionData(): Map<String, String> {
        return mapOf(
            "user_id" to getCurrentUserId(),
            "user_name" to getCurrentUserName(),
            "user_role" to getCurrentUserRole()
        )
    }
} 