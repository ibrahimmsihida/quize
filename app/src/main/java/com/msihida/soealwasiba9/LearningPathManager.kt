package com.msihida.soealwasiba9

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * فئة لإدارة مسارات التعلم في التطبيق
 */
class LearningPathManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("learning_path_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // مفاتيح لحفظ البيانات
    private val LEARNING_PATHS_KEY = "learning_paths"
    private val USER_PROGRESS_KEY = "user_progress"
    
    /**
     * فئة تمثل مسار تعلم
     */
    data class LearningPath(
        val id: String = UUID.randomUUID().toString(),
        val title: String,
        val description: String,
        val difficulty: String, // سهل، متوسط، صعب
        val estimatedTimeMinutes: Int,
        val stages: List<LearningStage>,
        val prerequisites: List<String> = listOf(), // قائمة معرفات المسارات المطلوبة قبل هذا المسار
        val imageResourceName: String = "" // اسم ملف الصورة في مجلد الموارد
    )
    
    /**
     * فئة تمثل مرحلة في مسار التعلم
     */
    data class LearningStage(
        val id: String = UUID.randomUUID().toString(),
        val title: String,
        val description: String,
        val categoryId: String, // معرف فئة الأسئلة
        val requiredScore: Int, // النتيجة المطلوبة لإكمال المرحلة
        val questionCount: Int, // عدد الأسئلة في المرحلة
        val isOptional: Boolean = false // هل المرحلة اختيارية
    )
    
    /**
     * فئة تمثل تقدم المستخدم في مسار تعلم
     */
    data class UserProgress(
        val pathId: String,
        val stageProgress: MutableMap<String, StageProgress> = mutableMapOf(),
        var isCompleted: Boolean = false,
        var startDate: Long = System.currentTimeMillis(),
        var completionDate: Long? = null
    )
    
    /**
     * فئة تمثل تقدم المستخدم في مرحلة
     */
    data class StageProgress(
        val stageId: String,
        var bestScore: Int = 0,
        var attempts: Int = 0,
        var isCompleted: Boolean = false,
        var completionDate: Long? = null
    )
    
    /**
     * الحصول على جميع مسارات التعلم
     */
    fun getAllLearningPaths(): List<LearningPath> {
        val json = sharedPreferences.getString(LEARNING_PATHS_KEY, null) ?: return getDefaultLearningPaths()
        val type = object : TypeToken<List<LearningPath>>() {}.type
        val paths = gson.fromJson<List<LearningPath>>(json, type)
        return paths ?: getDefaultLearningPaths()
    }
    
    /**
     * الحصول على مسار تعلم بواسطة المعرف
     */
    fun getLearningPathById(pathId: String): LearningPath? {
        return getAllLearningPaths().find { it.id == pathId }
    }
    
    /**
     * إضافة مسار تعلم جديد
     */
    fun addLearningPath(path: LearningPath) {
        val paths = getAllLearningPaths().toMutableList()
        paths.add(path)
        saveLearningPaths(paths)
    }
    
    /**
     * تحديث مسار تعلم موجود
     */
    fun updateLearningPath(path: LearningPath) {
        val paths = getAllLearningPaths().toMutableList()
        val index = paths.indexOfFirst { it.id == path.id }
        if (index != -1) {
            paths[index] = path
            saveLearningPaths(paths)
        }
    }
    
    /**
     * حذف مسار تعلم
     */
    fun deleteLearningPath(pathId: String) {
        val paths = getAllLearningPaths().toMutableList()
        paths.removeIf { it.id == pathId }
        saveLearningPaths(paths)
        
        // حذف تقدم المستخدم المرتبط بهذا المسار
        val progress = getUserProgress().toMutableMap()
        progress.remove(pathId)
        saveUserProgress(progress)
    }
    
    /**
     * حفظ مسارات التعلم
     */
    private fun saveLearningPaths(paths: List<LearningPath>) {
        val json = gson.toJson(paths)
        sharedPreferences.edit().putString(LEARNING_PATHS_KEY, json).apply()
    }
    
    /**
     * الحصول على تقدم المستخدم في جميع المسارات
     */
    fun getUserProgress(): Map<String, UserProgress> {
        val json = sharedPreferences.getString(USER_PROGRESS_KEY, null) ?: return mutableMapOf()
        val type = object : TypeToken<Map<String, UserProgress>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }
    
    /**
     * الحصول على تقدم المستخدم في مسار محدد
     */
    fun getUserProgressForPath(pathId: String): UserProgress? {
        return getUserProgress()[pathId]
    }
    
    /**
     * تحديث تقدم المستخدم في مرحلة
     */
    fun updateStageProgress(pathId: String, stageId: String, score: Int) {
        val allProgress = getUserProgress().toMutableMap()
        var pathProgress = allProgress[pathId]
        
        // إنشاء تقدم جديد للمسار إذا لم يكن موجودًا
        if (pathProgress == null) {
            pathProgress = UserProgress(pathId)
            allProgress[pathId] = pathProgress
        }
        
        // الحصول على تقدم المرحلة أو إنشاء واحد جديد
        var stageProgress = pathProgress.stageProgress[stageId]
        if (stageProgress == null) {
            stageProgress = StageProgress(stageId)
            pathProgress.stageProgress[stageId] = stageProgress
        }
        
        // تحديث التقدم
        stageProgress.attempts++
        if (score > stageProgress.bestScore) {
            stageProgress.bestScore = score
        }
        
        // التحقق مما إذا تم إكمال المرحلة
        val path = getLearningPathById(pathId)
        val stage = path?.stages?.find { it.id == stageId }
        if (stage != null && score >= stage.requiredScore && !stageProgress.isCompleted) {
            stageProgress.isCompleted = true
            stageProgress.completionDate = System.currentTimeMillis()
            
            // التحقق مما إذا تم إكمال المسار بأكمله
            checkPathCompletion(pathId, pathProgress)
        }
        
        // حفظ التقدم
        saveUserProgress(allProgress)
    }
    
    /**
     * التحقق مما إذا تم إكمال المسار بأكمله
     */
    private fun checkPathCompletion(pathId: String, progress: UserProgress) {
        val path = getLearningPathById(pathId) ?: return
        
        // التحقق من إكمال جميع المراحل غير الاختيارية
        val allRequiredStagesCompleted = path.stages
            .filter { !it.isOptional }
            .all { stage -> progress.stageProgress[stage.id]?.isCompleted == true }
        
        if (allRequiredStagesCompleted && !progress.isCompleted) {
            progress.isCompleted = true
            progress.completionDate = System.currentTimeMillis()
        }
    }
    
    /**
     * حفظ تقدم المستخدم
     */
    private fun saveUserProgress(progress: Map<String, UserProgress>) {
        val json = gson.toJson(progress)
        sharedPreferences.edit().putString(USER_PROGRESS_KEY, json).apply()
    }
    
    /**
     * الحصول على المسارات المتاحة للمستخدم (التي تم استيفاء متطلباتها المسبقة)
     */
    fun getAvailableLearningPaths(): List<LearningPath> {
        val allPaths = getAllLearningPaths()
        val userProgress = getUserProgress()
        
        return allPaths.filter { path ->
            // إذا لم يكن هناك متطلبات مسبقة، فالمسار متاح
            if (path.prerequisites.isEmpty()) {
                return@filter true
            }
            
            // التحقق من إكمال جميع المتطلبات المسبقة
            path.prerequisites.all { prerequisiteId ->
                userProgress[prerequisiteId]?.isCompleted == true
            }
        }
    }
    
    /**
     * الحصول على نسبة إكمال المسار
     */
    fun getPathCompletionPercentage(pathId: String): Int {
        val path = getLearningPathById(pathId) ?: return 0
        val progress = getUserProgressForPath(pathId) ?: return 0
        
        val totalRequiredStages = path.stages.count { !it.isOptional }
        if (totalRequiredStages == 0) return 0
        
        val completedStages = path.stages
            .filter { !it.isOptional }
            .count { stage -> progress.stageProgress[stage.id]?.isCompleted == true }
        
        return (completedStages * 100) / totalRequiredStages
    }
    
    /**
     * الحصول على المرحلة التالية المقترحة للمستخدم في مسار
     */
    fun getNextSuggestedStage(pathId: String): LearningStage? {
        val path = getLearningPathById(pathId) ?: return null
        val progress = getUserProgressForPath(pathId) ?: return path.stages.firstOrNull()
        
        // البحث عن أول مرحلة غير مكتملة
        return path.stages.find { stage ->
            progress.stageProgress[stage.id]?.isCompleted != true
        }
    }
    
    /**
     * إعادة تعيين تقدم المستخدم في مسار
     */
    fun resetPathProgress(pathId: String) {
        val progress = getUserProgress().toMutableMap()
        progress.remove(pathId)
        saveUserProgress(progress)
    }
    
    /**
     * إعادة تعيين جميع تقدم المستخدم
     */
    fun resetAllProgress() {
        sharedPreferences.edit().remove(USER_PROGRESS_KEY).apply()
    }
    
    /**
     * الحصول على مسارات التعلم الافتراضية
     */
    private fun getDefaultLearningPaths(): List<LearningPath> {
        // إنشاء بعض مسارات التعلم الافتراضية
        val beginnerPath = LearningPath(
            id = "beginner_path",
            title = "مسار المبتدئين",
            description = "مسار تعليمي للمبتدئين يغطي المعلومات العامة الأساسية",
            difficulty = "سهل",
            estimatedTimeMinutes = 30,
            stages = listOf(
                LearningStage(
                    id = "beginner_stage1",
                    title = "أساسيات المعلومات العامة",
                    description = "تعرف على المعلومات العامة الأساسية",
                    categoryId = "general",
                    requiredScore = 7,
                    questionCount = 10
                ),
                LearningStage(
                    id = "beginner_stage2",
                    title = "معلومات جغرافية أساسية",
                    description = "تعرف على أساسيات الجغرافيا",
                    categoryId = "geography",
                    requiredScore = 6,
                    questionCount = 10
                ),
                LearningStage(
                    id = "beginner_stage3",
                    title = "معلومات تاريخية أساسية",
                    description = "تعرف على أهم الأحداث التاريخية",
                    categoryId = "history",
                    requiredScore = 6,
                    questionCount = 10
                )
            )
        )
        
        val intermediatePath = LearningPath(
            id = "intermediate_path",
            title = "مسار المتوسطين",
            description = "مسار تعليمي للمستوى المتوسط يغطي مجموعة متنوعة من المواضيع",
            difficulty = "متوسط",
            estimatedTimeMinutes = 45,
            stages = listOf(
                LearningStage(
                    id = "intermediate_stage1",
                    title = "معلومات علمية",
                    description = "اختبر معلوماتك العلمية",
                    categoryId = "science",
                    requiredScore = 7,
                    questionCount = 10
                ),
                LearningStage(
                    id = "intermediate_stage2",
                    title = "معلومات رياضية",
                    description = "اختبر معلوماتك الرياضية",
                    categoryId = "sports",
                    requiredScore = 7,
                    questionCount = 10
                ),
                LearningStage(
                    id = "intermediate_stage3",
                    title = "معلومات فنية وأدبية",
                    description = "اختبر معلوماتك في الفن والأدب",
                    categoryId = "art",
                    requiredScore = 7,
                    questionCount = 10
                ),
                LearningStage(
                    id = "intermediate_bonus",
                    title = "أسئلة إضافية متنوعة",
                    description = "أسئلة إضافية لتحسين مستواك",
                    categoryId = "general",
                    requiredScore = 8,
                    questionCount = 15,
                    isOptional = true
                )
            ),
            prerequisites = listOf("beginner_path")
        )
        
        val advancedPath = LearningPath(
            id = "advanced_path",
            title = "مسار المتقدمين",
            description = "مسار تعليمي متقدم للمستخدمين ذوي المعرفة العالية",
            difficulty = "صعب",
            estimatedTimeMinutes = 60,
            stages = listOf(
                LearningStage(
                    id = "advanced_stage1",
                    title = "معلومات علمية متقدمة",
                    description = "اختبر معلوماتك العلمية المتقدمة",
                    categoryId = "science",
                    requiredScore = 8,
                    questionCount = 10
                ),
                LearningStage(
                    id = "advanced_stage2",
                    title = "معلومات تاريخية متقدمة",
                    description = "اختبر معلوماتك التاريخية المتقدمة",
                    categoryId = "history",
                    requiredScore = 8,
                    questionCount = 10
                ),
                LearningStage(
                    id = "advanced_stage3",
                    title = "معلومات تقنية",
                    description = "اختبر معلوماتك في مجال التكنولوجيا",
                    categoryId = "technology",
                    requiredScore = 8,
                    questionCount = 10
                ),
                LearningStage(
                    id = "advanced_stage4",
                    title = "ألغاز وأحاجي",
                    description = "اختبر قدراتك في حل الألغاز والأحاجي",
                    categoryId = "puzzles",
                    requiredScore = 7,
                    questionCount = 10
                )
            ),
            prerequisites = listOf("intermediate_path")
        )
        
        return listOf(beginnerPath, intermediatePath, advancedPath)
    }
}