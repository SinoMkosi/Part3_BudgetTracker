package com.budgettracker.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.budgettracker.R
import com.budgettracker.data.entities.ExpenseWithCategory
import com.budgettracker.utils.DateUtils

class ExpenseAdapter(
    private val expenses: List<ExpenseWithCategory>,
    private val onItemClick: (ExpenseWithCategory) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDescription: TextView = itemView.findViewById(R.id.tvExpenseDescription)
        val tvAmount: TextView = itemView.findViewById(R.id.tvExpenseAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvExpenseDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvExpenseTime)
        val tvCategory: TextView = itemView.findViewById(R.id.tvExpenseCategory)
        val ivPhoto: ImageView = itemView.findViewById(R.id.ivHasPhoto)
        val categoryDot: View = itemView.findViewById(R.id.viewCategoryDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = expenses[position]
        val expense = item.expense
        val category = item.category

        holder.tvDescription.text = expense.description
        holder.tvAmount.text = DateUtils.formatCurrency(expense.amount)
        holder.tvDate.text = DateUtils.dbStringToDisplay(expense.date)
        holder.tvTime.text = "${expense.startTime} – ${expense.endTime}"
        holder.tvCategory.text = category?.name ?: "Uncategorised"

        // Category color dot
        if (category != null) {
            try {
                holder.categoryDot.setBackgroundColor(Color.parseColor(category.colorHex))
            } catch (e: Exception) {
                holder.categoryDot.setBackgroundColor(Color.GRAY)
            }
        } else {
            holder.categoryDot.setBackgroundColor(Color.LTGRAY)
        }

        // Show camera icon if photo attached
        holder.ivPhoto.visibility = if (expense.photoPath != null) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = expenses.size
}
