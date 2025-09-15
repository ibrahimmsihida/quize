package com.msihida.soealwasiba9

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.msihida.soealwasiba9.BaseActivity

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        // إعداد شريط الأدوات مع زر العودة
        setSupportActionBar(findViewById(R.id.settings_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        // إضافة شظية الإعدادات إلى النشاط
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    // التعامل مع زر العودة في شريط الأدوات
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                onBackPressedDispatcher.onBackPressed()
            } else {
                @Suppress("DEPRECATION")
                onBackPressed()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // شظية الإعدادات
    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var darkModeManager: DarkModeManager
        private lateinit var notificationManager: NotificationManager
        private lateinit var languageManager: LanguageManager
        private lateinit var learningModeManager: LearningModeManager

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            
            // تهيئة المديرين
            darkModeManager = DarkModeManager(requireContext())
            notificationManager = NotificationManager(requireContext())
            languageManager = LanguageManager(requireContext())
            learningModeManager = LearningModeManager(requireContext())

            // إعداد تفضيل الوضع المظلم
            val darkModePreference = findPreference<SwitchPreferenceCompat>("dark_mode")
            darkModePreference?.isChecked = darkModeManager.isDarkModeEnabled()
            darkModePreference?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                darkModeManager.setDarkMode(enabled)
                true
            }

            // إعداد تفضيل الإشعارات
            val notificationsPreference = findPreference<SwitchPreferenceCompat>("notifications")
            notificationsPreference?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                notificationManager.setNotificationsEnabled(enabled)
                if (enabled) {
                    notificationManager.scheduleReminder()
                    Toast.makeText(requireContext(), getString(R.string.notifications_enabled), Toast.LENGTH_SHORT).show()
                } else {
                    notificationManager.cancelReminder()
                    Toast.makeText(requireContext(), getString(R.string.notifications_disabled), Toast.LENGTH_SHORT).show()
                }
                true
            }

            // تم إزالة إعداد تفضيل اللغة لأن التطبيق يستخدم اللغة العربية فقط

            // إعداد تفضيل وضع التعلم
            val learningModePreference = findPreference<SwitchPreferenceCompat>("learning_mode")
            learningModePreference?.isChecked = learningModeManager.isLearningModeEnabled()
            learningModePreference?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                learningModeManager.setLearningModeEnabled(enabled)
                if (enabled) {
                    Toast.makeText(requireContext(), getString(R.string.learning_mode_enabled), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), getString(R.string.learning_mode_disabled), Toast.LENGTH_SHORT).show()
                }
                true
            }

            // إعداد تفضيل عرض الشرح التلقائي
            val autoExplanationPreference = findPreference<SwitchPreferenceCompat>("auto_explanation")
            autoExplanationPreference?.isChecked = learningModeManager.isAutoExplanationEnabled()
            autoExplanationPreference?.isEnabled = learningModeManager.isLearningModeEnabled()
            autoExplanationPreference?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                learningModeManager.setAutoExplanationEnabled(enabled)
                true
            }

            // تحديث حالة تفضيلات وضع التعلم عند تغيير وضع التعلم
            learningModePreference?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                learningModeManager.setLearningModeEnabled(enabled)
                autoExplanationPreference?.isEnabled = enabled
                true
            }
        }
    }
}