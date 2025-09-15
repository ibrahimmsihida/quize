package com.msihida.soealwasiba9

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class CategoriesActivity : BaseActivity() {

    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var statsButton: MaterialButton
    private lateinit var dailyChallengeButton: MaterialButton
    private lateinit var achievementsButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)
        
        // تهيئة عناصر واجهة المستخدم
        initializeViews()
        
        // إعداد مستمعي الأحداث
        setupClickListeners()
        
        // إعداد قائمة الفئات
        setupCategoriesList()
    }
    
    private fun initializeViews() {
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView)
        statsButton = findViewById(R.id.statsButton)
        dailyChallengeButton = findViewById(R.id.dailyChallengeButton)
        achievementsButton = findViewById(R.id.achievementsButton)
    }
    
    private fun setupClickListeners() {
        // زر الإحصائيات
        statsButton.setOnClickListener {
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }
        
        // زر تحدي اليوم
        dailyChallengeButton.setOnClickListener {
            val intent = Intent(this, DailyChallengeActivity::class.java)
            startActivity(intent)
        }
        
        // زر الإنجازات
        achievementsButton.setOnClickListener {
            val intent = Intent(this, AchievementsActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupCategoriesList() {
        // إنشاء قائمة بسيطة من الفئات
        val categories = listOf(
            "العلوم العامة",
            "التاريخ",
            "الجغرافيا", 
            "الرياضة",
            "الثقافة العامة",
            "التكنولوجيا"
        )
        
        // إعداد RecyclerView
        categoriesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        categoriesRecyclerView.adapter = SimpleCategoryAdapter(categories) { category ->
            // عند النقر على فئة
            val intent = Intent(this, DifficultySelectionActivity::class.java)
            intent.putExtra("CATEGORY_NAME", category)
            startActivity(intent)
        }
    }
}
