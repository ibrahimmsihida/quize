package com.msihida.soealwasiba9

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.widget.Toast

/**
 * فئة لتوفير أدوات مساعدة في الاختبار
 */
class QuizHelper(private val context: Context) {

    // تفضيلات مشتركة لتخزين عدد الأدوات المساعدة المتبقية
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("quiz_helper", Context.MODE_PRIVATE)

    // مفاتيح لتخزين عدد الأدوات المساعدة المتبقية
    private val HINTS_KEY = "hints_remaining"
    private val FIFTY_FIFTY_KEY = "fifty_fifty_remaining"
    private val SKIP_KEY = "skip_remaining"

    // العدد الافتراضي للأدوات المساعدة
    private val DEFAULT_HINTS_COUNT = 3
    private val DEFAULT_FIFTY_FIFTY_COUNT = 3
    private val DEFAULT_SKIP_COUNT = 3
    
    // ألوان أيقونات الأدوات المساعدة
    val HINT_COLOR = Color.parseColor("#2196F3") // أزرق
    val FIFTY_FIFTY_COLOR = Color.parseColor("#FF9800") // برتقالي
    val SKIP_COLOR = Color.parseColor("#4CAF50") // أخضر

    /**
     * الحصول على عدد التلميحات المتبقية
     */
    fun getHintsRemaining(): Int {
        return sharedPreferences.getInt(HINTS_KEY, DEFAULT_HINTS_COUNT)
    }

    /**
     * الحصول على عدد مرات استخدام خيار 50:50 المتبقية
     */
    fun getFiftyFiftyRemaining(): Int {
        return sharedPreferences.getInt(FIFTY_FIFTY_KEY, DEFAULT_FIFTY_FIFTY_COUNT)
    }

    /**
     * الحصول على عدد مرات تخطي السؤال المتبقية
     */
    fun getSkipRemaining(): Int {
        return sharedPreferences.getInt(SKIP_KEY, DEFAULT_SKIP_COUNT)
    }

    /**
     * استخدام تلميح
     * @return true إذا تم استخدام التلميح بنجاح، false إذا لم تكن هناك تلميحات متبقية
     */
    fun useHint(): Boolean {
        val hintsRemaining = getHintsRemaining()
        if (hintsRemaining > 0) {
            sharedPreferences.edit().putInt(HINTS_KEY, hintsRemaining - 1).apply()
            return true
        }
        return false
    }

    /**
     * استخدام خيار 50:50
     * @return true إذا تم استخدام الخيار بنجاح، false إذا لم تكن هناك خيارات متبقية
     */
    fun useFiftyFifty(): Boolean {
        val fiftyFiftyRemaining = getFiftyFiftyRemaining()
        if (fiftyFiftyRemaining > 0) {
            sharedPreferences.edit().putInt(FIFTY_FIFTY_KEY, fiftyFiftyRemaining - 1).apply()
            return true
        }
        return false
    }

    /**
     * استخدام تخطي السؤال
     * @return true إذا تم استخدام التخطي بنجاح، false إذا لم تكن هناك مرات تخطي متبقية
     */
    fun useSkip(): Boolean {
        val skipRemaining = getSkipRemaining()
        if (skipRemaining > 0) {
            sharedPreferences.edit().putInt(SKIP_KEY, skipRemaining - 1).apply()
            return true
        }
        return false
    }

    /**
     * إعادة تعيين جميع الأدوات المساعدة
     */
    fun resetAllHelpers() {
        sharedPreferences.edit()
            .putInt(HINTS_KEY, DEFAULT_HINTS_COUNT)
            .putInt(FIFTY_FIFTY_KEY, DEFAULT_FIFTY_FIFTY_COUNT)
            .putInt(SKIP_KEY, DEFAULT_SKIP_COUNT)
            .apply()
    }

    /**
     * إضافة أداة مساعدة كمكافأة
     * @param helperType نوع الأداة المساعدة ("hint", "fifty_fifty", "skip")
     * @return رسالة تأكيد إضافة الأداة المساعدة
     */
    fun addHelper(helperType: String): String {
        return when (helperType) {
            "hint" -> {
                val current = getHintsRemaining()
                sharedPreferences.edit().putInt(HINTS_KEY, current + 1).apply()
                "تمت إضافة تلميح جديد! لديك الآن ${current + 1} تلميحات."
            }
            "fifty_fifty" -> {
                val current = getFiftyFiftyRemaining()
                sharedPreferences.edit().putInt(FIFTY_FIFTY_KEY, current + 1).apply()
                "تمت إضافة خيار 50:50 جديد! لديك الآن ${current + 1} خيارات."
            }
            "skip" -> {
                val current = getSkipRemaining()
                sharedPreferences.edit().putInt(SKIP_KEY, current + 1).apply()
                "تمت إضافة تخطي جديد! لديك الآن ${current + 1} مرات تخطي."
            }
            else -> "لم يتم التعرف على نوع الأداة المساعدة."
        }
    }
    
    /**
     * إظهار رسالة توضيحية عن الأداة المساعدة
     * @param helperType نوع الأداة المساعدة ("hint", "fifty_fifty", "skip")
     * @return وصف الأداة المساعدة
     */
    fun getHelperDescription(helperType: String): String {
        return when (helperType) {
            "hint" -> "التلميح: يعطيك إشارة إلى الإجابة الصحيحة."
            "fifty_fifty" -> "50:50: يحذف خيارين خاطئين من الخيارات المتاحة."
            "skip" -> "تخطي: يتيح لك تخطي السؤال الحالي والانتقال إلى السؤال التالي."
            else -> "أداة مساعدة غير معروفة."
        }
    }
    
    /**
     * عرض رسالة قصيرة للمستخدم
     * @param message الرسالة المراد عرضها
     */
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}