package com.msihida.soealwasiba9

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast

class MainActivity : BaseActivity() {

    // تم حذف SoundManager نهائياً لتجنب مشاكل الصوت
    // private val soundManager by lazy { SoundManager.getInstance(applicationContext) }
    
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // إعداد شريط الأدوات أولاً
        setSupportActionBar(findViewById(R.id.toolbar))
        
        // تهيئة ThemeManager
        themeManager = ThemeManager.getInstance(this)
        
        // إعداد زر البدء (بدون أصوات)
        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            startActivity(Intent(this, CategoriesActivity::class.java))
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_theme -> {
                // تبديل الوضع المظلم
                themeManager.toggleDarkMode()
                Toast.makeText(this, "تم تبديل الوضع", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_settings -> {
                // الانتقال إلى شاشة الإعدادات
                val intent = Intent(this, com.msihida.soealwasiba9.SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // تم حذف تحرير SoundManager
    }
}