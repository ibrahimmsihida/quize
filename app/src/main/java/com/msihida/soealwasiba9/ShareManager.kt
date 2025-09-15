package com.msihida.soealwasiba9

import android.content.Context
import android.content.Intent

/**
 * فئة لإدارة مشاركة نتائج الاختبارات على وسائل التواصل الاجتماعي
 */
class ShareManager(private val context: Context) {

    /**
     * مشاركة نتيجة اختبار
     * @param score النتيجة التي حصل عليها المستخدم
     * @param totalQuestions العدد الإجمالي للأسئلة
     * @param categoryName اسم فئة الاختبار (اختياري)
     */
    fun shareQuizResult(score: Int, totalQuestions: Int, categoryName: String = "") {
        val accuracy = if (totalQuestions > 0) (score.toFloat() / totalQuestions) * 100 else 0f
        
        // إنشاء نص المشاركة
        val shareText = buildShareText(score, totalQuestions, accuracy, categoryName)
        
        // إنشاء قصد المشاركة
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        // عرض منتقي المشاركة
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_via)))
    }
    
    /**
     * مشاركة إنجاز جديد
     * @param achievementTitle عنوان الإنجاز
     */
    fun shareAchievement(achievementTitle: String) {
        // إنشاء نص مشاركة الإنجاز
        val shareText = "🏆 لقد حققت إنجاز جديد في تطبيق ${context.getString(R.string.app_name)}: $achievementTitle"
        
        // إنشاء قصد المشاركة
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.achievement_unlocked))
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        // عرض منتقي المشاركة
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_via)))
    }
    
    /**
     * مشاركة تحدي اليوم
     * @param score النتيجة التي حصل عليها المستخدم
     * @param totalQuestions العدد الإجمالي للأسئلة
     */
    fun shareDailyChallenge(score: Int, totalQuestions: Int) {
        val accuracy = if (totalQuestions > 0) (score.toFloat() / totalQuestions) * 100 else 0f
        
        // إنشاء نص مشاركة تحدي اليوم
        val shareText = "🌟 لقد أكملت تحدي اليوم في تطبيق ${context.getString(R.string.app_name)}! \n" +
                "✅ النتيجة: $score من $totalQuestions \n" +
                "📊 نسبة الدقة: %.1f%%".format(accuracy) + "\n" +
                "🏆 حمل التطبيق وشارك في التحدي اليومي!"
        
        // إنشاء قصد المشاركة
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.daily_challenge))
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        // عرض منتقي المشاركة
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_via)))
    }
    
    /**
     * بناء نص المشاركة
     */
    private fun buildShareText(score: Int, totalQuestions: Int, accuracy: Float, categoryName: String): String {
        val categoryText = if (categoryName.isNotEmpty()) "في فئة $categoryName" else ""
        
        return "🎯 لقد أكملت اختبار $categoryText في تطبيق ${context.getString(R.string.app_name)}! \n" +
               "✅ النتيجة: $score من $totalQuestions \n" +
               "📊 نسبة الدقة: %.1f%%".format(accuracy) + "\n" +
               "🏆 حمل التطبيق وتحدى نفسك!"
    }
}