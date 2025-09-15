package com.msihida.soealwasiba9

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MemoryGameActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var scoreText: TextView
    private lateinit var timeText: TextView
    private lateinit var movesText: TextView
    private lateinit var restartButton: Button
    private lateinit var backButton: Button
    
    // Animation Views
    private lateinit var cardFlipAnimation: ImageView
    private lateinit var pulseAnimation: ImageView
    private lateinit var shakeAnimation: ImageView

    private var cards = mutableListOf<MemoryCard>()
    private var flippedCards = mutableListOf<Int>()
    private var matchedPairs = 0
    private var moves = 0
    private var score = 0
    private var timeElapsed = 0
    private var gameStarted = false
    private var gameFinished = false

    private lateinit var soundManager: SoundManager
    private val handler = Handler(Looper.getMainLooper())
    private val timeRunnable = object : Runnable {
        override fun run() {
            if (gameStarted && !gameFinished) {
                timeElapsed++
                updateTimeDisplay()
                handler.postDelayed(this, 1000)
            }
        }
    }

    // رموز الأيقونات للبطاقات
    private val cardIcons = listOf(
        "🎯", "🎮", "🎲", "🎪", "🎨", "🎭", "🎸", "🎺",
        "🎯", "🎮", "🎲", "🎪", "🎨", "🎭", "🎸", "🎺"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_game)

        initViews()
        initializeSoundManager()
        setupGame()
        setupClickListeners()
    }

    private fun initViews() {
        gridLayout = findViewById(R.id.gridLayout)
        scoreText = findViewById(R.id.scoreText)
        timeText = findViewById(R.id.timeText)
        movesText = findViewById(R.id.movesText)
        restartButton = findViewById(R.id.restartButton)
        backButton = findViewById(R.id.backButton)
        
        // تهيئة Animation Views
        cardFlipAnimation = findViewById(R.id.cardFlipAnimation)
        pulseAnimation = findViewById(R.id.pulseAnimation)
        shakeAnimation = findViewById(R.id.shakeAnimation)
    }

    private fun initializeSoundManager() {
        soundManager = SoundManager.getInstance(this)
    }

    private fun setupGame() {
        cards.clear()
        flippedCards.clear()
        matchedPairs = 0
        moves = 0
        score = 0
        timeElapsed = 0
        gameStarted = false
        gameFinished = false

        // إنشاء البطاقات
        val shuffledIcons = cardIcons.shuffled()
        for (i in 0 until 16) {
            cards.add(MemoryCard(i, shuffledIcons[i], false, false))
        }

        createCardViews()
        updateUI()
    }

    private fun createCardViews() {
        gridLayout.removeAllViews()
        
        for (i in cards.indices) {
            val cardView = layoutInflater.inflate(R.layout.memory_card_item, null)
            val cardButton = cardView.findViewById<Button>(R.id.cardButton)
            val cardIcon = cardView.findViewById<TextView>(R.id.cardIcon)
            
            cardButton.tag = i
            cardIcon.text = "?"
            cardIcon.visibility = View.VISIBLE
            
            cardButton.setOnClickListener { 
                soundManager.playClickSound()
                onCardClicked(i) 
            }
            
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = GridLayout.LayoutParams.WRAP_CONTENT
            params.columnSpec = GridLayout.spec(i % 4, 1f)
            params.rowSpec = GridLayout.spec(i / 4)
            params.setMargins(8, 8, 8, 8)
            
            cardView.layoutParams = params
            gridLayout.addView(cardView)
        }
    }

    private fun onCardClicked(position: Int) {
        if (!gameStarted) {
            gameStarted = true
            handler.post(timeRunnable)
        }

        val card = cards[position]
        if (card.isFlipped || card.isMatched || flippedCards.size >= 2) {
            return
        }

        // قلب البطاقة
        flipCard(position, true)
        flippedCards.add(position)

        if (flippedCards.size == 2) {
            moves++
            updateUI()
            
            handler.postDelayed({
                checkForMatch()
            }, 1000)
        }
    }

    private fun flipCard(position: Int, show: Boolean) {
        val cardView = gridLayout.getChildAt(position)
        val cardIcon = cardView.findViewById<TextView>(R.id.cardIcon)
        val cardButton = cardView.findViewById<Button>(R.id.cardButton)
        
        cards[position].isFlipped = show
        
        // تشغيل رسوم Lottie المتحركة لقلب البطاقة
        playCardFlipAnimation(cardView)
        
        // رسوم متحركة للقلب
        val flipAnimator = ObjectAnimator.ofFloat(cardView, "rotationY", 0f, 90f)
        flipAnimator.duration = 150
        
        flipAnimator.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (show) {
                    cardIcon.text = cards[position].icon
                    cardButton.setBackgroundColor(ContextCompat.getColor(this@MemoryGameActivity, R.color.card_flipped))
                } else {
                    cardIcon.text = "?"
                    cardButton.setBackgroundColor(ContextCompat.getColor(this@MemoryGameActivity, R.color.card_normal))
                }
                
                val flipBackAnimator = ObjectAnimator.ofFloat(cardView, "rotationY", 90f, 0f)
                flipBackAnimator.duration = 150
                flipBackAnimator.start()
            }
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
        
        flipAnimator.start()
    }

    private fun checkForMatch() {
        if (flippedCards.size != 2) return
        
        val firstCard = cards[flippedCards[0]]
        val secondCard = cards[flippedCards[1]]
        
        if (firstCard.icon == secondCard.icon) {
            // تطابق!
            soundManager.playCorrectSound()
            firstCard.isMatched = true
            secondCard.isMatched = true
            matchedPairs++
            score += 10
            
            // تشغيل رسوم Lottie للتطابق الناجح
            playPulseAnimation()
            
            // تأثير بصري للتطابق
            animateMatchedCards()
            
            if (matchedPairs == 8) {
                gameFinished = true
                soundManager.playGameCompleteSound()
                showGameCompletedDialog()
            }
        } else {
            // عدم تطابق - تشغيل رسوم الاهتزاز
            soundManager.playWrongSound()
            playShakeAnimation()
            
            flipCard(flippedCards[0], false)
            flipCard(flippedCards[1], false)
        }
        
        flippedCards.clear()
        updateUI()
    }

    private fun animateMatchedCards() {
        for (position in flippedCards) {
            val cardView = gridLayout.getChildAt(position)
            val scaleX = ObjectAnimator.ofFloat(cardView, "scaleX", 1f, 1.2f, 1f)
            val scaleY = ObjectAnimator.ofFloat(cardView, "scaleY", 1f, 1.2f, 1f)
            
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(scaleX, scaleY)
            animatorSet.duration = 300
            animatorSet.start()
            
            val cardButton = cardView.findViewById<Button>(R.id.cardButton)
            cardButton.setBackgroundColor(ContextCompat.getColor(this, R.color.card_matched))
        }
    }

    private fun updateUI() {
        scoreText.text = "النقاط: $score"
        movesText.text = "الحركات: $moves"
    }

    private fun updateTimeDisplay() {
        val minutes = timeElapsed / 60
        val seconds = timeElapsed % 60
        timeText.text = String.format("الوقت: %02d:%02d", minutes, seconds)
    }

    private fun setupClickListeners() {
        restartButton.setOnClickListener {
            soundManager.playClickSound()
            setupGame()
        }
        
        backButton.setOnClickListener {
            soundManager.playClickSound()
            finish()
        }
    }

    private fun showGameCompletedDialog() {
        val finalScore = score + (1000 - timeElapsed * 2) + (100 - moves * 5)
        
        AlertDialog.Builder(this)
            .setTitle("تهانينا!")
            .setMessage("لقد أكملت اللعبة!\n\nالنقاط النهائية: $finalScore\nالوقت: ${timeElapsed}s\nالحركات: $moves")
            .setPositiveButton("لعب مرة أخرى") { _, _ ->
                soundManager.playClickSound()
                setupGame()
            }
            .setNegativeButton("العودة") { _, _ ->
                soundManager.playClickSound()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    // دوال تشغيل رسوم Lottie المتحركة
    private fun playCardFlipAnimation(cardView: View) {
        cardFlipAnimation.visibility = View.VISIBLE
        cardFlipAnimation.x = cardView.x + cardView.width / 2 - cardFlipAnimation.width / 2
        cardFlipAnimation.y = cardView.y + cardView.height / 2 - cardFlipAnimation.height / 2
        
        Handler(Looper.getMainLooper()).postDelayed({
            cardFlipAnimation.visibility = View.GONE
        }, 600)
    }
    
    private fun playPulseAnimation() {
        pulseAnimation.visibility = View.VISIBLE
        
        Handler(Looper.getMainLooper()).postDelayed({
            pulseAnimation.visibility = View.GONE
        }, 1000)
    }
    
    private fun playShakeAnimation() {
        shakeAnimation.visibility = View.VISIBLE
        
        Handler(Looper.getMainLooper()).postDelayed({
            shakeAnimation.visibility = View.GONE
        }, 800)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeRunnable)
        soundManager.release()
    }

    data class MemoryCard(
        val id: Int,
        val icon: String,
        var isFlipped: Boolean = false,
        var isMatched: Boolean = false
    )
}