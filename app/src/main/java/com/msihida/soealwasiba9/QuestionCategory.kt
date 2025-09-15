package com.msihida.soealwasiba9

/**
 * فئة تمثل تصنيفات الأسئلة المختلفة
 */
data class QuestionCategory(
    val id: String,           // معرف الفئة
    val name: String,         // اسم الفئة
    val description: String,  // وصف الفئة
    val iconResId: Int,       // معرف أيقونة الفئة
    val difficulty: Difficulty // مستوى صعوبة الفئة
)

/**
 * تعداد يمثل مستويات الصعوبة
 */
enum class Difficulty {
    EASY,    // سهل
    MEDIUM,  // متوسط
    HARD     // صعب
}