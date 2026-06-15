package com.budgettracker.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.budgettracker.R
import com.budgettracker.data.entities.CategoryTotal
import com.budgettracker.utils.DateUtils
import kotlin.math.roundToInt

class CategoryReportAdapter(
    private val totals: List<CategoryTotal>,
    private val grandTotal: Double
) : RecyclerView.Adapter<CategoryReportAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvReportCategoryName)
        val tvAmount: TextView = itemView.findViewById(R.id.tvReportAmount)
        val tvPercentage: TextView = itemView.findViewById(R.id.tvReportPercentage)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressCategoryBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_report, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = totals[position]
        holder.tvCategoryName.text = item.categoryName ?: "Uncategorised"
        holder.tvAmount.text = DateUtils.formatCurrency(item.totalAmount)

        val pct = if (grandTotal > 0) (item.totalAmount / grandTotal * 100).roundToInt() else 0
        holder.tvPercentage.text = "$pct%"
        holder.progressBar.progress = pct
    }

    override fun getItemCount() = totals.size
}
