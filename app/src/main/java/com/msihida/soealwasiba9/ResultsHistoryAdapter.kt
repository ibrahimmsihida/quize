package com.msihida.soealwasiba9

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ResultsHistoryAdapter(private var results: List<QuizResult>) : 
    RecyclerView.Adapter<ResultsHistoryAdapter.ResultViewHolder>() {
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.textCategoryName)
        val difficulty: TextView = itemView.findViewById(R.id.textDifficulty)
        val score: TextView = itemView.findViewById(R.id.textScore)
        val correctAnswers: TextView = itemView.findViewById(R.id.textCorrectAnswers)
        val timeSpent: TextView = itemView.findViewById(R.id.textTimeSpent)
        val date: TextView = itemView.findViewById(R.id.textDate)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_result, parent, false)
        return ResultViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val result = results[position]
        
        holder.categoryName.text = result.categoryName
        
        // تحديد لون وترجمة مستوى الصعوبة
        when (result.difficulty) {
            "easy" -> {
                holder.difficulty.text = "سهل"
                holder.difficulty.setBackgroundResource(R.drawable.difficulty_easy_bg)
            }
            "medium" -> {
                holder.difficulty.text = "متوسط"
                holder.difficulty.setBackgroundResource(R.drawable.difficulty_medium_bg)
            }
            "hard" -> {
                holder.difficulty.text = "صعب"
                holder.difficulty.setBackgroundResource(R.drawable.difficulty_hard_bg)
            }
        }
        
        holder.score.text = "${result.score}%"
        holder.correctAnswers.text = "${result.correctAnswers}/${result.totalQuestions}"
        
        // تنسيق الوقت المستغرق
        val minutes = result.timeSpent / 60
        val seconds = result.timeSpent % 60
        holder.timeSpent.text = if (minutes > 0) {
            "${minutes}د ${seconds}ث"
        } else {
            "${seconds}ث"
        }
        
        holder.date.text = dateFormat.format(Date(result.timestamp))
        
        // تحديد لون النتيجة حسب الأداء
        when {
            result.score >= 80 -> holder.score.setTextColor(
                holder.itemView.context.getColor(android.R.color.holo_green_dark)
            )
            result.score >= 60 -> holder.score.setTextColor(
                holder.itemView.context.getColor(android.R.color.holo_orange_dark)
            )
            else -> holder.score.setTextColor(
                holder.itemView.context.getColor(android.R.color.holo_red_dark)
            )
        }
    }
    
    override fun getItemCount(): Int = results.size
    
    fun updateResults(newResults: List<QuizResult>) {
        results = newResults.sortedByDescending { it.timestamp }
        notifyDataSetChanged()
    }
}