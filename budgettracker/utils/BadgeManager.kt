package com.budgettracker.utils

import android.content.Context

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val earned: Boolean
)

object BadgeManager {

    fun evaluateBadges(
        totalSpent: Double,
        minGoal: Double,
        maxGoal: Double,
        expenseCount: Int,
        context: Context
    ): List<Badge> {
        val badges = mutableListOf<Badge>()

        badges.add(Badge(
            id = "first_log", title = "First Step",
            description = "Log your first expense", emoji = "🥇",
            earned = expenseCount >= 1
        ))

        badges.add(Badge(
            id = "consistent_logger", title = "Consistent Logger",
            description = "Log at least 5 expenses in a month", emoji = "📝",
            earned = expenseCount >= 5
        ))

        badges.add(Badge(
            id = "power_logger", title = "Power Logger",
            description = "Log 15 or more expenses in a month", emoji = "⚡",
            earned = expenseCount >= 15
        ))

        badges.add(Badge(
            id = "under_budget", title = "Under Budget",
            description = "Stay within your maximum spending goal", emoji = "✅",
            earned = maxGoal > 0 && totalSpent <= maxGoal
        ))

        badges.add(Badge(
            id = "goal_achiever", title = "Goal Achiever",
            description = "Spend between your minimum and maximum goals", emoji = "🎯",
            earned = maxGoal > 0 && minGoal > 0 && totalSpent >= minGoal && totalSpent <= maxGoal
        ))

        badges.add(Badge(
            id = "saver", title = "Super Saver",
            description = "Spend less than 50% of your max goal", emoji = "💰",
            earned = maxGoal > 0 && totalSpent < (maxGoal * 0.5)
        ))

        badges.add(Badge(
            id = "goals_set", title = "Goal Setter",
            description = "Set both a minimum and maximum spending goal", emoji = "🗺️",
            earned = minGoal > 0 && maxGoal > 0
        ))

        badges.add(Badge(
            id = "diversified", title = "Diversified Spender",
            description = "Log expenses across multiple categories", emoji = "🌈",
            earned = expenseCount >= 3
        ))

        return badges
    }
}