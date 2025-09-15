package com.msihida.soealwasiba9

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * مستقبل البث لمعالجة الإشعارات المجدولة
 */
class NotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = NotificationManager(context)
        
        when (intent.action) {
            "com.msihida.soealwasiba9.DAILY_CHALLENGE_REMINDER" -> {
                // عرض إشعار تحدي اليوم
                notificationManager.showDailyChallengeNotification()
            }
            "android.intent.action.BOOT_COMPLETED" -> {
                // إعادة جدولة الإشعارات بعد إعادة تشغيل الجهاز
                if (notificationManager.areNotificationsEnabled()) {
                    notificationManager.scheduleDailyChallengeReminder()
                }
            }
        }
    }
}