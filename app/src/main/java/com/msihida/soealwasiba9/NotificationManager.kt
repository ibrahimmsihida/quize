package com.msihida.soealwasiba9

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.*

/**
 * فئة لإدارة الإشعارات في التطبيق
 */
class NotificationManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
    private val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
    private val NOTIFICATION_TIME_HOUR_KEY = "notification_time_hour"
    private val NOTIFICATION_TIME_MINUTE_KEY = "notification_time_minute"
    
    // معرفات القنوات والإشعارات
    private val CHANNEL_ID = "quiz_notifications"
    private val DAILY_CHALLENGE_NOTIFICATION_ID = 1001
    private val REMINDER_NOTIFICATION_ID = 1002
    
    init {
        // إنشاء قناة الإشعارات (مطلوب لنظام Android 8.0 وما فوق)
        createNotificationChannel()
    }
    
    /**
     * إنشاء قناة الإشعارات
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "إشعارات التطبيق"
            val descriptionText = "إشعارات تطبيق سؤالني"
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            // تسجيل القناة في نظام الإشعارات
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * تفعيل أو تعطيل الإشعارات
     * @param enabled حالة تفعيل الإشعارات
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, enabled).apply()
        
        if (enabled) {
            scheduleDailyChallengeReminder()
        } else {
            cancelAllScheduledNotifications()
        }
    }
    
    /**
     * التحقق مما إذا كانت الإشعارات مفعلة
     * @return true إذا كانت الإشعارات مفعلة، false إذا كانت معطلة
     */
    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(NOTIFICATIONS_ENABLED_KEY, true)
    }
    
    /**
     * تعيين وقت الإشعار اليومي
     * @param hour الساعة (0-23)
     * @param minute الدقيقة (0-59)
     */
    fun setDailyNotificationTime(hour: Int, minute: Int) {
        sharedPreferences.edit()
            .putInt(NOTIFICATION_TIME_HOUR_KEY, hour)
            .putInt(NOTIFICATION_TIME_MINUTE_KEY, minute)
            .apply()
        
        // إعادة جدولة الإشعارات بالوقت الجديد
        if (areNotificationsEnabled()) {
            scheduleDailyChallengeReminder()
        }
    }
    
    /**
     * الحصول على ساعة الإشعار اليومي
     * @return ساعة الإشعار (0-23)
     */
    fun getDailyNotificationHour(): Int {
        return sharedPreferences.getInt(NOTIFICATION_TIME_HOUR_KEY, 9) // الافتراضي: 9 صباحًا
    }
    
    /**
     * الحصول على دقيقة الإشعار اليومي
     * @return دقيقة الإشعار (0-59)
     */
    fun getDailyNotificationMinute(): Int {
        return sharedPreferences.getInt(NOTIFICATION_TIME_MINUTE_KEY, 0) // الافتراضي: 0 دقيقة
    }
    
    /**
     * جدولة تذكير يومي (اسم بديل للتوافق)
     */
    fun scheduleReminder() {
        scheduleDailyChallengeReminder()
    }
    
    /**
     * إلغاء التذكير (اسم بديل للتوافق)
     */
    fun cancelReminder() {
        cancelAllScheduledNotifications()
    }
    
    /**
     * جدولة تذكير يومي بتحدي اليوم
     */
    fun scheduleDailyChallengeReminder() {
        if (!areNotificationsEnabled()) return
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.msihida.soealwasiba9.DAILY_CHALLENGE_REMINDER"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_CHALLENGE_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // تعيين وقت الإشعار
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, getDailyNotificationHour())
            set(Calendar.MINUTE, getDailyNotificationMinute())
            set(Calendar.SECOND, 0)
            
            // إذا كان الوقت المحدد قد مر بالفعل اليوم، قم بجدولته لليوم التالي
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        // جدولة الإشعار المتكرر
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
    
    /**
     * إلغاء جميع الإشعارات المجدولة
     */
    fun cancelAllScheduledNotifications() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        
        // إلغاء إشعار تحدي اليوم
        val dailyChallengePendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_CHALLENGE_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(dailyChallengePendingIntent)
        
        // إلغاء إشعار التذكير
        val reminderPendingIntent = PendingIntent.getBroadcast(
            context,
            REMINDER_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(reminderPendingIntent)
    }
    
    /**
     * عرض إشعار تحدي اليوم
     */
    fun showDailyChallengeNotification() {
        if (!areNotificationsEnabled()) return
        
        val intent = Intent(context, DailyChallengeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_daily_challenge)
            .setContentTitle("تحدي اليوم")
            .setContentText("تحدي جديد متاح اليوم! هل أنت مستعد؟")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(DAILY_CHALLENGE_NOTIFICATION_ID, builder.build())
    }
    
    /**
     * عرض إشعار تذكير
     * @param title عنوان الإشعار
     * @param message نص الإشعار
     */
    fun showReminderNotification(title: String, message: String) {
        if (!areNotificationsEnabled()) return
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(REMINDER_NOTIFICATION_ID, builder.build())
    }
}