package com.msihida.soealwasiba9

import android.content.Context
import android.content.SharedPreferences

/**
 * فئة لإدارة وضع التعلم في التطبيق
 * وضع التعلم يوفر شرحًا مفصلًا للإجابات ويسمح بالتنقل بين الأسئلة بحرية
 */
class LearningModeManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("learning_mode_prefs", Context.MODE_PRIVATE)
    private val LEARNING_MODE_ENABLED_KEY = "learning_mode_enabled"
    private val AUTO_SHOW_EXPLANATION_KEY = "auto_show_explanation"
    private val UNLIMITED_TIME_KEY = "unlimited_time"
    private val ALLOW_NAVIGATION_KEY = "allow_navigation"
    
    /**
     * تفعيل أو تعطيل وضع التعلم
     * @param enabled حالة تفعيل وضع التعلم
     */
    fun setLearningModeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(LEARNING_MODE_ENABLED_KEY, enabled).apply()
    }
    
    /**
     * التحقق مما إذا كان وضع التعلم مفعل
     * @return true إذا كان وضع التعلم مفعل، false إذا كان معطل
     */
    fun isLearningModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(LEARNING_MODE_ENABLED_KEY, false)
    }
    
    /**
     * تعيين خيار عرض الشرح تلقائيًا بعد الإجابة
     * @param enabled حالة تفعيل العرض التلقائي للشرح
     */
    fun setAutoShowExplanation(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(AUTO_SHOW_EXPLANATION_KEY, enabled).apply()
    }
    
    /**
     * التحقق مما إذا كان عرض الشرح تلقائيًا مفعل
     * @return true إذا كان عرض الشرح تلقائيًا مفعل، false إذا كان معطل
     */
    fun isAutoShowExplanationEnabled(): Boolean {
        return sharedPreferences.getBoolean(AUTO_SHOW_EXPLANATION_KEY, true)
    }
    
    /**
     * تعيين خيار عرض الشرح تلقائيًا (اسم بديل للتوافق)
     * @param enabled حالة تفعيل العرض التلقائي للشرح
     */
    fun setAutoExplanationEnabled(enabled: Boolean) {
        setAutoShowExplanation(enabled)
    }
    
    /**
     * التحقق مما إذا كان عرض الشرح تلقائيًا مفعل (اسم بديل للتوافق)
     * @return true إذا كان عرض الشرح تلقائيًا مفعل، false إذا كان معطل
     */
    fun isAutoExplanationEnabled(): Boolean {
        return isAutoShowExplanationEnabled()
    }
    
    /**
     * تعيين خيار الوقت غير المحدود
     * @param enabled حالة تفعيل الوقت غير المحدود
     */
    fun setUnlimitedTime(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(UNLIMITED_TIME_KEY, enabled).apply()
    }
    
    /**
     * التحقق مما إذا كان الوقت غير المحدود مفعل
     * @return true إذا كان الوقت غير المحدود مفعل، false إذا كان معطل
     */
    fun isUnlimitedTimeEnabled(): Boolean {
        return sharedPreferences.getBoolean(UNLIMITED_TIME_KEY, true)
    }
    
    /**
     * تعيين خيار السماح بالتنقل بين الأسئلة
     * @param enabled حالة تفعيل التنقل بين الأسئلة
     */
    fun setAllowNavigation(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(ALLOW_NAVIGATION_KEY, enabled).apply()
    }
    
    /**
     * التحقق مما إذا كان التنقل بين الأسئلة مسموحًا
     * @return true إذا كان التنقل مسموحًا، false إذا كان غير مسموح
     */
    fun isNavigationAllowed(): Boolean {
        return sharedPreferences.getBoolean(ALLOW_NAVIGATION_KEY, true)
    }
    
    /**
     * تفعيل وضع التعلم مع الإعدادات الافتراضية
     */
    fun enableLearningModeWithDefaults() {
        sharedPreferences.edit()
            .putBoolean(LEARNING_MODE_ENABLED_KEY, true)
            .putBoolean(AUTO_SHOW_EXPLANATION_KEY, true)
            .putBoolean(UNLIMITED_TIME_KEY, true)
            .putBoolean(ALLOW_NAVIGATION_KEY, true)
            .apply()
    }
    
    /**
     * الحصول على وصف وضع التعلم الحالي
     * @return وصف نصي لإعدادات وضع التعلم الحالية
     */
    fun getLearningModeDescription(): String {
        val features = mutableListOf<String>()
        
        if (isAutoShowExplanationEnabled()) {
            features.add("عرض الشرح تلقائيًا")
        }
        
        if (isUnlimitedTimeEnabled()) {
            features.add("وقت غير محدود")
        }
        
        if (isNavigationAllowed()) {
            features.add("التنقل بين الأسئلة")
        }
        
        return if (features.isEmpty()) {
            "وضع التعلم (بدون ميزات إضافية)"
        } else {
            "وضع التعلم (${features.joinToString(" • ")})"
        }
    }
}