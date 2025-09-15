package com.msihida.soealwasiba9

/**
 * فئة تمثل نموذج بيانات السؤال
 */
data class Question(
    val id: String = "",           // معرف السؤال
    val question: String,         // نص السؤال
    val options: List<String>,    // قائمة الخيارات المتاحة
    val correctAnswer: Int,       // مؤشر الإجابة الصحيحة (0-3)
    val explanation: String = "", // شرح للإجابة الصحيحة
    val categoryId: String = "",  // معرف الفئة التي ينتمي إليها السؤال
    val difficulty: String? = "easy", // مستوى صعوبة السؤال كنص
    val category: String = "general", // اسم الفئة
    val imageUrl: String = ""     // رابط صورة اختياري للسؤال
) {
    // Constructor للتوافق مع الكود القديم
    constructor(
        question: String,
        options: List<String>,
        correctAnswer: Int,
        difficulty: String? = "easy",
        category: String = "general"
    ) : this(
        id = "",
        question = question,
        options = options,
        correctAnswer = correctAnswer,
        explanation = "",
        categoryId = category,
        difficulty = difficulty,
        category = category,
        imageUrl = ""
    )
}