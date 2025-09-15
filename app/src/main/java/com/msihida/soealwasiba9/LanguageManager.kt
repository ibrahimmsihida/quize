package com.msihida.soealwasiba9

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.*

/**
 * فئة لإدارة اللغات المتعددة في التطبيق
 */
class LanguageManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
    private val SELECTED_LANGUAGE_KEY = "selected_language"
    
    // قائمة اللغات المدعومة
    enum class SupportedLanguage(val code: String, val displayName: String) {
        ARABIC("ar", "العربية")
    }
    
    /**
     * تعيين لغة التطبيق
     * @param languageCode رمز اللغة (مثل "ar" للعربية، "en" للإنجليزية)
     * @return true إذا تم تغيير اللغة بنجاح، false إذا كانت اللغة غير مدعومة
     */
    fun setLanguage(languageCode: String): Boolean {
        // التحقق من أن اللغة مدعومة
        if (SupportedLanguage.values().none { it.code == languageCode }) {
            return false
        }
        
        // حفظ اللغة المحددة
        sharedPreferences.edit().putString(SELECTED_LANGUAGE_KEY, languageCode).apply()
        
        // تطبيق اللغة
        applyLanguage()
        
        return true
    }
    
    /**
     * الحصول على رمز اللغة الحالية
     * @return رمز اللغة الحالية
     */
    fun getCurrentLanguageCode(): String {
        return sharedPreferences.getString(SELECTED_LANGUAGE_KEY, SupportedLanguage.ARABIC.code) ?: SupportedLanguage.ARABIC.code
    }
    
    /**
     * الحصول على اسم اللغة الحالية
     * @return اسم اللغة الحالية
     */
    fun getCurrentLanguageName(): String {
        val code = getCurrentLanguageCode()
        return SupportedLanguage.values().find { it.code == code }?.displayName ?: SupportedLanguage.ARABIC.displayName
    }
    
    /**
     * تطبيق اللغة المحددة على التطبيق
     */
    fun applyLanguage() {
        val languageCode = getCurrentLanguageCode()
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        // استخدام الطريقة الجديدة لتحديث التكوين
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }
    
    /**
     * الحصول على قائمة اللغات المدعومة
     * @return قائمة اللغات المدعومة
     */
    fun getSupportedLanguages(): List<SupportedLanguage> {
        return SupportedLanguage.values().toList()
    }
    
    /**
     * تطبيق اللغة المحددة على سياق معين
     * @param context السياق المراد تطبيق اللغة عليه
     * @return سياق جديد باللغة المحددة
     */
    fun applyLanguage(context: Context): Context {
        val languageCode = getCurrentLanguageCode()
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        return context.createConfigurationContext(config)
    }
    
    /**
     * التحقق مما إذا كانت اللغة الحالية تستخدم اتجاه RTL (من اليمين إلى اليسار)
     * @return true إذا كانت اللغة تستخدم RTL، false إذا كانت تستخدم LTR
     */
    fun isCurrentLanguageRTL(): Boolean {
        return true // اللغة العربية دائمًا من اليمين إلى اليسار
    }
    
    /**
     * إنشاء سياق جديد بلغة محددة
     * @param languageCode رمز اللغة
     * @return سياق جديد باللغة المحددة
     */
    fun createContextWithLanguage(languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        return context.createConfigurationContext(config)
    }
}