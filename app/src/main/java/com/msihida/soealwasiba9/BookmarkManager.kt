package com.msihida.soealwasiba9

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * فئة لإدارة الأسئلة المحفوظة (المفضلة)
 */
class BookmarkManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("bookmarks_prefs", Context.MODE_PRIVATE)
    private val BOOKMARKED_QUESTIONS_KEY = "bookmarked_questions"
    private val gson = Gson()
    
    /**
     * حفظ سؤال في المفضلة
     * @param question السؤال المراد حفظه
     * @return true إذا تم الحفظ بنجاح، false إذا كان السؤال محفوظًا بالفعل
     */
    fun bookmarkQuestion(question: Question): Boolean {
        val bookmarkedQuestions = getBookmarkedQuestions().toMutableList()
        
        // التحقق مما إذا كان السؤال محفوظًا بالفعل
        val existingQuestion = bookmarkedQuestions.find { it.id == question.id || it.question == question.question }
        if (existingQuestion != null) {
            return false // السؤال محفوظ بالفعل
        }
        
        // إضافة السؤال إلى القائمة
        bookmarkedQuestions.add(question)
        
        // حفظ القائمة المحدثة
        saveBookmarkedQuestions(bookmarkedQuestions)
        return true
    }
    
    /**
     * إزالة سؤال من المفضلة
     * @param questionId معرف السؤال المراد إزالته
     * @return true إذا تمت الإزالة بنجاح، false إذا لم يكن السؤال موجودًا
     */
    fun removeBookmark(questionId: String): Boolean {
        val bookmarkedQuestions = getBookmarkedQuestions().toMutableList()
        val initialSize = bookmarkedQuestions.size
        
        // إزالة السؤال من القائمة
        bookmarkedQuestions.removeAll { it.id == questionId }
        
        // التحقق مما إذا تمت إزالة أي سؤال
        if (bookmarkedQuestions.size < initialSize) {
            saveBookmarkedQuestions(bookmarkedQuestions)
            return true
        }
        return false
    }
    
    /**
     * التحقق مما إذا كان السؤال محفوظًا
     * @param questionId معرف السؤال
     * @return true إذا كان السؤال محفوظًا، false إذا لم يكن كذلك
     */
    fun isBookmarked(questionId: String): Boolean {
        return getBookmarkedQuestions().any { it.id == questionId }
    }
    
    /**
     * الحصول على قائمة الأسئلة المحفوظة
     * @return قائمة الأسئلة المحفوظة
     */
    fun getBookmarkedQuestions(): List<Question> {
        val json = sharedPreferences.getString(BOOKMARKED_QUESTIONS_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<Question>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * حفظ قائمة الأسئلة المحفوظة
     * @param questions قائمة الأسئلة المراد حفظها
     */
    private fun saveBookmarkedQuestions(questions: List<Question>) {
        val json = gson.toJson(questions)
        sharedPreferences.edit().putString(BOOKMARKED_QUESTIONS_KEY, json).apply()
    }
    
    /**
     * الحصول على عدد الأسئلة المحفوظة
     * @return عدد الأسئلة المحفوظة
     */
    fun getBookmarkedQuestionsCount(): Int {
        return getBookmarkedQuestions().size
    }
    
    /**
     * مسح جميع الأسئلة المحفوظة
     */
    fun clearAllBookmarks() {
        sharedPreferences.edit().remove(BOOKMARKED_QUESTIONS_KEY).apply()
    }
}