package com.msihida.soealwasiba9

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * فئة لإدارة قاعدة بيانات الأسئلة
 */
class QuizDatabase(private val context: Context) {
    
    // قائمة الفئات
    private val categories = mutableListOf<QuestionCategory>()
    
    // قائمة الأسئلة مصنفة حسب الفئة
    private val questionsByCategory = mutableMapOf<String, MutableList<Question>>()
    
    init {
        // تهيئة الفئات الافتراضية
        initializeCategories()
        
        // تحميل الأسئلة من ملف JSON
        loadQuestionsFromJson()
    }
    
    /**
     * تهيئة الفئات الافتراضية
     */
    private fun initializeCategories() {
        categories.add(QuestionCategory(
            "general", 
            "معلومات عامة", 
            "أسئلة متنوعة في مجالات مختلفة", 
            R.drawable.ic_general, 
            Difficulty.MEDIUM
        ))
        
        categories.add(QuestionCategory(
            "science", 
            "علوم", 
            "أسئلة في مجال العلوم والتكنولوجيا", 
            R.drawable.ic_science, 
            Difficulty.MEDIUM
        ))
        
        categories.add(QuestionCategory(
            "history", 
            "تاريخ", 
            "أسئلة في التاريخ العربي والعالمي", 
            R.drawable.ic_history, 
            Difficulty.MEDIUM
        ))
        
        categories.add(QuestionCategory(
            "geography", 
            "جغرافيا", 
            "أسئلة في الجغرافيا والمعالم", 
            R.drawable.ic_geography, 
            Difficulty.MEDIUM
        ))
        
        categories.add(QuestionCategory(
            "sports", 
            "رياضة", 
            "أسئلة في مجال الرياضة", 
            R.drawable.ic_sports, 
            Difficulty.EASY
        ))
        
        // إضافة الفئات الجديدة
        categories.add(QuestionCategory(
            "art", 
            context.getString(R.string.category_art), 
            context.getString(R.string.category_art_desc), 
            R.drawable.ic_art, 
            Difficulty.MEDIUM
        ))
        
        categories.add(QuestionCategory(
            "literature", 
            context.getString(R.string.category_literature), 
            context.getString(R.string.category_literature_desc), 
            R.drawable.ic_literature, 
            Difficulty.HARD
        ))
        
        categories.add(QuestionCategory(
            "technology", 
            context.getString(R.string.category_technology), 
            context.getString(R.string.category_technology_desc), 
            R.drawable.ic_technology, 
            Difficulty.MEDIUM
        ))
        
        categories.add(QuestionCategory(
            "religion", 
            context.getString(R.string.category_religion), 
            context.getString(R.string.category_religion_desc), 
            R.drawable.ic_religion, 
            Difficulty.MEDIUM
        ))
        
        // إضافة فئة الألغاز
        categories.add(QuestionCategory(
            "puzzles", 
            context.getString(R.string.category_puzzles), 
            context.getString(R.string.category_puzzles_desc), 
            R.drawable.ic_puzzles, 
            Difficulty.HARD
        ))
    }
    
    /**
     * تحميل الأسئلة من ملف JSON
     */
    private fun loadQuestionsFromJson() {
        try {
            // قراءة ملف JSON من مجلد الأصول
            val questionsJsonString = context.assets.open("questions.json").bufferedReader().use { it.readText() }
            val questionsJsonArray = JSONArray(questionsJsonString)
            
            // تهيئة قائمة الأسئلة العامة
            val generalQuestions = mutableListOf<Question>()
            
            // تحويل نص JSON إلى كائنات Question
            for (i in 0 until questionsJsonArray.length()) {
                val questionJson = questionsJsonArray.getJSONObject(i)
                val questionText = questionJson.getString("question")
                val correctAnswer = questionJson.getInt("correctAnswer")
                
                val optionsJsonArray = questionJson.getJSONArray("options")
                val options = mutableListOf<String>()
                
                for (j in 0 until optionsJsonArray.length()) {
                    options.add(optionsJsonArray.getString(j))
                }
                
                // إضافة السؤال إلى الفئة العامة
                generalQuestions.add(Question(
                    id = "q${i+1}",
                    question = questionText,
                    options = options,
                    correctAnswer = correctAnswer,
                    categoryId = "general"
                ))
            }
            
            // تخزين الأسئلة في الفئة العامة
            questionsByCategory["general"] = generalQuestions
            
        } catch (e: IOException) {
            Log.e("QuizDatabase", "Error loading questions from JSON", e)
        }
    }
    
    /**
     * الحصول على جميع الفئات
     */
    fun getAllCategories(): List<QuestionCategory> {
        return categories
    }
    
    /**
     * الحصول على فئة بالمعرف
     */
    fun getCategoryById(categoryId: String): QuestionCategory? {
        return categories.find { it.id == categoryId }
    }
    
    /**
     * الحصول على الأسئلة حسب الفئة
     */
    fun getQuestionsByCategory(categoryId: String): List<Question> {
        return questionsByCategory[categoryId] ?: emptyList()
    }
    
    /**
     * الحصول على جميع الأسئلة
     */
    fun getAllQuestions(): List<Question> {
        val allQuestions = mutableListOf<Question>()
        questionsByCategory.values.forEach { allQuestions.addAll(it) }
        return allQuestions
    }
    
    /**
     * الحصول على الأسئلة حسب مستوى الصعوبة
     */
    fun getQuestionsByDifficulty(difficulty: Difficulty): List<Question> {
        return getAllQuestions().filter { it.difficulty?.equals(difficulty.name.lowercase(), ignoreCase = true) == true }
    }
    
    /**
     * الحصول على سؤال عشوائي
     */
    fun getRandomQuestion(): Question? {
        val allQuestions = getAllQuestions()
        if (allQuestions.isEmpty()) return null
        return allQuestions.random()
    }
    
    /**
     * الحصول على سؤال عشوائي من فئة محددة
     */
    fun getRandomQuestionFromCategory(categoryId: String): Question? {
        val questions = getQuestionsByCategory(categoryId)
        if (questions.isEmpty()) return null
        return questions.random()
    }
}