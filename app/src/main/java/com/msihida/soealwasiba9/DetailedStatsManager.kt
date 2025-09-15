package com.msihida.soealwasiba9

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * فئة لإدارة الإحصائيات المفصلة للمستخدم
 */
class DetailedStatsManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("detailed_stats_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // مفاتيح لحفظ البيانات
    private val CATEGORY_STATS_KEY = "category_stats"
    private val DAILY_PERFORMANCE_KEY = "daily_performance"
    private val QUESTION_DIFFICULTY_STATS_KEY = "question_difficulty_stats"
    private val TIME_SPENT_KEY = "time_spent"
    private val QUIZ_HISTORY_KEY = "quiz_history"
    
    /**
     * فئة تمثل إحصائيات فئة معينة
     */
    data class CategoryStats(
        val categoryId: String,
        var totalQuestions: Int = 0,
        var correctAnswers: Int = 0,
        var incorrectAnswers: Int = 0,
        var timeSpentSeconds: Long = 0
    ) {
        fun getAccuracyPercentage(): Int {
            return if (totalQuestions > 0) {
                (correctAnswers * 100) / totalQuestions
            } else {
                0
            }
        }
    }
    
    /**
     * فئة تمثل أداء يومي
     */
    data class DailyPerformance(
        val date: String, // بتنسيق yyyy-MM-dd
        var questionsAnswered: Int = 0,
        var correctAnswers: Int = 0,
        var timeSpentSeconds: Long = 0
    ) {
        fun getAccuracyPercentage(): Int {
            return if (questionsAnswered > 0) {
                (correctAnswers * 100) / questionsAnswered
            } else {
                0
            }
        }
    }
    
    /**
     * فئة تمثل إحصائيات مستوى صعوبة الأسئلة
     */
    data class DifficultyStats(
        val difficulty: String, // سهل، متوسط، صعب
        var totalQuestions: Int = 0,
        var correctAnswers: Int = 0,
        var averageTimeSeconds: Float = 0f
    ) {
        fun getAccuracyPercentage(): Int {
            return if (totalQuestions > 0) {
                (correctAnswers * 100) / totalQuestions
            } else {
                0
            }
        }
    }
    
    /**
     * فئة تمثل سجل اختبار
     */
    data class QuizRecord(
        val id: String = UUID.randomUUID().toString(),
        val date: String, // بتنسيق yyyy-MM-dd HH:mm:ss
        val categoryId: String,
        val score: Int,
        val totalQuestions: Int,
        val timeSpentSeconds: Long,
        val difficulty: String
    ) {
        fun getScorePercentage(): Int {
            return if (totalQuestions > 0) {
                (score * 100) / totalQuestions
            } else {
                0
            }
        }
    }
    
    /**
     * الحصول على إحصائيات الفئات
     */
    fun getCategoryStats(): Map<String, CategoryStats> {
        val json = sharedPreferences.getString(CATEGORY_STATS_KEY, null) ?: return mutableMapOf()
        val type = object : TypeToken<Map<String, CategoryStats>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }
    
    /**
     * تحديث إحصائيات فئة
     */
    fun updateCategoryStats(categoryId: String, isCorrect: Boolean, timeSpentSeconds: Long) {
        val stats = getCategoryStats().toMutableMap()
        val categoryStats = stats[categoryId] ?: CategoryStats(categoryId)
        
        categoryStats.totalQuestions++
        if (isCorrect) {
            categoryStats.correctAnswers++
        } else {
            categoryStats.incorrectAnswers++
        }
        categoryStats.timeSpentSeconds += timeSpentSeconds
        
        stats[categoryId] = categoryStats
        saveCategoryStats(stats)
    }
    
    /**
     * حفظ إحصائيات الفئات
     */
    private fun saveCategoryStats(stats: Map<String, CategoryStats>) {
        val json = gson.toJson(stats)
        sharedPreferences.edit().putString(CATEGORY_STATS_KEY, json).apply()
    }
    
    /**
     * الحصول على الأداء اليومي
     */
    fun getDailyPerformance(): Map<String, DailyPerformance> {
        val json = sharedPreferences.getString(DAILY_PERFORMANCE_KEY, null) ?: return mutableMapOf()
        val type = object : TypeToken<Map<String, DailyPerformance>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }
    
    /**
     * تحديث الأداء اليومي
     */
    fun updateDailyPerformance(isCorrect: Boolean, timeSpentSeconds: Long) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        
        val performance = getDailyPerformance().toMutableMap()
        val dailyPerformance = performance[today] ?: DailyPerformance(today)
        
        dailyPerformance.questionsAnswered++
        if (isCorrect) {
            dailyPerformance.correctAnswers++
        }
        dailyPerformance.timeSpentSeconds += timeSpentSeconds
        
        performance[today] = dailyPerformance
        saveDailyPerformance(performance)
    }
    
    /**
     * حفظ الأداء اليومي
     */
    private fun saveDailyPerformance(performance: Map<String, DailyPerformance>) {
        val json = gson.toJson(performance)
        sharedPreferences.edit().putString(DAILY_PERFORMANCE_KEY, json).apply()
    }
    
    /**
     * الحصول على إحصائيات مستوى الصعوبة
     */
    fun getDifficultyStats(): Map<String, DifficultyStats> {
        val json = sharedPreferences.getString(QUESTION_DIFFICULTY_STATS_KEY, null) ?: return mutableMapOf()
        val type = object : TypeToken<Map<String, DifficultyStats>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }
    
    /**
     * تحديث إحصائيات مستوى الصعوبة
     */
    fun updateDifficultyStats(difficulty: String, isCorrect: Boolean, timeSpentSeconds: Long) {
        val stats = getDifficultyStats().toMutableMap()
        val difficultyStats = stats[difficulty] ?: DifficultyStats(difficulty)
        
        difficultyStats.totalQuestions++
        if (isCorrect) {
            difficultyStats.correctAnswers++
        }
        
        // تحديث متوسط الوقت
        val totalTime = difficultyStats.averageTimeSeconds * (difficultyStats.totalQuestions - 1) + timeSpentSeconds
        difficultyStats.averageTimeSeconds = totalTime / difficultyStats.totalQuestions
        
        stats[difficulty] = difficultyStats
        saveDifficultyStats(stats)
    }
    
    /**
     * حفظ إحصائيات مستوى الصعوبة
     */
    private fun saveDifficultyStats(stats: Map<String, DifficultyStats>) {
        val json = gson.toJson(stats)
        sharedPreferences.edit().putString(QUESTION_DIFFICULTY_STATS_KEY, json).apply()
    }
    
    /**
     * الحصول على إجمالي الوقت المستغرق
     */
    fun getTotalTimeSpent(): Long {
        return sharedPreferences.getLong(TIME_SPENT_KEY, 0)
    }
    
    /**
     * تحديث إجمالي الوقت المستغرق
     */
    fun updateTotalTimeSpent(timeSpentSeconds: Long) {
        val currentTotal = getTotalTimeSpent()
        sharedPreferences.edit().putLong(TIME_SPENT_KEY, currentTotal + timeSpentSeconds).apply()
    }
    
    /**
     * الحصول على سجل الاختبارات
     */
    fun getQuizHistory(): List<QuizRecord> {
        val json = sharedPreferences.getString(QUIZ_HISTORY_KEY, null) ?: return listOf()
        val type = object : TypeToken<List<QuizRecord>>() {}.type
        return gson.fromJson(json, type) ?: listOf()
    }
    
    /**
     * إضافة سجل اختبار جديد
     */
    fun addQuizRecord(categoryId: String, score: Int, totalQuestions: Int, timeSpentSeconds: Long, difficulty: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateTime = dateFormat.format(Date())
        
        val record = QuizRecord(
            date = dateTime,
            categoryId = categoryId,
            score = score,
            totalQuestions = totalQuestions,
            timeSpentSeconds = timeSpentSeconds,
            difficulty = difficulty
        )
        
        val history = getQuizHistory().toMutableList()
        history.add(record)
        saveQuizHistory(history)
    }
    
    /**
     * حفظ سجل الاختبارات
     */
    private fun saveQuizHistory(history: List<QuizRecord>) {
        val json = gson.toJson(history)
        sharedPreferences.edit().putString(QUIZ_HISTORY_KEY, json).apply()
    }
    
    /**
     * الحصول على أفضل فئة أداءً
     */
    fun getBestPerformingCategory(): CategoryStats? {
        val stats = getCategoryStats()
        if (stats.isEmpty()) return null
        
        return stats.values.maxByOrNull { it.getAccuracyPercentage() }
    }
    
    /**
     * الحصول على أسوأ فئة أداءً
     */
    fun getWorstPerformingCategory(): CategoryStats? {
        val stats = getCategoryStats()
        if (stats.isEmpty()) return null
        
        return stats.values.minByOrNull { it.getAccuracyPercentage() }
    }
    
    /**
     * الحصول على متوسط الدقة الإجمالي
     */
    fun getOverallAccuracy(): Int {
        val stats = getCategoryStats()
        if (stats.isEmpty()) return 0
        
        val totalQuestions = stats.values.sumOf { it.totalQuestions }
        val totalCorrect = stats.values.sumOf { it.correctAnswers }
        
        return if (totalQuestions > 0) {
            (totalCorrect * 100) / totalQuestions
        } else {
            0
        }
    }
    
    /**
     * الحصول على أداء آخر 7 أيام
     */
    fun getLast7DaysPerformance(): List<DailyPerformance> {
        val performance = getDailyPerformance()
        if (performance.isEmpty()) return listOf()
        
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val result = mutableListOf<DailyPerformance>()
        
        // الحصول على آخر 7 أيام
        for (i in 6 downTo 0) {
            calendar.time = Date() // إعادة تعيين إلى اليوم الحالي
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = dateFormat.format(calendar.time)
            
            // إضافة الأداء لهذا اليوم إذا كان موجودًا، أو إنشاء سجل فارغ
            val dailyPerformance = performance[date] ?: DailyPerformance(date)
            result.add(dailyPerformance)
        }
        
        return result
    }
    
    /**
     * مسح جميع الإحصائيات
     */
    fun clearAllStats() {
        sharedPreferences.edit()
            .remove(CATEGORY_STATS_KEY)
            .remove(DAILY_PERFORMANCE_KEY)
            .remove(QUESTION_DIFFICULTY_STATS_KEY)
            .remove(TIME_SPENT_KEY)
            .remove(QUIZ_HISTORY_KEY)
            .apply()
    }
}