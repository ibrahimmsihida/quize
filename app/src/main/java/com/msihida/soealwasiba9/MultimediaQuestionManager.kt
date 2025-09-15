package com.msihida.soealwasiba9

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.VideoView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * فئة لإدارة الأسئلة متعددة الوسائط (صور، صوت، فيديو)
 */
class MultimediaQuestionManager(private val context: Context) {
    private val TAG = "MultimediaQuestionMgr"
    private val gson = Gson()
    private var mediaPlayer: MediaPlayer? = null
    
    // أنواع الوسائط المدعومة
    enum class MediaType {
        IMAGE, AUDIO, VIDEO, NONE
    }
    
    /**
     * فئة تمثل وسائط متعددة مرتبطة بسؤال
     */
    data class QuestionMedia(
        val questionId: String,
        val mediaType: MediaType,
        val mediaPath: String, // المسار النسبي للملف في مجلد الأصول أو التخزين الداخلي
        val duration: Int = 0, // المدة بالثواني للصوت والفيديو
        val autoPlay: Boolean = false, // تشغيل تلقائي للوسائط
        val loop: Boolean = false, // تكرار الوسائط
        val caption: String = "" // نص وصفي للوسائط
    )
    
    /**
     * عرض الوسائط المتعددة في العناصر المرئية المناسبة
     * @param media معلومات الوسائط المتعددة
     * @param imageView عنصر ImageView لعرض الصور (يمكن أن يكون null إذا كان النوع ليس صورة)
     * @param videoView عنصر VideoView لعرض الفيديو (يمكن أن يكون null إذا كان النوع ليس فيديو)
     * @param onPrepared دالة تستدعى عند اكتمال تحميل الوسائط
     * @param onError دالة تستدعى عند حدوث خطأ
     */
    fun displayMedia(
        media: QuestionMedia,
        imageView: ImageView? = null,
        videoView: VideoView? = null,
        onPrepared: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        when (media.mediaType) {
            MediaType.IMAGE -> displayImage(media, imageView, onPrepared, onError)
            MediaType.AUDIO -> playAudio(media, onPrepared, onError)
            MediaType.VIDEO -> playVideo(media, videoView, onPrepared, onError)
            MediaType.NONE -> onPrepared()
        }
    }
    
    /**
     * عرض صورة في ImageView
     */
    private fun displayImage(
        media: QuestionMedia,
        imageView: ImageView?,
        onPrepared: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (imageView == null) {
            onError("ImageView is null")
            return
        }
        
        try {
            // محاولة تحميل الصورة من مجلد الأصول
            val inputStream = context.assets.open(media.mediaPath)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
            imageView.visibility = ImageView.VISIBLE
            onPrepared()
        } catch (e: IOException) {
            // إذا فشل التحميل من الأصول، نحاول من التخزين الداخلي
            try {
                val file = File(context.filesDir, media.mediaPath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    imageView.setImageBitmap(bitmap)
                    imageView.visibility = ImageView.VISIBLE
                    onPrepared()
                } else {
                    onError("Image file not found: ${media.mediaPath}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image: ${e.message}")
                onError("Error loading image: ${e.message}")
            }
        }
    }
    
    /**
     * تشغيل ملف صوتي
     */
    private fun playAudio(
        media: QuestionMedia,
        onPrepared: () -> Unit,
        onError: (String) -> Unit
    ) {
        // إيقاف أي تشغيل سابق
        stopMedia()
        
        try {
            mediaPlayer = MediaPlayer()
            
            // محاولة تحميل الصوت من مجلد الأصول
            val descriptor = context.assets.openFd(media.mediaPath)
            mediaPlayer?.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
            descriptor.close()
            
            setupMediaPlayer(media, onPrepared, onError)
        } catch (e: IOException) {
            // إذا فشل التحميل من الأصول، نحاول من التخزين الداخلي
            try {
                val file = File(context.filesDir, media.mediaPath)
                if (file.exists()) {
                    mediaPlayer?.setDataSource(file.absolutePath)
                    setupMediaPlayer(media, onPrepared, onError)
                } else {
                    onError("Audio file not found: ${media.mediaPath}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing audio: ${e.message}")
                onError("Error playing audio: ${e.message}")
            }
        }
    }
    
    /**
     * إعداد مشغل الوسائط
     */
    private fun setupMediaPlayer(
        media: QuestionMedia,
        onPrepared: () -> Unit,
        onError: (String) -> Unit
    ) {
        mediaPlayer?.setOnPreparedListener {
            if (media.autoPlay) {
                mediaPlayer?.start()
            }
            if (media.loop) {
                mediaPlayer?.isLooping = true
            }
            onPrepared()
        }
        
        mediaPlayer?.setOnErrorListener { _, what, extra ->
            Log.e(TAG, "MediaPlayer error: $what, $extra")
            onError("MediaPlayer error: $what, $extra")
            true
        }
        
        mediaPlayer?.prepareAsync()
    }
    
    /**
     * تشغيل ملف فيديو
     */
    private fun playVideo(
        media: QuestionMedia,
        videoView: VideoView?,
        onPrepared: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (videoView == null) {
            onError("VideoView is null")
            return
        }
        
        try {
            // محاولة تحميل الفيديو من مجلد الأصول
            val assetPath = "android.resource://${context.packageName}/raw/${getFileNameWithoutExtension(media.mediaPath)}"
            videoView.setVideoURI(Uri.parse(assetPath))
            
            videoView.setOnPreparedListener { mp ->
                videoView.visibility = VideoView.VISIBLE
                if (media.autoPlay) {
                    videoView.start()
                }
                if (media.loop) {
                    mp.isLooping = true
                }
                onPrepared()
            }
            
            videoView.setOnErrorListener { _, what, extra ->
                // إذا فشل التحميل من الموارد، نحاول من التخزين الداخلي
                try {
                    val file = File(context.filesDir, media.mediaPath)
                    if (file.exists()) {
                        videoView.setVideoPath(file.absolutePath)
                        return@setOnErrorListener false
                    } else {
                        Log.e(TAG, "Video file not found: ${media.mediaPath}")
                        onError("Video file not found: ${media.mediaPath}")
                        return@setOnErrorListener true
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error playing video: ${e.message}")
                    onError("Error playing video: ${e.message}")
                    return@setOnErrorListener true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up video: ${e.message}")
            onError("Error setting up video: ${e.message}")
        }
    }
    
    /**
     * إيقاف تشغيل الوسائط الحالية
     */
    fun stopMedia() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
        }
        mediaPlayer?.release()
        mediaPlayer = null
    }
    
    /**
     * إيقاف مؤقت للوسائط
     */
    fun pauseMedia() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }
    
    /**
     * استئناف تشغيل الوسائط
     */
    fun resumeMedia() {
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer?.start()
        }
    }
    
    /**
     * حفظ ملف وسائط في التخزين الداخلي
     * @param inputStream تيار البيانات للملف
     * @param fileName اسم الملف للحفظ
     * @return المسار النسبي للملف المحفوظ
     */
    fun saveMediaFile(inputStream: InputStream, fileName: String): String {
        val file = File(context.filesDir, fileName)
        try {
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var read: Int
            
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            
            return fileName
        } catch (e: IOException) {
            Log.e(TAG, "Error saving media file: ${e.message}")
            throw e
        }
    }
    
    /**
     * الحصول على اسم الملف بدون امتداد
     */
    private fun getFileNameWithoutExtension(path: String): String {
        val fileName = path.substring(path.lastIndexOf('/') + 1)
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex > 0) fileName.substring(0, dotIndex) else fileName
    }
    
    /**
     * تحويل صورة إلى Bitmap
     */
    fun getBitmapFromAsset(imagePath: String): Bitmap? {
        return try {
            val inputStream = context.assets.open(imagePath)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            Log.e(TAG, "Error loading bitmap from asset: ${e.message}")
            null
        }
    }
    
    /**
     * تحميل مسبق للوسائط المتعددة
     * @param media معلومات الوسائط المتعددة
     * @param onComplete دالة تستدعى عند اكتمال التحميل المسبق
     */
    fun preloadMedia(media: QuestionMedia, onComplete: () -> Unit) {
        when (media.mediaType) {
            MediaType.IMAGE -> {
                // تحميل مسبق للصورة
                Handler(Looper.getMainLooper()).post {
                    try {
                        context.assets.open(media.mediaPath).close()
                        onComplete()
                    } catch (e: IOException) {
                        try {
                            val file = File(context.filesDir, media.mediaPath)
                            if (file.exists()) {
                                onComplete()
                            } else {
                                Log.e(TAG, "Image file not found for preloading: ${media.mediaPath}")
                                onComplete()
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error preloading image: ${e.message}")
                            onComplete()
                        }
                    }
                }
            }
            MediaType.AUDIO, MediaType.VIDEO -> {
                // تحميل مسبق للصوت والفيديو
                val tempMediaPlayer = MediaPlayer()
                try {
                    val descriptor = context.assets.openFd(media.mediaPath)
                    tempMediaPlayer.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                    descriptor.close()
                    
                    tempMediaPlayer.setOnPreparedListener {
                        tempMediaPlayer.release()
                        onComplete()
                    }
                    
                    tempMediaPlayer.setOnErrorListener { _, _, _ ->
                        tempMediaPlayer.release()
                        onComplete()
                        true
                    }
                    
                    tempMediaPlayer.prepareAsync()
                } catch (e: IOException) {
                    tempMediaPlayer.release()
                    onComplete()
                }
            }
            MediaType.NONE -> onComplete()
        }
    }
    
    /**
     * تنظيف الموارد عند الانتهاء
     */
    fun cleanup() {
        stopMedia()
    }
}