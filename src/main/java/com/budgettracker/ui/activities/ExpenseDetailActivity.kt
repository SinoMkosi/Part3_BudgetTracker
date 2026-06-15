package com.budgettracker.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityExpenseDetailBinding
import com.budgettracker.utils.DateUtils
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File

class ExpenseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseDetailBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Expense Detail"

        val expenseId = intent.getIntExtra("expense_id", -1)
        if (expenseId == -1) {
            finish()
            return
        }

        loadExpense(expenseId)
    }

    private fun loadExpense(expenseId: Int) {
        lifecycleScope.launch {
            val expense = db.expenseDao().getExpenseById(expenseId)
            if (expense == null) {
                runOnUiThread { finish() }
                return@launch
            }

            val category = expense.categoryId?.let {
                db.categoryDao().getCategoryById(it)
            }

            runOnUiThread {
                binding.tvDescription.text = expense.description
                binding.tvDate.text = "Date: ${DateUtils.dbStringToDisplay(expense.date)}"
                binding.tvTime.text = "Time: ${expense.startTime} – ${expense.endTime}"
                binding.tvAmount.text = "Amount: ${DateUtils.formatCurrency(expense.amount)}"
                binding.tvCategory.text = "Category: ${category?.name ?: "Uncategorised"}"

                if (expense.photoPath != null) {
                    val file = File(expense.photoPath)
                    if (file.exists()) {
                        binding.ivExpensePhoto.visibility = View.VISIBLE
                        binding.tvNoPhoto.visibility = View.GONE
                        Glide.with(this@ExpenseDetailActivity)
                            .load(file)
                            .centerCrop()
                            .into(binding.ivExpensePhoto)
                    } else {
                        binding.ivExpensePhoto.visibility = View.GONE
                        binding.tvNoPhoto.visibility = View.VISIBLE
                        binding.tvNoPhoto.text = "Photo not found"
                    }
                } else {
                    binding.ivExpensePhoto.visibility = View.GONE
                    binding.tvNoPhoto.visibility = View.VISIBLE
                    binding.tvNoPhoto.text = "No photo attached"
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
