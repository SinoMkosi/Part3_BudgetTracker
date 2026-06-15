package com.budgettracker.ui.activities

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityGoalProgressBinding
import com.budgettracker.utils.DateUtils
import com.budgettracker.utils.SessionManager
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class GoalProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalProgressBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Monthly Goal Progress"

        loadGoalProgress()
    }

    private fun loadGoalProgress() {
        val userId = sessionManager.getUserId()
        val startDate = DateUtils.getFirstDayOfMonthDbString()
        val endDate = DateUtils.getTodayDbString()

        db.userDao().getUserById(userId).observe(this) { user ->
            user ?: return@observe
            val minGoal = user.minMonthlyGoal
            val maxGoal = user.maxMonthlyGoal

            db.expenseDao().getTotalSpentForPeriod(userId, startDate, endDate)
                .observe(this) { total ->
                    updateProgressUI(total ?: 0.0, minGoal, maxGoal)
                }

            db.expenseDao().getExpensesWithCategoryForPeriod(userId, startDate, endDate)
                .observe(this) { expenses ->
                    val dailyMap = mutableMapOf<Int, Double>()
                    expenses.forEach { ewc ->
                        val day = ewc.expense.date.split("-")[2].toIntOrNull() ?: 1
                        dailyMap[day] = (dailyMap[day] ?: 0.0) + ewc.expense.amount
                    }
                    var cumulative = 0.0
                    val entries = dailyMap.keys.sorted().map { day ->
                        cumulative += dailyMap[day] ?: 0.0
                        Entry(day.toFloat(), cumulative.toFloat())
                    }.ifEmpty { listOf(Entry(1f, 0f)) }

                    val dataSet = LineDataSet(entries, "Cumulative Spending").apply {
                        color = Color.parseColor("#1E88E5")
                        setCircleColor(Color.parseColor("#1E88E5"))
                        lineWidth = 2.5f; circleRadius = 4f
                        setDrawFilled(true); fillAlpha = 40
                        fillColor = Color.parseColor("#1E88E5")
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    }

                    binding.lineChart.apply {
                        description.isEnabled = false
                        data = LineData(dataSet)
                        xAxis.granularity = 1f
                        axisRight.isEnabled = false
                        axisLeft.removeAllLimitLines()

                        if (minGoal > 0) axisLeft.addLimitLine(
                            LimitLine(minGoal.toFloat(), "Min: ${DateUtils.formatCurrency(minGoal)}").apply {
                                lineColor = Color.parseColor("#43A047"); lineWidth = 2f
                                textColor = Color.parseColor("#43A047"); textSize = 10f
                                labelPosition = LimitLabelPosition.RIGHT_TOP
                                enableDashedLine(10f, 5f, 0f)
                            })
                        if (maxGoal > 0) axisLeft.addLimitLine(
                            LimitLine(maxGoal.toFloat(), "Max: ${DateUtils.formatCurrency(maxGoal)}").apply {
                                lineColor = Color.parseColor("#E53935"); lineWidth = 2f
                                textColor = Color.parseColor("#E53935"); textSize = 10f
                                labelPosition = LimitLabelPosition.RIGHT_TOP
                                enableDashedLine(10f, 5f, 0f)
                            })
                        animateX(800); invalidate()
                    }
                }
        }
    }

    private fun updateProgressUI(spent: Double, minGoal: Double, maxGoal: Double) {
        binding.tvSpentAmount.text = DateUtils.formatCurrency(spent)

        if (maxGoal <= 0) {
            binding.tvProgressStatus.text = "Set goals to track your progress"
            binding.progressBarGoal.progress = 0
            binding.tvProgressPercent.text = "–"
            binding.tvMinGoalLabel.text = "Min: Not set"
            binding.tvMaxGoalLabel.text = "Max: Not set"
            return
        }

        val percent = ((spent / maxGoal) * 100).toInt().coerceIn(0, 100)
        binding.progressBarGoal.progress = percent
        binding.tvProgressPercent.text = "$percent% of max goal used"
        binding.tvMinGoalLabel.text = "Min: ${DateUtils.formatCurrency(minGoal)}"
        binding.tvMaxGoalLabel.text = "Max: ${DateUtils.formatCurrency(maxGoal)}"

        when {
            spent > maxGoal -> {
                binding.tvProgressStatus.text = "⚠️ Over budget! You exceeded your max goal."
                binding.progressBarGoal.progressTintList = ColorStateList.valueOf(Color.parseColor("#E53935"))
            }
            minGoal > 0 && spent < minGoal -> {
                binding.tvProgressStatus.text = "📉 Below minimum spending goal this month."
                binding.progressBarGoal.progressTintList = ColorStateList.valueOf(Color.parseColor("#FB8C00"))
            }
            else -> {
                binding.tvProgressStatus.text = "✅ On track! Spending is within your goals."
                binding.progressBarGoal.progressTintList = ColorStateList.valueOf(Color.parseColor("#43A047"))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}