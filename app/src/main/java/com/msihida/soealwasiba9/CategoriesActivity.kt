package com.msihida.soealwasiba9

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CategoriesActivity : BaseActivity(), CategoryAdapter.OnCategoryClickListener {

    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var statsButton: MaterialButton
    private lateinit var dailyChallengeButton: MaterialButton
    private lateinit var achievementsButton: MaterialButton
    private lateinit var resultsHistoryButton: MaterialButton
    private lateinit var themeSettingsButton: MaterialButton
    private lateinit var searchView: SearchView
    private lateinit var quizDatabase: QuizDatabase
    private lateinit var soundManager: SoundManager
    
    // قائمة الفئات الأصلية والمفلترة
    private lateinit var allCategories: List<QuestionCategory>
    private var filteredCategories = mutableListOf<QuestionCategory>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)
        
        // تهيئة قاعدة بيانات الأسئلة
        quizDatabase = QuizDatabase(this)
        
        // تهيئة SoundManager
        soundManager = SoundManager.getInstance(this)
        
        // تهيئة عناصر واجهة المستخدم
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView)
        statsButton = findViewById(R.id.statsButton)
        dailyChallengeButton = findViewById(R.id.dailyChallengeButton)
        achievementsButton = findViewById(R.id.achievementsButton)
        resultsHistoryButton = findViewById(R.id.resultsHistoryButton)
        themeSettingsButton = findViewById(R.id.themeSettingsButton)
        searchView = findViewById(R.id.searchView)
        
        // إعداد RecyclerView
        setupRecyclerView()
        
        // إضافة مستمع الحدث لزر الإحصائيات
        statsButton.setOnClickListener {
            soundManager.playClickSound()
            // الانتقال إلى شاشة الإحصائيات
            val intent = Intent(this, StatsActivity::class.java)
            startActivity(intent)
        }
        
        // إضافة مستمع الحدث لزر تحدي اليوم
        dailyChallengeButton.setOnClickListener {
            soundManager.playClickSound()
            // الانتقال إلى شاشة تحدي اليوم
            val intent = Intent(this, DailyChallengeActivity::class.java)
            startActivity(intent)
        }
        
        // إضافة مستمع الحدث لزر الإنجازات
        achievementsButton.setOnClickListener {
            soundManager.playClickSound()
            // الانتقال إلى شاشة الإنجازات
            val intent = Intent(this, AchievementsActivity::class.java)
            startActivity(intent)
        }
        
        // إضافة مستمع الحدث لزر تاريخ النتائج
        resultsHistoryButton.setOnClickListener {
            soundManager.playClickSound()
            // الانتقال إلى شاشة تاريخ النتائج
            val intent = Intent(this, ResultsHistoryActivity::class.java)
            startActivity(intent)
        }
        
        // إضافة مستمع الحدث لزر إعدادات الثيم
        themeSettingsButton.setOnClickListener {
            soundManager.playClickSound()
            // الانتقال إلى شاشة إعدادات الثيم
            val intent = Intent(this, ThemeSettingsActivity::class.java)
            startActivity(intent)
        }
    }
    
    /**
     * إعداد RecyclerView لعرض الفئات
     */
    private fun setupRecyclerView() {
        // الحصول على قائمة الفئات
        allCategories = quizDatabase.getAllCategories()
        filteredCategories.addAll(allCategories)
        
        // إنشاء محول الفئات
        val adapter = CategoryAdapter(filteredCategories, this)
        
        // تعيين المحول والتخطيط للـ RecyclerView
        categoriesRecyclerView.adapter = adapter
        categoriesRecyclerView.layoutManager = GridLayoutManager(this, 1) // يمكن تغييره إلى 2 للعرض في شبكة
        
        // إعداد وظيفة البحث
        setupSearch()
    }
    
    /**
     * إعداد وظيفة البحث
     */
    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterCategories(newText)
                return true
            }
        })
    }
    
    /**
     * تصفية الفئات حسب نص البحث
     */
    private fun filterCategories(query: String?) {
        filteredCategories.clear()
        
        if (query.isNullOrEmpty()) {
            filteredCategories.addAll(allCategories)
        } else {
            val searchQuery = query.lowercase()
            allCategories.forEach { category ->
                if (category.name.lowercase().contains(searchQuery) ||
                    category.description.lowercase().contains(searchQuery)) {
                    filteredCategories.add(category)
                }
            }
        }
        
        // تحديث RecyclerView
        categoriesRecyclerView.adapter?.notifyDataSetChanged()
    }
    
    /**
     * إنشاء قائمة الخيارات في شريط العنوان
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.categories_menu, menu)
        return true
    }
    
    /**
     * معالجة النقر على عناصر القائمة
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * عرض مربع حوار التصفية
     */
    private fun showFilterDialog() {
        val difficulties = arrayOf(
            getString(R.string.difficulty_easy),
            getString(R.string.difficulty_medium),
            getString(R.string.difficulty_hard)
        )
        
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.filter_categories))
            .setItems(difficulties) { _, which ->
                filterByDifficulty(when (which) {
                    0 -> Difficulty.EASY
                    1 -> Difficulty.MEDIUM
                    else -> Difficulty.HARD
                })
            }
            .show()
    }
    
    /**
     * تصفية الفئات حسب مستوى الصعوبة
     */
    private fun filterByDifficulty(difficulty: Difficulty) {
        filteredCategories.clear()
        
        allCategories.forEach { category ->
            if (category.difficulty == difficulty) {
                filteredCategories.add(category)
            }
        }
        
        // تحديث RecyclerView
        categoriesRecyclerView.adapter?.notifyDataSetChanged()
    }
    
    /**
     * عرض مربع حوار الترتيب
     */
    private fun showSortDialog() {
        val sortOptions = arrayOf(
            getString(R.string.sort_by_name),
            getString(R.string.sort_by_difficulty)
        )
        
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.sort_by))
            .setItems(sortOptions) { _, which ->
                when (which) {
                    0 -> sortByName()
                    1 -> sortByDifficulty()
                }
            }
            .show()
    }
    
    /**
     * ترتيب الفئات حسب الاسم
     */
    private fun sortByName() {
        filteredCategories.sortBy { it.name }
        categoriesRecyclerView.adapter?.notifyDataSetChanged()
    }
    
    /**
     * ترتيب الفئات حسب مستوى الصعوبة
     */
    private fun sortByDifficulty() {
        filteredCategories.sortBy { it.difficulty.ordinal }
        categoriesRecyclerView.adapter?.notifyDataSetChanged()
    }
    
    /**
     * معالجة النقر على فئة
     */
    override fun onCategoryClick(category: QuestionCategory) {
        soundManager.playClickSound()
        // الانتقال إلى شاشة اختيار الصعوبة مع تمرير معرف الفئة
        val intent = Intent(this, DifficultySelectionActivity::class.java)
        intent.putExtra("categoryId", category.id)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}