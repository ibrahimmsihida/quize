package com.msihida.soealwasiba9

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import java.io.IOException
import java.nio.charset.Charset

class QuestionsActivity : BaseActivity() {

    // عناصر واجهة المستخدم
    private lateinit var questionCardView: MaterialCardView
    private lateinit var answersCardView: MaterialCardView
    private lateinit var feedbackCardView: MaterialCardView
    private lateinit var helpersCardView: MaterialCardView
    private lateinit var questionTextView: TextView
    private lateinit var answersRadioGroup: RadioGroup
    private lateinit var option1RadioButton: MaterialRadioButton
    private lateinit var option2RadioButton: MaterialRadioButton
    private lateinit var option3RadioButton: MaterialRadioButton
    private lateinit var option4RadioButton: MaterialRadioButton
    private lateinit var feedbackTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var nextButton: MaterialButton
    private lateinit var timeRemainingTextView: TextView
    private lateinit var hintButton: MaterialButton
    private lateinit var fiftyFiftyButton: MaterialButton
    private lateinit var skipButton: MaterialButton
    
    // ألوان أزرار المساعدة
    private val HINT_COLOR = Color.parseColor("#2196F3") // أزرق
    private val FIFTY_FIFTY_COLOR = Color.parseColor("#FF9800") // برتقالي
    private val SKIP_COLOR = Color.parseColor("#4CAF50") // أخضر

    // قائمة الأسئلة
    private lateinit var questions: List<Question>
    
    // متغيرات تتبع حالة اللعبة
    private var currentQuestionIndex = 0
    private var score = 0
    private var answered = false
    private var categoryId = "general"
    private var correctAnswersCount = 0
    private var wrongAnswersCount = 0
    
    // مؤقت للسؤال ومستوى الصعوبة
    private var countDownTimer: CountDownTimer? = null
    private var timeRemaining = 0
    private var questionTimeInSeconds = 30
    private lateinit var difficultyLevel: DifficultyLevel
    private var maxQuestions = 10
    
    // إحصائيات المستخدم
    private lateinit var userStats: UserStats
    
    // قاعدة بيانات الأسئلة
    private lateinit var quizDatabase: QuizDatabase
    
    // أدوات المساعدة
    private lateinit var quizHelper: QuizHelper
    
    // مدير حفظ النتائج
    private lateinit var resultsManager: QuizResultsManager
    
    // متغيرات تتبع الوقت
    private var quizStartTime: Long = 0
    
    // مدير الأصوات
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions)

        // تهيئة إحصائيات المستخدم وقاعدة بيانات الأسئلة وأدوات المساعدة
        userStats = UserStats(this)
        quizDatabase = QuizDatabase(this)
        quizHelper = QuizHelper(this)
        resultsManager = QuizResultsManager(this)
        
        // تسجيل وقت بداية الاختبار
        quizStartTime = System.currentTimeMillis()
        
        // الحصول على معرف الفئة ومستوى الصعوبة من Intent
        categoryId = intent.getStringExtra("CATEGORY_ID") ?: "general"
        val difficultyName = intent.getStringExtra("DIFFICULTY_LEVEL") ?: DifficultyLevel.EASY.name
        difficultyLevel = DifficultyLevel.fromString(difficultyName)
        
        // تطبيق إعدادات مستوى الصعوبة
        questionTimeInSeconds = difficultyLevel.timePerQuestion
        maxQuestions = difficultyLevel.questionsCount
        
        // تهيئة عناصر واجهة المستخدم
        initializeViews()
        
        // تهيئة مدير الأصوات
        initializeSoundManager()
        
        // تحميل الأسئلة من قاعدة البيانات
        loadQuestions()
        
        // عرض السؤال الأول
        if (questions.isNotEmpty()) {
            displayQuestion(currentQuestionIndex)
        }

        // إضافة مستمع الحدث لمجموعة الراديو
        answersRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (!answered) {
                // تشغيل صوت النقر
                soundManager.playClickSound()
                // التحقق من الإجابة عند اختيار أحد الخيارات
                val selectedOptionIndex = when (checkedId) {
                    R.id.option1RadioButton -> 0
                    R.id.option2RadioButton -> 1
                    R.id.option3RadioButton -> 2
                    R.id.option4RadioButton -> 3
                    else -> -1
                }

                if (selectedOptionIndex != -1) {
                    // إيقاف المؤقت
                    countDownTimer?.cancel()
                    
                    // التحقق من الإجابة
                    checkAnswer(selectedOptionIndex)
                }
            }
        }

        // تحميل تأثير تمكين الأزرار
        val buttonEnabledAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_enabled)
        
        // إضافة مستمع الحدث لزر التالي
        nextButton.setOnClickListener {
            // تشغيل صوت النقر
            soundManager.playClickSound()
            // تطبيق تأثير الضغط على الزر
            it.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_press))
            if (answered) {
                // إذا تم الإجابة على السؤال الحالي، انتقل إلى السؤال التالي
                currentQuestionIndex++
                if (currentQuestionIndex < questions.size) {
                    displayQuestion(currentQuestionIndex)
                } else {
                    // إذا كان هذا آخر سؤال، أظهر شاشة النتيجة النهائية
                    showQuizCompletedDialog()
                }
            } else {
                // إذا لم يتم الإجابة بعد، اطلب من المستخدم اختيار إجابة
                feedbackTextView.text = "الرجاء اختيار إجابة"
                feedbackTextView.setTextColor(Color.BLUE)
                feedbackCardView.visibility = View.VISIBLE
                // تطبيق تأثير الاهتزاز على زر التالي
                it.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake))
            }
        }
    }

    /**
     * تهيئة عناصر واجهة المستخدم
     */
    private fun initializeViews() {
        // تهيئة البطاقات
        questionCardView = findViewById(R.id.questionCardView)
        answersCardView = findViewById(R.id.answersCardView)
        feedbackCardView = findViewById(R.id.feedbackCardView)
        helpersCardView = findViewById(R.id.helpersCardView)
        
        // تهيئة عناصر النص والأزرار
        questionTextView = findViewById(R.id.questionTextView)
        answersRadioGroup = findViewById(R.id.answersRadioGroup)
        option1RadioButton = findViewById(R.id.option1RadioButton)
        option2RadioButton = findViewById(R.id.option2RadioButton)
        option3RadioButton = findViewById(R.id.option3RadioButton)
        option4RadioButton = findViewById(R.id.option4RadioButton)
        feedbackTextView = findViewById(R.id.feedbackTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
        nextButton = findViewById(R.id.nextButton)
        timeRemainingTextView = findViewById(R.id.timeRemainingTextView)
        hintButton = findViewById(R.id.hintButton)
        fiftyFiftyButton = findViewById(R.id.fiftyFiftyButton)
        skipButton = findViewById(R.id.skipButton)
        
        // تعيين ألوان أزرار المساعدة
        hintButton.setBackgroundColor(HINT_COLOR)
        fiftyFiftyButton.setBackgroundColor(FIFTY_FIFTY_COLOR)
        skipButton.setBackgroundColor(SKIP_COLOR)
        
        // إضافة مستمعي النقر الطويل لأزرار المساعدة لعرض الوصف
        hintButton.setOnLongClickListener {
            Snackbar.make(it, quizHelper.getHelperDescription("hint"), Snackbar.LENGTH_SHORT).show()
            true
        }
        
        fiftyFiftyButton.setOnLongClickListener {
            Snackbar.make(it, quizHelper.getHelperDescription("fifty_fifty"), Snackbar.LENGTH_SHORT).show()
            true
        }
        
        skipButton.setOnLongClickListener {
            Snackbar.make(it, quizHelper.getHelperDescription("skip"), Snackbar.LENGTH_SHORT).show()
            true
        }
        
        // تحديث نصوص أزرار المساعدة
        updateHelperButtonsText()

        // تعيين النص الأولي للنتيجة
        scoreTextView.text = getString(R.string.score, score)
        
        // إخفاء بطاقة التعليقات في البداية
        feedbackCardView.visibility = View.INVISIBLE
        
        // إضافة مستمعي الأحداث لأزرار المساعدة
        setupHelperButtons()
        
        // تعيين عنوان الشاشة حسب الفئة المختارة
        val category = quizDatabase.getCategoryById(categoryId)
        title = category?.name ?: getString(R.string.app_name)
    }
    
    /**
     * تهيئة مدير الأصوات
     */
    private fun initializeSoundManager() {
        soundManager = SoundManager.getInstance(this)
    }

    /**
     * تحميل الأسئلة من قاعدة البيانات أو ملف JSON
     */
    private fun loadQuestions() {
        // تحميل الأسئلة من قاعدة البيانات حسب الفئة المختارة
        var allQuestions = quizDatabase.getQuestionsByCategory(categoryId)
        
        // إذا لم تكن هناك أسئلة في هذه الفئة، استخدم الأسئلة العامة
        if (allQuestions.isEmpty()) {
            allQuestions = quizDatabase.getQuestionsByCategory("general")
        }
        
        // إذا كانت القائمة لا تزال فارغة، حاول تحميل الأسئلة من ملف JSON
        if (allQuestions.isEmpty()) {
            loadQuestionsFromJson()
            return
        }
        
        // فلترة الأسئلة حسب مستوى الصعوبة
        val filteredQuestions = allQuestions.filter { question ->
            question.difficulty?.equals(difficultyLevel.name.lowercase(), ignoreCase = true) == true
        }
        
        // إذا لم توجد أسئلة بمستوى الصعوبة المحدد، استخدم جميع الأسئلة
        val questionsToUse = if (filteredQuestions.isNotEmpty()) filteredQuestions else allQuestions
        
        // اختيار عدد محدود من الأسئلة حسب مستوى الصعوبة
        questions = questionsToUse.shuffled().take(maxQuestions)
    }
    
    /**
     * تحميل الأسئلة من ملف JSON
     */
    private fun loadQuestionsFromJson() {
        val questionsJsonString = try {
            // قراءة ملف JSON من مجلد الأصول
            assets.open("questions.json").bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            // في حالة حدوث خطأ، استخدم قائمة فارغة
            questions = emptyList()
            return
        }

        // تحويل نص JSON إلى كائنات Question
        val questionsJsonArray = JSONArray(questionsJsonString)
        val allQuestions = mutableListOf<Question>()

        for (i in 0 until questionsJsonArray.length()) {
            val questionJson = questionsJsonArray.getJSONObject(i)
            val questionText = questionJson.getString("question")
            val correctAnswer = questionJson.getInt("correctAnswer")
            
            val optionsJsonArray = questionJson.getJSONArray("options")
            val options = mutableListOf<String>()
            
            for (j in 0 until optionsJsonArray.length()) {
                options.add(optionsJsonArray.getString(j))
            }
            
            // إضافة مستوى الصعوبة إذا كان متوفراً
            val difficulty = if (questionJson.has("difficulty")) {
                questionJson.getString("difficulty")
            } else {
                "easy" // القيمة الافتراضية
            }
            
            // إضافة الفئة إذا كانت متوفرة
            val category = if (questionJson.has("category")) {
                questionJson.getString("category")
            } else {
                "general" // القيمة الافتراضية
            }
            
            allQuestions.add(Question(questionText, options, correctAnswer, difficulty, category))
        }

        // فلترة الأسئلة حسب الفئة ومستوى الصعوبة
        val categoryFiltered = allQuestions.filter { it.category == categoryId || categoryId == "general" }
        val difficultyFiltered = categoryFiltered.filter { 
            it.difficulty?.equals(difficultyLevel.name.lowercase(), ignoreCase = true) == true 
        }
        
        // إذا لم توجد أسئلة بمستوى الصعوبة المحدد، استخدم جميع أسئلة الفئة
        val questionsToUse = if (difficultyFiltered.isNotEmpty()) difficultyFiltered else categoryFiltered
        
        // اختيار عدد محدود من الأسئلة حسب مستوى الصعوبة
        questions = questionsToUse.shuffled().take(maxQuestions)
    }

    /**
     * إعداد أزرار المساعدة
     */
    private fun setupHelperButtons() {
        // تحميل تأثير النبض
        val pulseAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.pulse)
        
        // زر التلميح
        hintButton.setOnClickListener {
            // تشغيل صوت النقر
            soundManager.playClickSound()
            // تطبيق تأثير النبض على الزر
            it.startAnimation(pulseAnimation)
            
            if (quizHelper.useHint()) {
                // عرض تلميح للسؤال الحالي
                showHint()
                // تحديث نص الزر
                updateHelperButtonsText()
            } else {
                // إظهار رسالة بأنه لا توجد تلميحات متبقية
                feedbackTextView.text = getString(R.string.no_hints_remaining)
                feedbackTextView.setTextColor(Color.RED)
                feedbackCardView.visibility = View.VISIBLE
                feedbackCardView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in))
            }
        }
        
        // زر 50:50
        fiftyFiftyButton.setOnClickListener {
            // تشغيل صوت النقر
            soundManager.playClickSound()
            // تطبيق تأثير النبض على الزر
            it.startAnimation(pulseAnimation)
            
            if (quizHelper.useFiftyFifty()) {
                // تطبيق خيار 50:50
                applyFiftyFifty()
                // تحديث نص الزر
                updateHelperButtonsText()
            } else {
                // إظهار رسالة بأنه لا توجد خيارات 50:50 متبقية
                feedbackTextView.text = getString(R.string.no_fifty_fifty_remaining)
                feedbackTextView.setTextColor(Color.RED)
                feedbackCardView.visibility = View.VISIBLE
                feedbackCardView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in))
            }
        }
        
        // زر التخطي
        skipButton.setOnClickListener {
            // تشغيل صوت النقر
            soundManager.playClickSound()
            // تطبيق تأثير النبض على الزر
            it.startAnimation(pulseAnimation)
            
            if (quizHelper.useSkip()) {
                // تخطي السؤال الحالي
                skipQuestion()
                // تحديث نص الزر
                updateHelperButtonsText()
            } else {
                // إظهار رسالة بأنه لا توجد مرات تخطي متبقية
                feedbackTextView.text = getString(R.string.no_skips_remaining)
                feedbackTextView.setTextColor(Color.RED)
                feedbackCardView.visibility = View.VISIBLE
                feedbackCardView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in))
            }
        }
    }
    
    /**
     * تحديث نصوص أزرار المساعدة
     */
    private fun updateHelperButtonsText() {
        hintButton.text = getString(R.string.hint_button_with_count, quizHelper.getHintsRemaining())
        fiftyFiftyButton.text = getString(R.string.fifty_fifty_button_with_count, quizHelper.getFiftyFiftyRemaining())
        skipButton.text = getString(R.string.skip_button_with_count, quizHelper.getSkipRemaining())
        
        // تعطيل الأزرار إذا كان العدد المتبقي صفر
        hintButton.isEnabled = quizHelper.getHintsRemaining() > 0
        fiftyFiftyButton.isEnabled = quizHelper.getFiftyFiftyRemaining() > 0
        skipButton.isEnabled = quizHelper.getSkipRemaining() > 0
        
        // تغيير لون الأزرار بناءً على حالة التمكين
        hintButton.setBackgroundColor(if (hintButton.isEnabled) HINT_COLOR else ContextCompat.getColor(this, R.color.colorDisabled))
        fiftyFiftyButton.setBackgroundColor(if (fiftyFiftyButton.isEnabled) FIFTY_FIFTY_COLOR else ContextCompat.getColor(this, R.color.colorDisabled))
        skipButton.setBackgroundColor(if (skipButton.isEnabled) SKIP_COLOR else ContextCompat.getColor(this, R.color.colorDisabled))
    }
    
    /**
     * عرض تلميح للسؤال الحالي
     */
    private fun showHint() {
        val currentQuestion = questions[currentQuestionIndex]
        val correctAnswerIndex = currentQuestion.correctAnswer
        
        // عرض تلميح بناءً على السؤال الحالي
        val hintMessage = getString(R.string.hint_message, getOptionLetter(correctAnswerIndex))
        
        // عرض التلميح في Snackbar بدلاً من مربع حوار
        Snackbar.make(findViewById(android.R.id.content), hintMessage, Snackbar.LENGTH_LONG)
            .setBackgroundTint(HINT_COLOR)
            .setTextColor(Color.WHITE)
            .show()
    }
    
    /**
     * الحصول على حرف الخيار (أ، ب، ج، د) بناءً على المؤشر
     */
    private fun getOptionLetter(index: Int): String {
        return when (index) {
            0 -> "أ"
            1 -> "ب"
            2 -> "ج"
            3 -> "د"
            else -> ""
        }
    }
    
    /**
     * تطبيق خيار 50:50 (إخفاء خيارين خاطئين)
     */
    private fun applyFiftyFifty() {
        val currentQuestion = questions[currentQuestionIndex]
        val correctAnswerIndex = currentQuestion.correctAnswer
        
        // قائمة بمؤشرات الخيارات الخاطئة
        val wrongOptions = mutableListOf<Int>()
        for (i in 0 until 4) {
            if (i != correctAnswerIndex) {
                wrongOptions.add(i)
            }
        }
        
        // خلط الخيارات الخاطئة واختيار اثنين منها لإخفائهما
        wrongOptions.shuffle()
        val optionsToHide = wrongOptions.take(2)
        
        // إخفاء الخيارات المحددة
        for (optionIndex in optionsToHide) {
            when (optionIndex) {
                0 -> option1RadioButton.visibility = View.INVISIBLE
                1 -> option2RadioButton.visibility = View.INVISIBLE
                2 -> option3RadioButton.visibility = View.INVISIBLE
                3 -> option4RadioButton.visibility = View.INVISIBLE
            }
        }
        
        // عرض رسالة Snackbar
        Snackbar.make(findViewById(android.R.id.content), "تم استخدام 50:50", Snackbar.LENGTH_LONG)
            .setBackgroundTint(FIFTY_FIFTY_COLOR)
            .setTextColor(Color.WHITE)
            .show()
    }
    
    /**
     * تخطي السؤال الحالي
     */
    private fun skipQuestion() {
        // تطبيق الرسوم المتحركة على بطاقة السؤال الحالي
        val slideOutLeft = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_out_left)
        val fadeOut = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_out)
        
        slideOutLeft.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                // إعادة تعيين خلفية الخيارات
                resetOptionBackgrounds()
                
                // الانتقال إلى السؤال التالي
                currentQuestionIndex++
                if (currentQuestionIndex < questions.size) {
                    // عرض رسالة Snackbar
                    Snackbar.make(findViewById(android.R.id.content), "تم تخطي السؤال", Snackbar.LENGTH_LONG)
                        .setBackgroundTint(SKIP_COLOR)
                        .setTextColor(Color.WHITE)
                        .show()
                    displayQuestion(currentQuestionIndex)
                } else {
                    // إذا كان هذا آخر سؤال، أظهر شاشة النتيجة النهائية
                    showQuizCompletedDialog()
                }
            }
            
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })
        
        questionCardView.startAnimation(slideOutLeft)
        answersCardView.startAnimation(fadeOut)
    }
    
    /**
     * عرض السؤال بالمؤشر المحدد
     */
    private fun displayQuestion(index: Int) {
        // إعادة تعيين واجهة المستخدم
        answersRadioGroup.clearCheck();
        feedbackCardView.visibility = View.INVISIBLE

        // تطبيق الرسوم المتحركة على بطاقة السؤال
        val fadeIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideInRight = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        val rotate = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.rotate)
        val questionChange = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.question_change)
        
        questionCardView.startAnimation(rotate)
        answersCardView.startAnimation(fadeIn)

        // عرض السؤال الحالي مع تأثير تغيير السؤال
        val currentQuestion = questions[index]
        questionTextView.startAnimation(questionChange)
        questionTextView.text = currentQuestion.question
        
        // عرض الخيارات مع تأثير
        val buttonEnabledAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_enabled)
        option1RadioButton.text = currentQuestion.options[0]
        option1RadioButton.startAnimation(buttonEnabledAnimation)
        option2RadioButton.text = currentQuestion.options[1]
        option2RadioButton.startAnimation(buttonEnabledAnimation)
        option3RadioButton.text = currentQuestion.options[2]
        option3RadioButton.startAnimation(buttonEnabledAnimation)
        option4RadioButton.text = currentQuestion.options[3]
        option4RadioButton.startAnimation(buttonEnabledAnimation)
        
        // إعادة تعيين حالة رؤية الخيارات (في حالة استخدام 50:50 سابقًا)
        option1RadioButton.visibility = View.VISIBLE
        option2RadioButton.visibility = View.VISIBLE
        option3RadioButton.visibility = View.VISIBLE
        option4RadioButton.visibility = View.VISIBLE
        
        // إعادة تعيين خلفية الخيارات
        resetOptionBackgrounds()
        
        // إعادة تعيين حالة الإجابة
        answered = false
        
        // بدء المؤقت للسؤال
        startQuestionTimer()
    }
    
    private fun startQuestionTimer() {
        // إلغاء أي مؤقت سابق
        countDownTimer?.cancel()
        
        // تعيين الوقت المتبقي
        timeRemaining = questionTimeInSeconds
        
        // تحديث نص الوقت المتبقي
        timeRemainingTextView.text = getString(R.string.time_remaining, timeRemaining)
        timeRemainingTextView.setTextColor(Color.BLACK)
        
        // تحميل تأثير النبض للمؤقت
        val timerPulseAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.pulse)
        
        // إنشاء مؤقت جديد
        countDownTimer = object : CountDownTimer(timeRemaining * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = (millisUntilFinished / 1000).toInt()
                timeRemainingTextView.text = getString(R.string.time_remaining, timeRemaining)
                
                // تغيير لون النص عندما يكون الوقت المتبقي أقل من 10 ثوانٍ
                if (timeRemaining <= 10) {
                    timeRemainingTextView.setTextColor(Color.RED)
                    
                    // تطبيق تأثير النبض عندما يكون الوقت أقل من 5 ثوانٍ
                    if (timeRemaining <= 5) {
                        timeRemainingTextView.startAnimation(timerPulseAnimation)
                    }
                }
            }
            
            override fun onFinish() {
                timeRemaining = 0
                timeRemainingTextView.text = getString(R.string.time_remaining, timeRemaining)
                timeRemainingTextView.setTextColor(Color.RED)
                
                // إذا لم يتم الإجابة على السؤال، اعتبره إجابة خاطئة
                if (!answered) {
                    handleTimeOut()
                }
            }
        }.start()
    }
    
    private fun handleTimeOut() {
        // تحديث حالة الإجابة
        answered = true
        
        // زيادة عدد الإجابات الخاطئة
        wrongAnswersCount++
        
        // عرض رسالة انتهاء الوقت
        feedbackTextView.text = getString(R.string.time_out)
        feedbackTextView.setTextColor(Color.RED)
        feedbackCardView.visibility = View.VISIBLE
        feedbackCardView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in))
        
        // تشغيل صوت انتهاء الوقت
         soundManager.playWrongSound()
        
        // تطبيق تأثير تحديث النتيجة
        scoreTextView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.score_update))
        
        // تلوين الخيار الصحيح باللون الأخضر
        val currentQuestion = questions[currentQuestionIndex]
        when (currentQuestion.correctAnswer) {
            0 -> option1RadioButton.setBackgroundColor(Color.GREEN)
            1 -> option2RadioButton.setBackgroundColor(Color.GREEN)
            2 -> option3RadioButton.setBackgroundColor(Color.GREEN)
            3 -> option4RadioButton.setBackgroundColor(Color.GREEN)
        }
        
        // تطبيق رسوم متحركة للاهتزاز على جميع الخيارات
        val shake = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
        option1RadioButton.startAnimation(shake)
        option2RadioButton.startAnimation(shake)
        option3RadioButton.startAnimation(shake)
        option4RadioButton.startAnimation(shake)
    }

    /**
     * التحقق من الإجابة المحددة
     */
    private fun checkAnswer(selectedOptionIndex: Int) {
        val currentQuestion = questions[currentQuestionIndex]
        val isCorrect = selectedOptionIndex == currentQuestion.correctAnswer

        // تحميل الرسوم المتحركة
        val scaleUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.scale_up)
        val shake = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.shake)
        
        if (isCorrect) {
            // إذا كانت الإجابة صحيحة
            feedbackTextView.text = getString(R.string.correct_answer)
            feedbackTextView.setTextColor(Color.GREEN)
            score++
            correctAnswersCount++
            
            // تشغيل صوت الإجابة الصحيحة
             soundManager.playCorrectSound()
            
            // تطبيق رسوم متحركة للتكبير على الإجابة الصحيحة
            when (selectedOptionIndex) {
                0 -> option1RadioButton.startAnimation(scaleUp)
                1 -> option2RadioButton.startAnimation(scaleUp)
                2 -> option3RadioButton.startAnimation(scaleUp)
                3 -> option4RadioButton.startAnimation(scaleUp)
            }
            
            // تلوين الخيار الصحيح
            when (selectedOptionIndex) {
                0 -> option1RadioButton.setBackgroundColor(Color.GREEN)
                1 -> option2RadioButton.setBackgroundColor(Color.GREEN)
                2 -> option3RadioButton.setBackgroundColor(Color.GREEN)
                3 -> option4RadioButton.setBackgroundColor(Color.GREEN)
            }
        } else {
            // إذا كانت الإجابة خاطئة
            feedbackTextView.text = getString(R.string.wrong_answer)
            feedbackTextView.setTextColor(Color.RED)
            wrongAnswersCount++
            
            // تشغيل صوت الإجابة الخاطئة
             soundManager.playWrongSound()
            
            // تطبيق رسوم متحركة للاهتزاز على الإجابة الخاطئة
            when (selectedOptionIndex) {
                0 -> option1RadioButton.startAnimation(shake)
                1 -> option2RadioButton.startAnimation(shake)
                2 -> option3RadioButton.startAnimation(shake)
                3 -> option4RadioButton.startAnimation(shake)
            }
            
            // تلوين الخيار الخاطئ باللون الأحمر
            when (selectedOptionIndex) {
                0 -> option1RadioButton.setBackgroundColor(Color.RED)
                1 -> option2RadioButton.setBackgroundColor(Color.RED)
                2 -> option3RadioButton.setBackgroundColor(Color.RED)
                3 -> option4RadioButton.setBackgroundColor(Color.RED)
            }
            
            // تلوين الخيار الصحيح باللون الأخضر
            when (currentQuestion.correctAnswer) {
                0 -> option1RadioButton.setBackgroundColor(Color.GREEN)
                1 -> option2RadioButton.setBackgroundColor(Color.GREEN)
                2 -> option3RadioButton.setBackgroundColor(Color.GREEN)
                3 -> option4RadioButton.setBackgroundColor(Color.GREEN)
            }
        }

        // تحديث النتيجة وإظهار التعليقات مع رسوم متحركة
        scoreTextView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.score_update))
        scoreTextView.text = getString(R.string.score, score)
        feedbackCardView.visibility = View.VISIBLE
        feedbackCardView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in))
        answered = true
        
        // عرض شرح الإجابة إذا كان متوفرًا
        showExplanationDialog(currentQuestion)
    }
    
    private fun showExplanationDialog(question: Question) {
        // التحقق مما إذا كان هناك شرح للإجابة
        if (question.explanation.isNullOrEmpty()) {
            return
        }
        
        // إنشاء مربع حوار لعرض الشرح
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.explanation))
        builder.setMessage(question.explanation)
        builder.setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
    
    private fun showQuizCompletedDialog() {
        // إلغاء المؤقت
        countDownTimer?.cancel()
        
        // تشغيل صوت إكمال اللعبة
        soundManager.playGameCompleteSound()
        
        // حساب النسبة المئوية للإجابات الصحيحة
        val totalQuestions = questions.size
        val percentageCorrect = (correctAnswersCount.toFloat() / totalQuestions) * 100
        
        // تحميل تأثير تحديث الإحصائيات
        val statsUpdateAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.stats_update)
        
        // حساب الوقت المستغرق
        val timeSpent = (System.currentTimeMillis() - quizStartTime) / 1000 // بالثواني
        
        // الحصول على اسم الفئة
        val categoryName = quizDatabase.getCategoryById(categoryId)?.name ?: "عام"
        
        // إنشاء كائن النتيجة وحفظه
        val quizResult = QuizResult(
            categoryId = categoryId,
            categoryName = categoryName,
            difficulty = difficultyLevel.name.lowercase(),
            score = score,
            correctAnswers = correctAnswersCount,
            wrongAnswers = wrongAnswersCount,
            totalQuestions = totalQuestions,
            timeSpent = timeSpent
        )
        resultsManager.saveQuizResult(quizResult)
        
        // تحديث إحصائيات المستخدم مع تطبيق التأثير
        userStats.updateHighScore(score)
        userStats.incrementCompletedQuizzes()
        // إضافة الإجابات الصحيحة والخاطئة
        for (i in 1..correctAnswersCount) {
            userStats.incrementCorrectAnswers()
        }
        for (i in 1..wrongAnswersCount) {
            userStats.incrementWrongAnswers()
        }
        
        // إضافة مكافأة إذا كانت النسبة المئوية عالية
        var rewardMessage = ""
        if (percentageCorrect >= 80) {
            // إضافة مكافأة: تلميح إضافي
            quizHelper.addHelper("hint")
            rewardMessage = "\n\nمكافأة: لقد حصلت على تلميح إضافي!"
        }
        
        // إنشاء رسالة النتيجة مع تأثير تحديث الإحصائيات
        val resultMessage = getString(
            R.string.quiz_completed_message,
            score,
            totalQuestions,
            percentageCorrect
        ) + rewardMessage
        
        // إنشاء مربع حوار لعرض النتيجة
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.quiz_completed))
        builder.setMessage(resultMessage)
        builder.setCancelable(false)
        
        // زر مشاركة النتيجة
        builder.setPositiveButton(getString(R.string.share_results)) { _, _ ->
            shareResults(resultMessage)
        }
        
        // زر العودة إلى الفئات
        builder.setNegativeButton(getString(R.string.back_to_categories)) { _, _ ->
            finish()
        }
        
        // زر إعادة المحاولة
        builder.setNeutralButton(getString(R.string.try_again)) { _, _ ->
            resetQuiz()
        }
        
        // عرض مربع الحوار
        val dialog = builder.create()
        dialog.show()
        
        // إضافة زر لعبة الذاكرة
        dialog.setOnDismissListener {
            showMemoryGameOption()
        }
        
        // تطبيق تأثير الارتداد على مربع الحوار
        val bounceAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bounce)
        dialog.findViewById<View>(android.R.id.message)?.startAnimation(bounceAnimation)
        
        // تطبيق تأثير النبض على أزرار مربع الحوار
        val pulseAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.pulse)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.startAnimation(pulseAnimation)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.postDelayed({
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.startAnimation(pulseAnimation)
        }, 200)
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.postDelayed({
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.startAnimation(pulseAnimation)
        }, 400)
        
        // تعيين أيقونات للأزرار بعد إنشاء مربع الحوار
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(this, R.drawable.ic_share), null, null, null)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(this, R.drawable.ic_restart), null, null, null)
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setCompoundDrawablesWithIntrinsicBounds(
            ContextCompat.getDrawable(this, R.drawable.ic_home), null, null, null)
    }
    
    private fun showMemoryGameOption() {
        AlertDialog.Builder(this)
            .setTitle("🎮 لعبة مسلية!")
            .setMessage("هل تريد لعب لعبة الذاكرة التفاعلية؟\nاختبر ذاكرتك مع لعبة البطاقات المطابقة!")
            .setPositiveButton("نعم، العب الآن!") { _, _ ->
                val intent = Intent(this, MemoryGameActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("لا، شكراً") { _, _ ->
                // لا حاجة لفعل شيء
            }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }
    
    private fun shareResults(resultMessage: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        
        // إضافة اسم الفئة إلى الرسالة
        val categoryName = quizDatabase.getCategoryById(categoryId)?.name ?: getString(R.string.app_name)
        
        // إنشاء رسالة مشاركة مخصصة
        val shareMessage = "$resultMessage\n\nجرب تطبيق ${getString(R.string.app_name)} الآن!"
        
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "نتيجة اختبار $categoryName")
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }
    
    private fun resetQuiz() {
        // إعادة تعيين المتغيرات
        currentQuestionIndex = 0
        score = 0
        correctAnswersCount = 0
        wrongAnswersCount = 0
        answered = false
        
        // تحديث نص النتيجة مع تأثير
        scoreTextView.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.score_update))
        scoreTextView.text = getString(R.string.score, score)
        
        // تطبيق تأثير تمكين الأزرار
        val buttonEnabledAnimation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.button_enabled)
        option1RadioButton.startAnimation(buttonEnabledAnimation)
        option2RadioButton.startAnimation(buttonEnabledAnimation)
        option3RadioButton.startAnimation(buttonEnabledAnimation)
        option4RadioButton.startAnimation(buttonEnabledAnimation)
        nextButton.startAnimation(buttonEnabledAnimation)
        
        // عرض السؤال الأول
        if (questions.isNotEmpty()) {
            displayQuestion(currentQuestionIndex)
        }
    }
    
    /**
     * إعادة تعيين خلفية الخيارات
     */
    private fun resetOptionBackgrounds() {
        option1RadioButton.setBackgroundResource(R.drawable.option_selector)
        option2RadioButton.setBackgroundResource(R.drawable.option_selector)
        option3RadioButton.setBackgroundResource(R.drawable.option_selector)
        option4RadioButton.setBackgroundResource(R.drawable.option_selector)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // إلغاء المؤقت عند تدمير النشاط
        countDownTimer?.cancel()
    }
}