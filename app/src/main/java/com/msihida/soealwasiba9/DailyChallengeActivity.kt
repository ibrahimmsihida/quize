package com.msihida.soealwasiba9

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

/**
 * نشاط لعرض وبدء تحدي اليوم
 */
class DailyChallengeActivity : BaseActivity() {

    private lateinit var textViewDate: TextView
    private lateinit var textViewStatus: TextView
    private lateinit var buttonStartChallenge: Button
    private lateinit var buttonBackToCategories: Button

    private lateinit var quizDatabase: QuizDatabase
    private lateinit var dailyChallenge: DailyChallenge
    private lateinit var userStats: UserStats

    // مفتاح لتخزين تاريخ آخر تحدي تم إكماله
    private val LAST_CHALLENGE_DATE_KEY = "last_challenge_date"
    private val sharedPreferences by lazy { getSharedPreferences("daily_challenge", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_challenge)

        // تهيئة قاعدة البيانات والتحدي اليومي وإحصائيات المستخدم
        quizDatabase = QuizDatabase(this)
        dailyChallenge = DailyChallenge(quizDatabase)
        userStats = UserStats(this)

        // تهيئة عناصر واجهة المستخدم
        textViewDate = findViewById(R.id.textViewDailyChallengeDate)
        textViewStatus = findViewById(R.id.textViewDailyChallengeStatus)
        buttonStartChallenge = findViewById(R.id.buttonStartDailyChallenge)
        buttonBackToCategories = findViewById(R.id.buttonBackToCategories)

        // عرض التاريخ الحالي
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(dailyChallenge.getCurrentDate().time)
        textViewDate.text = currentDate

        // التحقق من حالة التحدي اليومي
        checkDailyChallengeStatus()

        // تعيين مستمع للزر "بدء التحدي"
        buttonStartChallenge.setOnClickListener {
            startDailyChallenge()
        }

        // تعيين مستمع للزر "العودة إلى الفئات"
        buttonBackToCategories.setOnClickListener {
            finish()
        }
    }

    /**
     * التحقق من حالة التحدي اليومي
     */
    private fun checkDailyChallengeStatus() {
        // الحصول على تاريخ آخر تحدي تم إكماله
        val lastChallengeTimestamp = sharedPreferences.getLong(LAST_CHALLENGE_DATE_KEY, 0)
        val lastChallengeDate = Calendar.getInstance()
        lastChallengeDate.timeInMillis = lastChallengeTimestamp

        // التحقق مما إذا كان التحدي اليومي جديدًا
        if (dailyChallenge.isNewDay(lastChallengeDate)) {
            // تحدي جديد متاح
            textViewStatus.text = getString(R.string.new_challenge_available)
            buttonStartChallenge.isEnabled = true
        } else {
            // تم إكمال التحدي اليومي بالفعل
            textViewStatus.text = getString(R.string.challenge_already_completed)
            buttonStartChallenge.isEnabled = false
        }
    }

    /**
     * بدء التحدي اليومي
     */
    private fun startDailyChallenge() {
        // الحصول على أسئلة التحدي
        val challengeQuestions = dailyChallenge.getChallengeQuestions()

        // إذا كانت هناك أسئلة، بدء نشاط الأسئلة
        if (challengeQuestions.isNotEmpty()) {
            val intent = Intent(this, QuestionsActivity::class.java)
            intent.putExtra("categoryId", "daily_challenge")
            intent.putExtra("isDailyChallenge", true)
            startActivity(intent)

            // تحديث تاريخ آخر تحدي تم إكماله
            sharedPreferences.edit()
                .putLong(LAST_CHALLENGE_DATE_KEY, dailyChallenge.getCurrentDate().timeInMillis)
                .apply()
        }
    }

    override fun onResume() {
        super.onResume()
        // تحديث حالة التحدي اليومي عند العودة إلى النشاط
        checkDailyChallengeStatus()
    }
}