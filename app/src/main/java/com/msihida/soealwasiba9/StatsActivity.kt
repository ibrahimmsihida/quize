package com.msihida.soealwasiba9

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class StatsActivity : BaseActivity() {

    private lateinit var highScoreValueTextView: TextView
    private lateinit var quizzesValueTextView: TextView
    private lateinit var correctAnswersValueTextView: TextView
    private lateinit var wrongAnswersValueTextView: TextView
    private lateinit var accuracyValueTextView: TextView
    private lateinit var resetStatsButton: Button
    private lateinit var backButton: Button
    
    private lateinit var userStats: UserStats
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        
        // تهيئة إحصائيات المستخدم
        userStats = UserStats(this)
        
        // تهيئة عناصر واجهة المستخدم
        initializeViews()
        
        // عرض الإحصائيات
        displayStats()
        
        // إضافة مستمع الحدث لزر إعادة تعيين الإحصائيات
        resetStatsButton.setOnClickListener {
            showResetConfirmationDialog()
        }
        
        // إضافة مستمع الحدث لزر الرجوع
        backButton.setOnClickListener {
            finish()
        }
    }
    
    /**
     * تهيئة عناصر واجهة المستخدم
     */
    private fun initializeViews() {
        highScoreValueTextView = findViewById(R.id.highScoreValueTextView)
        quizzesValueTextView = findViewById(R.id.quizzesValueTextView)
        correctAnswersValueTextView = findViewById(R.id.correctAnswersValueTextView)
        wrongAnswersValueTextView = findViewById(R.id.wrongAnswersValueTextView)
        accuracyValueTextView = findViewById(R.id.accuracyValueTextView)
        resetStatsButton = findViewById(R.id.resetStatsButton)
        backButton = findViewById(R.id.backButton)
    }
    
    /**
     * عرض إحصائيات المستخدم
     */
    private fun displayStats() {
        // عرض أعلى نتيجة
        val highScore = userStats.getHighScore()
        highScoreValueTextView.text = highScore.toString()
        
        // عرض عدد الاختبارات المكتملة
        val completedQuizzes = userStats.getCompletedQuizzes()
        quizzesValueTextView.text = completedQuizzes.toString()
        
        // عرض عدد الإجابات الصحيحة
        val correctAnswers = userStats.getCorrectAnswers()
        correctAnswersValueTextView.text = correctAnswers.toString()
        
        // عرض عدد الإجابات الخاطئة
        val wrongAnswers = userStats.getWrongAnswers()
        wrongAnswersValueTextView.text = wrongAnswers.toString()
        
        // حساب وعرض نسبة الدقة
        val totalAnswers = correctAnswers + wrongAnswers
        val accuracy = if (totalAnswers > 0) {
            (correctAnswers.toFloat() / totalAnswers.toFloat()) * 100
        } else {
            0f
        }
        accuracyValueTextView.text = String.format("%.1f%%", accuracy)
    }
    
    /**
     * عرض مربع حوار تأكيد إعادة تعيين الإحصائيات
     */
    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("إعادة تعيين الإحصائيات")
            .setMessage("هل أنت متأكد من رغبتك في إعادة تعيين جميع الإحصائيات؟ لا يمكن التراجع عن هذا الإجراء.")
            .setPositiveButton("نعم") { _, _ ->
                // إعادة تعيين الإحصائيات
                userStats.resetAllStats()
                // تحديث العرض
                displayStats()
            }
            .setNegativeButton("لا", null)
            .show()
    }
}