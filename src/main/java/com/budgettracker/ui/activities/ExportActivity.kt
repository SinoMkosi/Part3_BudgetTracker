package com.budgettracker.ui.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityExportBinding
import com.budgettracker.utils.DateUtils
import com.budgettracker.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

class ExportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExportBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    private var startDate = DateUtils.getFirstDayOfMonthDbString()
    private var endDate = DateUtils.getTodayDbString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Export Expenses"

        updateDateButtons()
        binding.btnExportStartDate.setOnClickListener { showDatePicker(true) }
        binding.btnExportEndDate.setOnClickListener { showDatePicker(false) }
        binding.btnExportCsv.setOnClickListener { exportToCsv() }
    }

    private fun showDatePicker(isStart: Boolean) {
        val date = if (isStart) startDate else endDate
        val parts = date.split("-")
        DatePickerDialog(this, { _, y, m, d ->
            val selected = "%04d-%02d-%02d".format(y, m + 1, d)
            if (isStart) {
                if (selected <= endDate) { startDate = selected; updateDateButtons() }
                else Toast.makeText(this, "Start must be before end", Toast.LENGTH_SHORT).show()
            } else {
                if (selected >= startDate) { endDate = selected; updateDateButtons() }
                else Toast.makeText(this, "End must be after start", Toast.LENGTH_SHORT).show()
            }
        }, parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt()).show()
    }

    private fun updateDateButtons() {
        binding.btnExportStartDate.text = "From: ${DateUtils.dbStringToDisplay(startDate)}"
        binding.btnExportEndDate.text = "To: ${DateUtils.dbStringToDisplay(endDate)}"
    }

    private fun exportToCsv() {
        val userId = sessionManager.getUserId()
        lifecycleScope.launch {
            val expenses = db.expenseDao()
                .getExpensesWithCategoryForPeriodSync(userId, startDate, endDate)

            if (expenses.isEmpty()) {
                runOnUiThread { Toast.makeText(this@ExportActivity, "No expenses found for this period", Toast.LENGTH_SHORT).show() }
                return@launch
            }

            try {
                val fileName = "spendwise_${startDate}_to_${endDate}.csv"
                val exportDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                val file = File(exportDir, fileName)

                FileWriter(file).use { writer ->
                    writer.append("Date,Description,Category,Amount,Start Time,End Time\n")
                    expenses.forEach { ewc ->
                        val e = ewc.expense
                        val cat = ewc.category?.name ?: "Uncategorised"
                        writer.append("${e.date},\"${e.description}\",\"$cat\",${e.amount},${e.startTime},${e.endTime}\n")
                    }
                }

                runOnUiThread {
                    Toast.makeText(this@ExportActivity, "Exported: $fileName", Toast.LENGTH_LONG).show()
                    val uri = FileProvider.getUriForFile(this@ExportActivity, "${packageName}.fileprovider", file)
                    startActivity(Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }, "Share CSV Export"
                    ))
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this@ExportActivity, "Export failed: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}