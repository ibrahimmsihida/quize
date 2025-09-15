package com.msihida.soealwasiba9

/**
 * تعداد مستويات الصعوبة في التطبيق
 */
enum class DifficultyLevel(val displayName: String, val timePerQuestion: Int, val questionsCount: Int, val scoreMultiplier: Double) {
    EASY("سهل", 45, 5, 1.0),
    MEDIUM("متوسط", 30, 8, 1.5),
    HARD("صعب", 20, 10, 2.0);

    companion object {
        /**
         * الحصول على مستوى الصعوبة من النص
         */
        fun fromString(difficulty: String): DifficultyLevel {
            return when (difficulty.lowercase()) {
                "easy" -> EASY
                "medium" -> MEDIUM
                "hard" -> HARD
                else -> MEDIUM // القيمة الافتراضية
            }
        }

        /**
         * الحصول على جميع مستويات الصعوبة
         */
        fun getAllLevels(): List<DifficultyLevel> {
            return values().toList()
        }
    }

    /**
     * الحصول على وصف مستوى الصعوبة
     */
    fun getDescription(): String {
        return when (this) {
            EASY -> "$questionsCount أسئلة، $timePerQuestion ثانية لكل سؤال"
            MEDIUM -> "$questionsCount أسئلة، $timePerQuestion ثانية لكل سؤال"
            HARD -> "$questionsCount أسئلة، $timePerQuestion ثانية لكل سؤال"
        }
    }

    /**
     * الحصول على لون مستوى الصعوبة
     */
    fun getColor(): String {
        return when (this) {
            EASY -> "#4CAF50" // أخضر
            MEDIUM -> "#FF9800" // برتقالي
            HARD -> "#F44336" // أحمر
        }
    }

    /**
     * الحصول على أيقونة مستوى الصعوبة
     */
    fun getIcon(): String {
        return when (this) {
            EASY -> "🟢"
            MEDIUM -> "🟡"
            HARD -> "🔴"
        }
    }
}