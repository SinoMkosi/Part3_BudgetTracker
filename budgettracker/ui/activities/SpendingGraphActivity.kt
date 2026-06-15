package com.budgettracker.ui.activities

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivitySpendingGraphBinding
import com.budgettracker.utils.DateUtils
import com.budgettracker.utils.SessionManager
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

class SpendingGraphActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpendingGraphBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    private var startDate = DateUtils.getFirstDayOfMonthDbString()
    private var endDate = DateUtils.getTodayDbString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpendingGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Spending Graph"

        setupChart()
        updateDateButtons()
        loadChartData()

        binding.btnGraphStartDate.setOnClickListener { showDatePicker(true) }
        binding.btnGraphEndDate.setOnClickListener { showDatePicker(false) }
    }

    private fun setupChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setFitBars(true)
            legend.isEnabled = true
            animateY(800)
        }
    }

    private fun showDatePicker(isStart: Boolean) {
        val date = if (isStart) startDate else endDate
        val parts = date.split("-")
        DatePickerDialog(this, { _, y, m, d ->
            val selected = "%04d-%02d-%02d".format(y, m + 1, d)
            if (isStart) {
                if (selected <= endDate) { startDate = selected; updateDateButtons(); loadChartData() }
                else Toast.makeText(this, "Start must be before end", Toast.LENGTH_SHORT).show()
            } else {
                if (selected >= startDate) { endDate = selected; updateDateButtons(); loadChartData() }
                else Toast.makeText(this, "End must be after start", Toast.LENGTH_SHORT).show()
            }
        }, parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt()).show()
    }

    private fun updateDateButtons() {
        binding.btnGraphStartDate.text = "From: ${DateUtils.dbStringToDisplay(startDate)}"
        binding.btnGraphEndDate.text = "To: ${DateUtils.dbStringToDisplay(endDate)}"
    }

    private fun loadChartData() {
        val userId = sessionManager.getUserId()

        db.expenseDao().getCategoryTotalsForPeriod(userId, startDate, endDate)
            .observe(this) { totals ->
                if (totals.isEmpty()) {
                    binding.tvGraphEmpty.visibility = View.VISIBLE
                    binding.barChart.clear()
                    return@observe
                }
                binding.tvGraphEmpty.visibility = View.GONE

                val entries = totals.mapIndexed { i, item ->
                    BarEntry(i.toFloat(), item.totalAmount.toFloat())
                }
                val labels = totals.map { it.categoryName ?: "Other" }

                val colors = listOf(
                    Color.parseColor("#1E88E5"), Color.parseColor("#43A047"),
                    Color.parseColor("#FB8C00"), Color.parseColor("#8E24AA"),
                    Color.parseColor("#E53935"), Color.parseColor("#00ACC1")
                )

                val dataSet = BarDataSet(entries, "Spent per Category (R)").apply {
                    setColors(colors)
                    valueTextSize = 10f
                }

                binding.barChart.apply {
                    data = BarData(dataSet).apply { barWidth = 0.6f }
                    xAxis.apply {
                        valueFormatter = IndexAxisValueFormatter(labels)
                        granularity = 1f
                        setDrawGridLines(false)
                        labelRotationAngle = -30f
                    }
                    axisLeft.removeAllLimitLines()
                    axisRight.isEnabled = false

                    lifecycleScope.launch {
                        db.userDao().getUserById(userId).observe(this@SpendingGraphActivity) { user ->
                            user ?: return@observe
                            axisLeft.removeAllLimitLines()
                            if (user.minMonthlyGoal > 0) {
                                axisLeft.addLimitLine(LimitLine(user.minMonthlyGoal.toFloat(), "Min Goal").apply {
                                    lineColor = Color.parseColor("#43A047")
                                    lineWidth = 2f; textColor = Color.parseColor("#43A047")
                                    textSize = 11f; labelPosition = LimitLabelPosition.RIGHT_TOP
                                    enableDashedLine(10f, 5f, 0f)
                                })
                            }
                            if (user.maxMonthlyGoal > 0) {
                                axisLeft.addLimitLine(LimitLine(user.maxMonthlyGoal.toFloat(), "Max Goal").apply {
                                    lineColor = Color.parseColor("#E53935")
                                    lineWidth = 2f; textColor = Color.parseColor("#E53935")
                                    textSize = 11f; labelPosition = LimitLabelPosition.RIGHT_TOP
                                    enableDashedLine(10f, 5f, 0f)
                                })
                            }
                            invalidate()
                        }
                    }
                    invalidate()
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}