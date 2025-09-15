package com.msihida.soealwasiba9

import java.util.*

/**
 * فئة تمثل تحدي اليوم
 */
class DailyChallenge(private val quizDatabase: QuizDatabase) {
    
    // تاريخ التحدي الحالي
    private val currentDate: Calendar = Calendar.getInstance()
    
    // قائمة أسئلة التحدي
    private val challengeQuestions = mutableListOf<Question>()
    
    // عدد الأسئلة في التحدي اليومي
    private val CHALLENGE_QUESTIONS_COUNT = 5
    
    init {
        // تهيئة أسئلة التحدي
        generateDailyChallenge()
    }
    
    /**
     * إنشاء تحدي يومي جديد
     */
    private fun generateDailyChallenge() {
        // مسح الأسئلة السابقة
        challengeQuestions.clear()
        
        // الحصول على جميع الأسئلة
        val allQuestions = quizDatabase.getAllQuestions()
        
        // إذا كان هناك أسئلة كافية
        if (allQuestions.size >= CHALLENGE_QUESTIONS_COUNT) {
            // خلط الأسئلة
            val shuffledQuestions = allQuestions.shuffled()
            
            // اختيار عدد محدد من الأسئلة
            for (i in 0 until CHALLENGE_QUESTIONS_COUNT) {
                if (i < shuffledQuestions.size) {
                    challengeQuestions.add(shuffledQuestions[i])
                }
            }
        } else {
            // إضافة جميع الأسئلة المتاحة
            challengeQuestions.addAll(allQuestions)
        }
    }
    
    /**
     * التحقق مما إذا كان التحدي اليومي جديدًا
     */
    fun isNewDay(lastCompletedDate: Calendar): Boolean {
        return lastCompletedDate.get(Calendar.DAY_OF_YEAR) != currentDate.get(Calendar.DAY_OF_YEAR) ||
               lastCompletedDate.get(Calendar.YEAR) != currentDate.get(Calendar.YEAR)
    }
    
    /**
     * الحصول على أسئلة التحدي اليومي
     */
    fun getChallengeQuestions(): List<Question> {
        return challengeQuestions
    }
    
    /**
     * الحصول على تاريخ التحدي الحالي
     */
    fun getCurrentDate(): Calendar {
        return currentDate
    }
}