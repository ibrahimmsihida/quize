package com.msihida.soealwasiba9

import android.app.Application
import android.content.Context

/**
 * فئة التطبيق الرئيسية التي تستخدم لتهيئة المكونات العامة للتطبيق
 */
class ApplicationClass : Application() {
    // تبسيط الكلاس - إزالة المدراء غير المستخدمة
    
    override fun onCreate() {
        super.onCreate()
        // تبسيط التهيئة - إزالة المعالجة المعقدة
        android.util.Log.d("ApplicationClass", "Application started successfully")
    }
    
    override fun attachBaseContext(base: Context) {
        // تبسيط - استخدام السياق الأساسي مباشرة
        super.attachBaseContext(base)
    }
}