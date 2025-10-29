package com.example.cvmaker.utils


import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {

    companion object {
        private const val PREF_NAME = "app_preferences"
        private const val KEY_LANG_CODE = "lang_code"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // --- Language ---
    fun saveLanguageCode(code: String) {
        prefs.edit().putString(KEY_LANG_CODE, code).apply()
    }

    fun getLanguageCode(): String? {
        return prefs.getString(KEY_LANG_CODE, null)
    }

    fun clearLanguage() {
        prefs.edit().remove(KEY_LANG_CODE).apply()
    }
}
