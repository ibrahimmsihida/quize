package com.msihida.soealwasiba9

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * فئة لإدارة الوضع الليلي (الوضع المظلم) في التطبيق
 */
class DarkModeManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("dark_mode_prefs", Context.MODE_PRIVATE)
    private val DARK_MODE_KEY = "dark_mode_enabled"

    /**
     * تفعيل أو تعطيل الوضع الليلي
     */
    fun setDarkMode(enabled: Boolean) {
        // حفظ الإعداد في التفضيلات المشتركة
        sharedPreferences.edit().putBoolean(DARK_MODE_KEY, enabled).apply()
        
        // تطبيق الوضع الليلي
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    /**
     * التحقق مما إذا كان الوضع الليلي مفعل
     */
    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(DARK_MODE_KEY, false)
    }

    /**
     * تبديل حالة الوضع الليلي
     */
    fun toggleDarkMode() {
        val currentMode = isDarkModeEnabled()
        setDarkMode(!currentMode)
    }

    /**
     * تطبيق الوضع الليلي عند بدء التطبيق
     */
    fun applyDarkMode() {
        val darkModeEnabled = isDarkModeEnabled()
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
    
    /**
     * تطبيق السمة المناسبة عند بدء التطبيق
     */
    fun applyTheme() {
        applyDarkMode()
    }
}