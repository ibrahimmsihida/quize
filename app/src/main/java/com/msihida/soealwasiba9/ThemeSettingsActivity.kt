package com.msihida.soealwasiba9

import android.os.Bundle
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton

class ThemeSettingsActivity : BaseActivity() {
    
    private lateinit var themeRadioGroup: RadioGroup
    private lateinit var cardThemeRadioGroup: RadioGroup
    private lateinit var lightThemeRadio: RadioButton
    private lateinit var darkThemeRadio: RadioButton
    private lateinit var autoThemeRadio: RadioButton
    private lateinit var defaultCardRadio: RadioButton
    private lateinit var colorfulCardRadio: RadioButton
    private lateinit var minimalCardRadio: RadioButton
    private lateinit var gradientCardRadio: RadioButton
    private lateinit var applyButton: MaterialButton
    private lateinit var previewCard: CardView
    
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_settings)
        
        // إعداد شريط الأدوات
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "إعدادات الثيم"
        }
        
        initViews()
        themeManager = ThemeManager.getInstance(this)
        loadCurrentSettings()
        setupListeners()
    }
    
    private fun initViews() {
        themeRadioGroup = findViewById(R.id.themeRadioGroup)
        cardThemeRadioGroup = findViewById(R.id.cardThemeRadioGroup)
        lightThemeRadio = findViewById(R.id.lightThemeRadio)
        darkThemeRadio = findViewById(R.id.darkThemeRadio)
        autoThemeRadio = findViewById(R.id.autoThemeRadio)
        defaultCardRadio = findViewById(R.id.defaultCardRadio)
        colorfulCardRadio = findViewById(R.id.colorfulCardRadio)
        minimalCardRadio = findViewById(R.id.minimalCardRadio)
        gradientCardRadio = findViewById(R.id.gradientCardRadio)
        applyButton = findViewById(R.id.applyButton)
        previewCard = findViewById(R.id.previewCard)
    }
    
    private fun loadCurrentSettings() {
        // تحميل إعدادات الثيم الحالية
        val currentTheme = themeManager.getSavedThemeMode()
        when (currentTheme) {
            ThemeManager.THEME_LIGHT -> lightThemeRadio.isChecked = true
            ThemeManager.THEME_DARK -> darkThemeRadio.isChecked = true
            ThemeManager.THEME_AUTO -> autoThemeRadio.isChecked = true
        }
        
        // تحميل إعدادات ثيم البطاقات الحالية
        val currentCardTheme = themeManager.getSavedCardTheme()
        when (currentCardTheme) {
            ThemeManager.CARD_THEME_DEFAULT -> defaultCardRadio.isChecked = true
            ThemeManager.CARD_THEME_COLORFUL -> colorfulCardRadio.isChecked = true
            ThemeManager.CARD_THEME_MINIMAL -> minimalCardRadio.isChecked = true
            ThemeManager.CARD_THEME_GRADIENT -> gradientCardRadio.isChecked = true
        }
        
        updatePreview()
    }
    
    private fun setupListeners() {
        // مستمع تغيير ثيم التطبيق
        themeRadioGroup.setOnCheckedChangeListener { _, _ ->
            updatePreview()
        }
        
        // مستمع تغيير ثيم البطاقات
        cardThemeRadioGroup.setOnCheckedChangeListener { _, _ ->
            updatePreview()
        }
        
        // مستمع زر التطبيق
        applyButton.setOnClickListener {
            applySelectedThemes()
        }
    }
    
    private fun updatePreview() {
        val selectedCardTheme = getSelectedCardTheme()
        val backgroundResource = themeManager.getCardBackground(selectedCardTheme)
        previewCard.setBackgroundResource(backgroundResource)
    }
    
    private fun getSelectedTheme(): String {
        return when (themeRadioGroup.checkedRadioButtonId) {
            R.id.lightThemeRadio -> ThemeManager.THEME_LIGHT
            R.id.darkThemeRadio -> ThemeManager.THEME_DARK
            R.id.autoThemeRadio -> ThemeManager.THEME_AUTO
            else -> ThemeManager.THEME_AUTO
        }
    }
    
    private fun getSelectedCardTheme(): String {
        return when (cardThemeRadioGroup.checkedRadioButtonId) {
            R.id.defaultCardRadio -> ThemeManager.CARD_THEME_DEFAULT
            R.id.colorfulCardRadio -> ThemeManager.CARD_THEME_COLORFUL
            R.id.minimalCardRadio -> ThemeManager.CARD_THEME_MINIMAL
            R.id.gradientCardRadio -> ThemeManager.CARD_THEME_GRADIENT
            else -> ThemeManager.CARD_THEME_DEFAULT
        }
    }
    
    private fun applySelectedThemes() {
        val selectedTheme = getSelectedTheme()
        val selectedCardTheme = getSelectedCardTheme()
        
        // حفظ الإعدادات
        themeManager.saveThemeMode(selectedTheme)
        themeManager.saveCardTheme(selectedCardTheme)
        
        // إعادة إنشاء النشاط لتطبيق التغييرات
        recreate()
        
        // إظهار رسالة تأكيد
        android.widget.Toast.makeText(this, "تم تطبيق الثيم بنجاح", android.widget.Toast.LENGTH_SHORT).show()
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