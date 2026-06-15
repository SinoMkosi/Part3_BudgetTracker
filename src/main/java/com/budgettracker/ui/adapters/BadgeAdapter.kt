package com.budgettracker.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.budgettracker.R
import com.budgettracker.utils.Badge

class BadgeAdapter(private val badges: List<Badge>) :
    RecyclerView.Adapter<BadgeAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tvBadgeEmoji)
        val tvTitle: TextView = itemView.findViewById(R.id.tvBadgeTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvBadgeDesc)
        val tvStatus: TextView = itemView.findViewById(R.id.tvBadgeStatus)
        val card: CardView = itemView.findViewById(R.id.cardBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val badge = badges[position]
        holder.tvEmoji.text = badge.emoji
        holder.tvTitle.text = badge.title
        holder.tvDesc.text = badge.description
        if (badge.earned) {
            holder.tvStatus.text = "EARNED"
            holder.tvStatus.setTextColor(Color.parseColor("#43A047"))
            holder.card.alpha = 1.0f
        } else {
            holder.tvStatus.text = "LOCKED"
            holder.tvStatus.setTextColor(Color.parseColor("#9E9E9E"))
            holder.card.alpha = 0.5f
        }
    }

    override fun getItemCount() = badges.size
}