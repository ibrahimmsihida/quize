package com.msihida.soealwasiba9

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * BaseActivity - الفئة الأساسية لجميع الأنشطة
 * مبسطة لتجنب الأخطاء
 */
abstract class BaseActivity : AppCompatActivity() {
    // تبسيط - إزالة المدراء المعقدة
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // تبسيط التهيئة - إزالة المعالجة المعقدة
    }
    
    override fun attachBaseContext(newBase: Context) {
        // تبسيط - استخدام السياق الأساسي مباشرة
        super.attachBaseContext(newBase)
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // تبسيط - إزالة معالجة تغيير التكوين المعقدة
    }
}