package com.budgettracker.ui.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityCategoryReportBinding
import com.budgettracker.ui.adapters.CategoryReportAdapter
import com.budgettracker.utils.DateUtils
import com.budgettracker.utils.SessionManager

class CategoryReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryReportBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    private var startDate = DateUtils.getFirstDayOfMonthDbString()
    private var endDate = DateUtils.getTodayDbString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Category Report"

        binding.rvCategoryReport.layoutManager = LinearLayoutManager(this)

        updateDateButtons()
        loadReport()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnStartDate.setOnClickListener { showDatePicker(true) }
        binding.btnEndDate.setOnClickListener { showDatePicker(false) }
    }

    private fun showDatePicker(isStart: Boolean) {
        val date = if (isStart) startDate else endDate
        val parts = date.split("-")
        val year = parts[0].toInt(); val month = parts[1].toInt() - 1; val day = parts[2].toInt()

        DatePickerDialog(this, { _, y, m, d ->
            val selected = "%04d-%02d-%02d".format(y, m + 1, d)
            if (isStart) {
                if (selected <= endDate) { startDate = selected; updateDateButtons(); loadReport() }
                else android.widget.Toast.makeText(this, "Start must be before end", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                if (selected >= startDate) { endDate = selected; updateDateButtons(); loadReport() }
                else android.widget.Toast.makeText(this, "End must be after start", android.widget.Toast.LENGTH_SHORT).show()
            }
        }, year, month, day).show()
    }

    private fun updateDateButtons() {
        binding.btnStartDate.text = "From: ${DateUtils.dbStringToDisplay(startDate)}"
        binding.btnEndDate.text = "To: ${DateUtils.dbStringToDisplay(endDate)}"
    }

    private fun loadReport() {
        db.expenseDao()
            .getCategoryTotalsForPeriod(sessionManager.getUserId(), startDate, endDate)
            .observe(this) { totals ->
                val grandTotal = totals.sumOf { it.totalAmount }
                binding.tvGrandTotal.text = "Grand Total: ${DateUtils.formatCurrency(grandTotal)}"

                val adapter = CategoryReportAdapter(totals, grandTotal)
                binding.rvCategoryReport.adapter = adapter

                binding.tvEmpty.visibility = if (totals.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
