package com.budgettracker.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.budgettracker.R
import com.budgettracker.data.entities.Category

class CategoryAdapter(
    private val categories: List<Category>,
    private val onDelete: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val viewColor: View = itemView.findViewById(R.id.viewCategoryColor)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.tvName.text = category.name
        try {
            holder.viewColor.setBackgroundColor(Color.parseColor(category.colorHex))
        } catch (e: Exception) {
            holder.viewColor.setBackgroundColor(Color.GRAY)
        }
        holder.btnDelete.setOnClickListener { onDelete(category) }
    }

    override fun getItemCount() = categories.size
}
