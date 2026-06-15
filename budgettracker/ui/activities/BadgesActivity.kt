package com.budgettracker.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityBadgesBinding
import com.budgettracker.ui.adapters.BadgeAdapter
import com.budgettracker.utils.BadgeManager
import com.budgettracker.utils.DateUtils
import com.budgettracker.utils.SessionManager

class BadgesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBadgesBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBadgesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Badges & Rewards"

        binding.rvBadges.layoutManager = LinearLayoutManager(this)
        loadBadges()
    }

    private fun loadBadges() {
        val userId = sessionManager.getUserId()
        val startDate = DateUtils.getFirstDayOfMonthDbString()
        val endDate = DateUtils.getTodayDbString()

        db.userDao().getUserById(userId).observe(this) { user ->
            user ?: return@observe
            db.expenseDao().getTotalSpentForPeriod(userId, startDate, endDate)
                .observe(this) { total ->
                    db.expenseDao().getExpensesWithCategoryForPeriod(userId, startDate, endDate)
                        .observe(this) { expenses ->
                            val badges = BadgeManager.evaluateBadges(
                                totalSpent = total ?: 0.0,
                                minGoal = user.minMonthlyGoal,
                                maxGoal = user.maxMonthlyGoal,
                                expenseCount = expenses.size,
                                context = this
                            )
                            val earned = badges.count { it.earned }
                            binding.tvBadgeSummary.text = "You've earned $earned / ${badges.size} badges this month!"
                            binding.rvBadges.adapter = BadgeAdapter(badges)
                        }
                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}