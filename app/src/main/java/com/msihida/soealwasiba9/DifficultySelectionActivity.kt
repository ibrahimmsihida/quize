package com.msihida.soealwasiba9

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class DifficultySelectionActivity : BaseActivity() {

    private lateinit var categoryTitleTextView: TextView
    private lateinit var easyCardView: MaterialCardView
    private lateinit var mediumCardView: MaterialCardView
    private lateinit var hardCardView: MaterialCardView
    private lateinit var backButton: MaterialButton
    private lateinit var soundManager: SoundManager
    
    private var categoryId: String = "general"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty_selection)
        
        // الحصول على معرف الفئة من Intent
        categoryId = intent.getStringExtra("CATEGORY_ID") ?: "general"
        
        // تهيئة SoundManager
        soundManager = SoundManager.getInstance(this)
        
        // تهيئة عناصر واجهة المستخدم
        initializeViews()
        
        // إعداد مستمعي الأحداث
        setupClickListeners()
        
        // تحديث عنوان الفئة
        updateCategoryTitle()
    }
    
    private fun initializeViews() {
        categoryTitleTextView = findViewById(R.id.categoryTitleTextView)
        easyCardView = findViewById(R.id.easyCardView)
        mediumCardView = findViewById(R.id.mediumCardView)
        hardCardView = findViewById(R.id.hardCardView)
        backButton = findViewById(R.id.backButton)
    }
    
    private fun setupClickListeners() {
        // مستوى سهل
        easyCardView.setOnClickListener {
            soundManager.playClickSound()
            startQuizWithDifficulty(DifficultyLevel.EASY)
        }
        
        // مستوى متوسط
        mediumCardView.setOnClickListener {
            soundManager.playClickSound()
            startQuizWithDifficulty(DifficultyLevel.MEDIUM)
        }
        
        // مستوى صعب
        hardCardView.setOnClickListener {
            soundManager.playClickSound()
            startQuizWithDifficulty(DifficultyLevel.HARD)
        }
        
        // زر الرجوع
        backButton.setOnClickListener {
            soundManager.playClickSound()
            finish()
        }
    }
    
    private fun updateCategoryTitle() {
        val quizDatabase = QuizDatabase(this)
        val category = quizDatabase.getCategoryById(categoryId)
        categoryTitleTextView.text = category?.name ?: "اختبار عام"
    }
    
    private fun startQuizWithDifficulty(difficulty: DifficultyLevel) {
        val intent = Intent(this, QuestionsActivity::class.java)
        intent.putExtra("CATEGORY_ID", categoryId)
        intent.putExtra("DIFFICULTY_LEVEL", difficulty.name)
        startActivity(intent)
        finish() // إنهاء هذا النشاط بعد الانتقال
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}