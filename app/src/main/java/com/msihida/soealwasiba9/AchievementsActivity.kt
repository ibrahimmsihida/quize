package com.msihida.soealwasiba9

import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * نشاط لعرض إنجازات المستخدم
 */
class AchievementsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var achievementAdapter: AchievementAdapter
    private lateinit var achievementManager: AchievementManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)

        // تهيئة مدير الإنجازات
        achievementManager = AchievementManager(this)

        // تهيئة RecyclerView
        recyclerView = findViewById(R.id.recyclerViewAchievements)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // تحديث الإنجازات والتحقق من الإنجازات الجديدة
        val newlyUnlockedAchievements = achievementManager.checkAchievements()
        
        // إذا كانت هناك إنجازات جديدة، عرض رسالة تهنئة
        if (newlyUnlockedAchievements.isNotEmpty()) {
            showNewAchievementsDialog(newlyUnlockedAchievements)
        }

        // تهيئة المحول وتعيينه للـ RecyclerView
        achievementAdapter = AchievementAdapter(this, achievementManager.getAllAchievements())
        recyclerView.adapter = achievementAdapter

        // تعيين مستمع للزر "العودة إلى الفئات"
        val buttonBackToCategories = findViewById<Button>(R.id.buttonBackToCategories)
        buttonBackToCategories.setOnClickListener {
            finish()
        }
    }

    /**
     * عرض حوار للإنجازات الجديدة
     */
    private fun showNewAchievementsDialog(achievements: List<Achievement>) {
        val achievementTitles = achievements.joinToString("\n") { "• ${it.title}" }
        val message = getString(R.string.new_achievements_unlocked) + "\n" + achievementTitles

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.congratulations)
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .setIcon(R.drawable.ic_launcher)
            .show()
    }
}