package com.msihida.soealwasiba9

/**
 * فئة تمثل إنجازًا يمكن للمستخدم تحقيقه
 */
data class Achievement(
    val id: String,           // معرف الإنجاز
    val title: String,        // عنوان الإنجاز
    val description: String,  // وصف الإنجاز
    val iconResId: Int,       // معرف أيقونة الإنجاز
    val requiredValue: Int,   // القيمة المطلوبة لتحقيق الإنجاز
    val type: AchievementType // نوع الإنجاز
)

/**
 * تعداد يمثل أنواع الإنجازات
 */
enum class AchievementType {
    COMPLETED_QUIZZES,    // عدد الاختبارات المكتملة
    CORRECT_ANSWERS,      // عدد الإجابات الصحيحة
    HIGH_SCORE,           // أعلى نتيجة
    STREAK,               // سلسلة الإجابات الصحيحة المتتالية
    CATEGORIES_COMPLETED  // عدد الفئات المكتملة
}