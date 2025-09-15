package com.msihida.soealwasiba9

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class ThemeManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ThemeManager"
        private const val PREFS_NAME = "theme_preferences"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_CARD_THEME = "card_theme"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
        
        // أنواع الثيمات
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_AUTO = "auto"
        
        // ثيمات البطاقات
        const val CARD_THEME_DEFAULT = "default"
        const val CARD_THEME_COLORFUL = "colorful"
        const val CARD_THEME_MINIMAL = "minimal"
        const val CARD_THEME_GRADIENT = "gradient"
        
        @Volatile
        private var INSTANCE: ThemeManager? = null
        
        /**
         * الحصول على نسخة وحيدة من مدير الثيمات
         */
        @JvmStatic
        fun getInstance(context: Context): ThemeManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemeManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * حفظ ثيم التطبيق
     */
    fun saveThemeMode(themeMode: String) {
        try {
            sharedPreferences.edit()
                .putString(KEY_THEME_MODE, themeMode)
                .apply()
            applyTheme(themeMode)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error saving theme mode: ${e.message}")
        }
    }
    
    /**
     * الحصول على ثيم التطبيق المحفوظ
     */
    fun getSavedThemeMode(): String {
        return try {
            sharedPreferences.getString(KEY_THEME_MODE, THEME_AUTO) ?: THEME_AUTO
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting saved theme mode: ${e.message}")
            THEME_AUTO
        }
    }
    
    /**
     * تطبيق الثيم
     */
    fun applyTheme(themeMode: String) {
        try {
            when (themeMode) {
                THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                THEME_AUTO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error applying theme: ${e.message}")
            // استخدام الثيم الافتراضي في حالة حدوث خطأ
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    /**
     * تفعيل أو تعطيل الوضع الليلي (متوافق مع DarkModeManager)
     */
    fun setDarkMode(enabled: Boolean) {
        try {
            sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
            
            if (enabled) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error setting dark mode: ${e.message}")
        }
    }
    
    /**
     * التحقق مما إذا كان الوضع الليلي مفعل
     */
    fun isDarkModeEnabled(): Boolean {
        return try {
            sharedPreferences.getBoolean(KEY_DARK_MODE, false)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error checking dark mode: ${e.message}")
            false
        }
    }
    
    /**
     * تبديل حالة الوضع الليلي
     */
    fun toggleDarkMode() {
        try {
            val currentMode = isDarkModeEnabled()
            setDarkMode(!currentMode)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error toggling dark mode: ${e.message}")
        }
    }
    
    /**
     * تطبيق الوضع الليلي عند بدء التطبيق
     */
    fun applyDarkMode() {
        try {
            val darkModeEnabled = isDarkModeEnabled()
            if (darkModeEnabled) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error applying dark mode: ${e.message}")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    /**
     * حفظ ثيم البطاقات
     */
    fun saveCardTheme(cardTheme: String) {
        try {
            sharedPreferences.edit()
                .putString(KEY_CARD_THEME, cardTheme)
                .apply()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error saving card theme: ${e.message}")
        }
    }
    
    /**
     * الحصول على ثيم البطاقات المحفوظ
     */
    fun getSavedCardTheme(): String {
        return try {
            sharedPreferences.getString(KEY_CARD_THEME, CARD_THEME_DEFAULT) ?: CARD_THEME_DEFAULT
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting saved card theme: ${e.message}")
            CARD_THEME_DEFAULT
        }
    }
    
    /**
     * الحصول على ألوان ثيم البطاقة
     */
    fun getCardThemeColors(cardTheme: String): CardThemeColors {
        return try {
            when (cardTheme) {
                CARD_THEME_COLORFUL -> CardThemeColors(
                    primaryColor = R.color.colorful_primary,
                    secondaryColor = R.color.colorful_secondary,
                    backgroundColor = R.color.colorful_background,
                    textColor = R.color.colorful_text
                )
                CARD_THEME_MINIMAL -> CardThemeColors(
                    primaryColor = R.color.minimal_primary,
                    secondaryColor = R.color.minimal_secondary,
                    backgroundColor = R.color.minimal_background,
                    textColor = R.color.minimal_text
                )
                CARD_THEME_GRADIENT -> CardThemeColors(
                    primaryColor = R.color.gradient_primary,
                    secondaryColor = R.color.gradient_secondary,
                    backgroundColor = R.color.gradient_background,
                    textColor = R.color.gradient_text
                )
                else -> CardThemeColors(
                    primaryColor = R.color.colorPrimary,
                    secondaryColor = R.color.colorSecondary,
                    backgroundColor = R.color.colorBackground,
                    textColor = R.color.primary_text_color
                )
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting card theme colors: ${e.message}")
            // إرجاع الثيم الافتراضي في حالة الخطأ
            CardThemeColors(
                primaryColor = R.color.colorPrimary,
                secondaryColor = R.color.colorSecondary,
                backgroundColor = R.color.colorBackground,
                textColor = R.color.primary_text_color
            )
        }
    }
    
    /**
     * الحصول على خلفية البطاقة حسب الثيم
     */
    fun getCardBackground(cardTheme: String): Int {
        return try {
            when (cardTheme) {
                CARD_THEME_COLORFUL -> R.drawable.card_background_colorful
                CARD_THEME_MINIMAL -> R.drawable.card_background_minimal
                CARD_THEME_GRADIENT -> R.drawable.card_background_gradient
                else -> R.drawable.card_background_default
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting card background: ${e.message}")
            R.drawable.card_background_default
        }
    }
    
    /**
     * تهيئة الثيم عند بدء التطبيق
     */
    fun initializeTheme() {
        try {
            val savedTheme = getSavedThemeMode()
            applyTheme(savedTheme)
        } catch (e: Exception) {
            // استخدام الثيم الافتراضي في حالة حدوث خطأ
            android.util.Log.e(TAG, "Error initializing theme: ${e.message}")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}

/**
 * فئة لتخزين ألوان ثيم البطاقة
 */
data class CardThemeColors(
    val primaryColor: Int,
    val secondaryColor: Int,
    val backgroundColor: Int,
    val textColor: Int
)