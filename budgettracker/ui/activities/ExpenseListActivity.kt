package com.budgettracker.ui.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityExpenseListBinding
import com.budgettracker.ui.adapters.ExpenseAdapter
import com.budgettracker.utils.DateUtils
import com.budgettracker.utils.SessionManager
import java.util.Calendar

class ExpenseListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseListBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    private var startDate = DateUtils.getFirstDayOfMonthDbString()
    private var endDate = DateUtils.getTodayDbString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Expenses"

        binding.rvExpenses.layoutManager = LinearLayoutManager(this)

        updateDateButtons()
        loadExpenses()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnStartDate.setOnClickListener { showDatePicker(true) }
        binding.btnEndDate.setOnClickListener { showDatePicker(false) }
    }

    private fun showDatePicker(isStart: Boolean) {
        val date = if (isStart) startDate else endDate
        val parts = date.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val day = parts[2].toInt()

        DatePickerDialog(this, { _, y, m, d ->
            val selected = "%04d-%02d-%02d".format(y, m + 1, d)
            if (isStart) {
                if (selected <= endDate) {
                    startDate = selected
                    updateDateButtons()
                    loadExpenses()
                } else {
                    android.widget.Toast.makeText(this, "Start date must be before end date", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                if (selected >= startDate) {
                    endDate = selected
                    updateDateButtons()
                    loadExpenses()
                } else {
                    android.widget.Toast.makeText(this, "End date must be after start date", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }, year, month, day).show()
    }

    private fun updateDateButtons() {
        binding.btnStartDate.text = "From: ${DateUtils.dbStringToDisplay(startDate)}"
        binding.btnEndDate.text = "To: ${DateUtils.dbStringToDisplay(endDate)}"
    }

    private fun loadExpenses() {
        db.expenseDao()
            .getExpensesWithCategoryForPeriod(sessionManager.getUserId(), startDate, endDate)
            .observe(this) { expenses ->
                val adapter = ExpenseAdapter(expenses) { expenseWithCategory ->
                    val intent = Intent(this, ExpenseDetailActivity::class.java)
                    intent.putExtra("expense_id", expenseWithCategory.expense.id)
                    startActivity(intent)
                }
                binding.rvExpenses.adapter = adapter

                val total = expenses.sumOf { it.expense.amount }
                binding.tvTotal.text = "Total: ${DateUtils.formatCurrency(total)} (${expenses.size} entries)"

                binding.tvEmpty.visibility =
                    if (expenses.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
