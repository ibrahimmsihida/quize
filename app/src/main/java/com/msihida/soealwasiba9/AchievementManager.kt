package com.msihida.soealwasiba9

import android.content.Context
import android.content.SharedPreferences

/**
 * فئة لإدارة إنجازات المستخدم
 */
class AchievementManager(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("achievements", Context.MODE_PRIVATE)
    private val userStats = UserStats(context)
    private val achievements = mutableListOf<Achievement>()
    
    init {
        // تهيئة الإنجازات المتاحة
        initializeAchievements()
    }
    
    /**
     * تهيئة الإنجازات المتاحة
     */
    private fun initializeAchievements() {
        // إنجازات عدد الاختبارات المكتملة
        achievements.add(Achievement(
            "completed_1",
            "المبتدئ",
            "أكمل اختبارًا واحدًا",
            R.drawable.ic_launcher,
            1,
            AchievementType.COMPLETED_QUIZZES
        ))
        
        achievements.add(Achievement(
            "completed_5",
            "المتعلم",
            "أكمل 5 اختبارات",
            R.drawable.ic_launcher,
            5,
            AchievementType.COMPLETED_QUIZZES
        ))
        
        achievements.add(Achievement(
            "completed_10",
            "المثابر",
            "أكمل 10 اختبارات",
            R.drawable.ic_launcher,
            10,
            AchievementType.COMPLETED_QUIZZES
        ))
        
        // إنجازات عدد الإجابات الصحيحة
        achievements.add(Achievement(
            "correct_10",
            "الذكي",
            "أجب على 10 أسئلة بشكل صحيح",
            R.drawable.ic_launcher,
            10,
            AchievementType.CORRECT_ANSWERS
        ))
        
        achievements.add(Achievement(
            "correct_50",
            "العالم",
            "أجب على 50 سؤالًا بشكل صحيح",
            R.drawable.ic_launcher,
            50,
            AchievementType.CORRECT_ANSWERS
        ))
        
        // إنجازات أعلى نتيجة
        achievements.add(Achievement(
            "score_100",
            "المتفوق",
            "احصل على نتيجة 100 في اختبار واحد",
            R.drawable.ic_launcher,
            100,
            AchievementType.HIGH_SCORE
        ))
    }
    
    /**
     * التحقق من الإنجازات وتحديثها
     */
    fun checkAchievements(): List<Achievement> {
        val newlyUnlockedAchievements = mutableListOf<Achievement>()
        
        for (achievement in achievements) {
            // التحقق مما إذا كان الإنجاز مقفلًا
            if (!isAchievementUnlocked(achievement.id)) {
                // التحقق من استيفاء شروط الإنجاز
                val currentValue = when (achievement.type) {
                    AchievementType.COMPLETED_QUIZZES -> userStats.getCompletedQuizzes()
                    AchievementType.CORRECT_ANSWERS -> userStats.getCorrectAnswers()
                    AchievementType.HIGH_SCORE -> userStats.getHighScore()
                    else -> 0
                }
                
                // إذا تم استيفاء الشرط، قم بفتح الإنجاز
                if (currentValue >= achievement.requiredValue) {
                    unlockAchievement(achievement.id)
                    newlyUnlockedAchievements.add(achievement)
                }
            }
        }
        
        return newlyUnlockedAchievements
    }
    
    /**
     * التحقق مما إذا كان الإنجاز مفتوحًا
     */
    fun isAchievementUnlocked(achievementId: String): Boolean {
        return sharedPreferences.getBoolean(achievementId, false)
    }
    
    /**
     * فتح إنجاز
     */
    private fun unlockAchievement(achievementId: String) {
        sharedPreferences.edit().putBoolean(achievementId, true).apply()
    }
    
    /**
     * الحصول على جميع الإنجازات
     */
    fun getAllAchievements(): List<Achievement> {
        return achievements
    }
    
    /**
     * الحصول على الإنجازات المفتوحة
     */
    fun getUnlockedAchievements(): List<Achievement> {
        return achievements.filter { isAchievementUnlocked(it.id) }
    }
    
    /**
     * الحصول على الإنجازات المقفلة
     */
    fun getLockedAchievements(): List<Achievement> {
        return achievements.filter { !isAchievementUnlocked(it.id) }
    }
    
    /**
     * إعادة تعيين جميع الإنجازات
     */
    fun resetAllAchievements() {
        sharedPreferences.edit().clear().apply()
    }
}