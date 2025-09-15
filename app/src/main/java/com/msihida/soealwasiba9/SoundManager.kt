package com.msihida.soealwasiba9

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

/**
 * فئة لإدارة تشغيل الأصوات في التطبيق
 */
class SoundManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "SoundManager"
        private const val MAX_STREAMS = 5
        
        @Volatile
        private var INSTANCE: SoundManager? = null
        
        /**
         * الحصول على نسخة وحيدة من مدير الصوت
         */
        @JvmStatic
        fun getInstance(context: Context): SoundManager {
            try {
                return INSTANCE ?: synchronized(this) {
                    INSTANCE ?: SoundManager(context.applicationContext).also { INSTANCE = it }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting SoundManager instance: ${e.message}")
                // إنشاء نسخة جديدة في حالة حدوث خطأ
                INSTANCE = SoundManager(context.applicationContext)
                return INSTANCE!!
            }
        }
    }
    
    private var soundPool: SoundPool? = null
    private var isEnabled = true
    
    // معرفات الأصوات
    private var clickSoundId: Int = 0
    private var correctSoundId: Int = 0
    private var wrongSoundId: Int = 0
    private var gameCompleteSoundId: Int = 0
    
    init {
        initializeSoundPool()
        loadSounds()
    }
    
    /**
     * تهيئة مشغل الصوت
     */
    private fun initializeSoundPool() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            
            soundPool = SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(audioAttributes)
                .build()
                
            Log.d(TAG, "SoundPool initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SoundPool: ${e.message}")
            // تعيين soundPool إلى null لتجنب الأخطاء عند استخدامه
            soundPool = null
        }
    }
    
    /**
     * تحميل جميع الأصوات
     */
    private fun loadSounds() {
        try {
            soundPool?.let { pool ->
                // تحميل الأصوات مع معالجة الأخطاء لكل صوت على حدة
                try {
                    clickSoundId = pool.load(context, R.raw.click, 1)
                    Log.d(TAG, "Click sound loaded successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading click sound: ${e.message}")
                    clickSoundId = 0
                }
                
                try {
                    correctSoundId = pool.load(context, R.raw.correct, 1)
                    Log.d(TAG, "Correct sound loaded successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading correct sound: ${e.message}")
                    correctSoundId = 0
                }
                
                try {
                    wrongSoundId = pool.load(context, R.raw.wrong, 1)
                    Log.d(TAG, "Wrong sound loaded successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading wrong sound: ${e.message}")
                    wrongSoundId = 0
                }
                
                try {
                    gameCompleteSoundId = pool.load(context, R.raw.game_complete, 1)
                    Log.d(TAG, "Game complete sound loaded successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading game complete sound: ${e.message}")
                    gameCompleteSoundId = 0
                }
                
                Log.d(TAG, "Sound loading process completed")
            } ?: run {
                Log.e(TAG, "SoundPool is null, cannot load sounds")
                clickSoundId = 0
                correctSoundId = 0
                wrongSoundId = 0
                gameCompleteSoundId = 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in loadSounds: ${e.message}")
            // تعيين معرفات الأصوات إلى 0 لتجنب الأخطاء عند استخدامها
            clickSoundId = 0
            correctSoundId = 0
            wrongSoundId = 0
            gameCompleteSoundId = 0
        }
    }
    
    /**
     * تشغيل صوت النقر
     */
    fun playClickSound() {
        playSound(clickSoundId)
    }
    
    /**
     * تشغيل صوت الإجابة الصحيحة
     */
    fun playCorrectSound() {
        playSound(correctSoundId)
    }
    
    /**
     * تشغيل صوت الإجابة الخاطئة
     */
    fun playWrongSound() {
        playSound(wrongSoundId)
    }
    
    /**
     * تشغيل صوت إكمال اللعبة
     */
    fun playGameCompleteSound() {
        playSound(gameCompleteSoundId)
    }
    
    /**
     * تشغيل صوت محدد
     */
    private fun playSound(soundId: Int) {
        if (!isEnabled || soundId == 0) return
        
        soundPool?.let { pool ->
            try {
                pool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing sound: ${e.message}")
            }
        }
    }
    
    /**
     * تفعيل أو إلغاء تفعيل الأصوات
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        Log.d(TAG, "Sound enabled: $enabled")
    }
    
    /**
     * التحقق من حالة تفعيل الأصوات
     */
    fun isEnabled(): Boolean {
        return isEnabled
    }
    
    /**
     * تنظيف الموارد
     */
    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            INSTANCE = null
            Log.d(TAG, "SoundManager resources released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing SoundManager: ${e.message}")
        }
    }
}