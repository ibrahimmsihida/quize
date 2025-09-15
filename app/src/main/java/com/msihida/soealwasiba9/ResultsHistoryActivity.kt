package com.msihida.soealwasiba9

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

class ResultsHistoryActivity : AppCompatActivity() {
    
    private lateinit var resultsManager: QuizResultsManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ResultsHistoryAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var totalQuizzesText: TextView
    private lateinit var averageScoreText: TextView
    private lateinit var bestScoreText: TextView
    private lateinit var totalTimeText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results_history)
        
        // إعداد شريط الأدوات
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "تاريخ النتائج"
        }
        
        // تهيئة المكونات
        initViews()
        setupRecyclerView()
        setupTabs()
        loadStatistics()
        loadResults("all")
    }
    
    private fun initViews() {
        resultsManager = QuizResultsManager(this)
        recyclerView = findViewById(R.id.recyclerViewResults)
        tabLayout = findViewById(R.id.tabLayoutFilter)
        totalQuizzesText = findViewById(R.id.textTotalQuizzes)
        averageScoreText = findViewById(R.id.textAverageScore)
        bestScoreText = findViewById(R.id.textBestScore)
        totalTimeText = findViewById(R.id.textTotalTime)
    }
    
    private fun setupRecyclerView() {
        adapter = ResultsHistoryAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("الكل"))
        tabLayout.addTab(tabLayout.newTab().setText("سهل"))
        tabLayout.addTab(tabLayout.newTab().setText("متوسط"))
        tabLayout.addTab(tabLayout.newTab().setText("صعب"))
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadResults("all")
                    1 -> loadResults("easy")
                    2 -> loadResults("medium")
                    3 -> loadResults("hard")
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    private fun loadStatistics() {
        val stats = resultsManager.getOverallStats()
        val detailedStatsManager = DetailedStatsManager(this)
        
        totalQuizzesText.text = "${stats.totalQuizzes} اختبار"
        averageScoreText.text = String.format("%.1f%%", stats.averageScore)
        bestScoreText.text = "${stats.bestScore}%"
        
        val totalTimeSpent = detailedStatsManager.getTotalTimeSpent()
        val hours = totalTimeSpent / 3600
        val minutes = (totalTimeSpent % 3600) / 60
        totalTimeText.text = if (hours > 0) {
            "${hours}س ${minutes}د"
        } else {
            "${minutes}د"
        }
    }
    
    private fun loadResults(difficulty: String) {
        val results = if (difficulty == "all") {
            resultsManager.getAllResults()
        } else {
            resultsManager.getResultsByDifficulty(difficulty)
        }
        
        adapter.updateResults(results)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}