package com.msihida.soealwasiba9

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * محول لعرض الإنجازات في RecyclerView
 */
class AchievementAdapter(private val context: Context, private val achievements: List<Achievement>) :
    RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    private val achievementManager = AchievementManager(context)
    private val userStats = UserStats(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.bind(achievement)
    }

    override fun getItemCount(): Int {
        return achievements.size
    }

    inner class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewIcon: ImageView = itemView.findViewById(R.id.imageViewAchievementIcon)
        private val textViewTitle: TextView = itemView.findViewById(R.id.textViewAchievementTitle)
        private val textViewDescription: TextView = itemView.findViewById(R.id.textViewAchievementDescription)
        private val textViewStatus: TextView = itemView.findViewById(R.id.textViewAchievementStatus)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarAchievement)

        fun bind(achievement: Achievement) {
            // تعيين الأيقونة والعنوان والوصف
            imageViewIcon.setImageResource(achievement.iconResId)
            textViewTitle.text = achievement.title
            textViewDescription.text = achievement.description

            // التحقق من حالة الإنجاز
            val isUnlocked = achievementManager.isAchievementUnlocked(achievement.id)
            
            // الحصول على القيمة الحالية
            val currentValue = when (achievement.type) {
                AchievementType.COMPLETED_QUIZZES -> userStats.getCompletedQuizzes()
                AchievementType.CORRECT_ANSWERS -> userStats.getCorrectAnswers()
                AchievementType.HIGH_SCORE -> userStats.getHighScore()
                else -> 0
            }

            // تعيين حالة الإنجاز
            if (isUnlocked) {
                textViewStatus.text = context.getString(R.string.achievement_unlocked)
                textViewStatus.setTextColor(context.getColor(android.R.color.holo_green_dark))
                progressBar.progress = achievement.requiredValue
                progressBar.max = achievement.requiredValue
            } else {
                textViewStatus.text = context.getString(R.string.achievement_locked)
                textViewStatus.setTextColor(context.getColor(android.R.color.darker_gray))
                progressBar.progress = currentValue
                progressBar.max = achievement.requiredValue
            }
        }
    }
}