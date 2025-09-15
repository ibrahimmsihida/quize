package com.msihida.soealwasiba9

import android.content.Context
import android.content.SharedPreferences

/**
 * فئة لإدارة إحصائيات المستخدم وتخزينها
 */
class UserStats(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_stats", Context.MODE_PRIVATE)
    
    // الحصول على أعلى نتيجة
    fun getHighScore(): Int {
        return sharedPreferences.getInt(KEY_HIGH_SCORE, 0)
    }
    
    // تحديث أعلى نتيجة إذا كانت النتيجة الجديدة أعلى
    fun updateHighScore(newScore: Int): Boolean {
        val currentHighScore = getHighScore()
        if (newScore > currentHighScore) {
            sharedPreferences.edit().putInt(KEY_HIGH_SCORE, newScore).apply()
            return true
        }
        return false
    }
    
    // زيادة عدد الاختبارات المكتملة
    fun incrementCompletedQuizzes() {
        val current = getCompletedQuizzes()
        sharedPreferences.edit().putInt(KEY_COMPLETED_QUIZZES, current + 1).apply()
    }
    
    // الحصول على عدد الاختبارات المكتملة
    fun getCompletedQuizzes(): Int {
        return sharedPreferences.getInt(KEY_COMPLETED_QUIZZES, 0)
    }
    
    // زيادة عدد الإجابات الصحيحة
    fun incrementCorrectAnswers() {
        val current = getCorrectAnswers()
        sharedPreferences.edit().putInt(KEY_CORRECT_ANSWERS, current + 1).apply()
    }
    
    // الحصول على عدد الإجابات الصحيحة
    fun getCorrectAnswers(): Int {
        return sharedPreferences.getInt(KEY_CORRECT_ANSWERS, 0)
    }
    
    // زيادة عدد الإجابات الخاطئة
    fun incrementWrongAnswers() {
        val current = getWrongAnswers()
        sharedPreferences.edit().putInt(KEY_WRONG_ANSWERS, current + 1).apply()
    }
    
    // الحصول على عدد الإجابات الخاطئة
    fun getWrongAnswers(): Int {
        return sharedPreferences.getInt(KEY_WRONG_ANSWERS, 0)
    }
    
    // إعادة تعيين جميع الإحصائيات
    fun resetAllStats() {
        sharedPreferences.edit().clear().apply()
    }
    
    companion object {
        private const val KEY_HIGH_SCORE = "high_score"
        private const val KEY_COMPLETED_QUIZZES = "completed_quizzes"
        private const val KEY_CORRECT_ANSWERS = "correct_answers"
        private const val KEY_WRONG_ANSWERS = "wrong_answers"
    }
}