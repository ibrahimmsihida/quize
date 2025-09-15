package com.msihida.soealwasiba9

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * فئة لإدارة حفظ وتحميل تاريخ نتائج الاختبارات
 */
class QuizResultsManager(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("quiz_results_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    /**
     * حفظ نتيجة اختبار جديدة
     */
    fun saveQuizResult(result: QuizResult) {
        val currentResults = getAllResults().toMutableList()
        currentResults.add(0, result) // إضافة في المقدمة (الأحدث أولاً)
        
        // الاحتفاظ بآخر 100 نتيجة فقط لتوفير المساحة
        if (currentResults.size > MAX_RESULTS) {
            currentResults.removeAt(currentResults.size - 1)
        }
        
        val json = gson.toJson(currentResults)
        sharedPreferences.edit().putString(KEY_RESULTS_LIST, json).apply()
    }
    
    /**
     * الحصول على جميع النتائج
     */
    fun getAllResults(): List<QuizResult> {
        val json = sharedPreferences.getString(KEY_RESULTS_LIST, null)
        return if (json != null) {
            val type = object : TypeToken<List<QuizResult>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    /**
     * الحصول على النتائج حسب الفئة
     */
    fun getResultsByCategory(categoryId: String): List<QuizResult> {
        return getAllResults().filter { it.categoryId == categoryId }
    }
    
    /**
     * الحصول على النتائج حسب مستوى الصعوبة
     */
    fun getResultsByDifficulty(difficulty: String): List<QuizResult> {
        return getAllResults().filter { it.difficulty == difficulty }
    }
    
    /**
     * الحصول على أفضل النتائج (أعلى 10 نتائج)
     */
    fun getTopResults(limit: Int = 10): List<QuizResult> {
        return getAllResults().sortedByDescending { it.score }.take(limit)
    }
    
    /**
     * الحصول على إحصائيات شاملة
     */
    fun getOverallStats(): QuizStats {
        val results = getAllResults()
        
        if (results.isEmpty()) {
            return QuizStats()
        }
        
        val totalQuizzes = results.size
        val totalScore = results.sumOf { it.score }
        val averageScore = totalScore.toDouble() / totalQuizzes
        val bestScore = results.maxOfOrNull { it.score } ?: 0
        val totalCorrectAnswers = results.sumOf { it.correctAnswers }
        val totalWrongAnswers = results.sumOf { it.wrongAnswers }
        val totalQuestions = totalCorrectAnswers + totalWrongAnswers
        val accuracyPercentage = if (totalQuestions > 0) {
            (totalCorrectAnswers.toDouble() / totalQuestions * 100)
        } else 0.0
        
        // إحصائيات حسب مستوى الصعوبة
        val easyResults = results.filter { it.difficulty == "easy" }
        val mediumResults = results.filter { it.difficulty == "medium" }
        val hardResults = results.filter { it.difficulty == "hard" }
        
        return QuizStats(
            totalQuizzes = totalQuizzes,
            averageScore = averageScore,
            bestScore = bestScore,
            totalCorrectAnswers = totalCorrectAnswers,
            totalWrongAnswers = totalWrongAnswers,
            accuracyPercentage = accuracyPercentage,
            easyQuizzes = easyResults.size,
            mediumQuizzes = mediumResults.size,
            hardQuizzes = hardResults.size,
            averageEasyScore = if (easyResults.isNotEmpty()) easyResults.map { it.score }.average() else 0.0,
            averageMediumScore = if (mediumResults.isNotEmpty()) mediumResults.map { it.score }.average() else 0.0,
            averageHardScore = if (hardResults.isNotEmpty()) hardResults.map { it.score }.average() else 0.0
        )
    }
    
    /**
     * مسح جميع النتائج
     */
    fun clearAllResults() {
        sharedPreferences.edit().remove(KEY_RESULTS_LIST).apply()
    }
    
    companion object {
        private const val KEY_RESULTS_LIST = "results_list"
        private const val MAX_RESULTS = 100
    }
}

/**
 * نموذج بيانات نتيجة الاختبار
 */
data class QuizResult(
    val id: String = UUID.randomUUID().toString(),
    val categoryId: String,
    val categoryName: String,
    val difficulty: String,
    val score: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val totalQuestions: Int,
    val timeSpent: Long, // بالثواني
    val date: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * نموذج بيانات الإحصائيات الشاملة
 */
data class QuizStats(
    val totalQuizzes: Int = 0,
    val averageScore: Double = 0.0,
    val bestScore: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val totalWrongAnswers: Int = 0,
    val accuracyPercentage: Double = 0.0,
    val easyQuizzes: Int = 0,
    val mediumQuizzes: Int = 0,
    val hardQuizzes: Int = 0,
    val averageEasyScore: Double = 0.0,
    val averageMediumScore: Double = 0.0,
    val averageHardScore: Double = 0.0
)