package com.msihida.soealwasiba9

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * محول لعرض الفئات في RecyclerView
 */
class CategoryAdapter(
    private val categories: List<QuestionCategory>,
    private val listener: OnCategoryClickListener
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    /**
     * واجهة للاستماع لأحداث النقر على الفئة
     */
    interface OnCategoryClickListener {
        fun onCategoryClick(category: QuestionCategory)
    }

    /**
     * حامل العرض للفئة
     */
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView = itemView.findViewById(R.id.categoryIconImageView)
        val nameTextView: TextView = itemView.findViewById(R.id.categoryNameTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.categoryDescriptionTextView)
        val difficultyTextView: TextView = itemView.findViewById(R.id.categoryDifficultyTextView)

        init {
            // إضافة مستمع النقر للعنصر
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onCategoryClick(categories[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        // إنشاء عرض العنصر
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        // الحصول على الفئة الحالية
        val category = categories[position]

        // تعيين بيانات الفئة في العناصر
        holder.iconImageView.setImageResource(category.iconResId)
        holder.nameTextView.text = category.name
        holder.descriptionTextView.text = category.description

        // تعيين نص مستوى الصعوبة
        val difficultyText = when (category.difficulty) {
            Difficulty.EASY -> holder.itemView.context.getString(R.string.difficulty_easy)
            Difficulty.MEDIUM -> holder.itemView.context.getString(R.string.difficulty_medium)
            Difficulty.HARD -> holder.itemView.context.getString(R.string.difficulty_hard)
        }
        holder.difficultyTextView.text = difficultyText
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}