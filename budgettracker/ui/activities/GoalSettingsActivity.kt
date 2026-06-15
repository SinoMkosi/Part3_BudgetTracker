package com.budgettracker.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.databinding.ActivityGoalSettingsBinding
import com.budgettracker.utils.DateUtils
import com.budgettracker.utils.SessionManager
import kotlinx.coroutines.launch

class GoalSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGoalSettingsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    // SeekBar max = 10000, each step = R10 or equivalent
    private val SEEKBAR_MAX = 200      // 0-200 steps
    private val STEP_VALUE = 50.0       // each step = 50 currency units

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Monthly Goals"

        loadCurrentGoals()
        setupSeekBars()

        binding.btnSaveGoals.setOnClickListener { saveGoals() }
    }

    private fun loadCurrentGoals() {
        db.userDao().getUserById(sessionManager.getUserId()).observe(this) { user ->
            user?.let {
                val minSteps = (it.minMonthlyGoal / STEP_VALUE).toInt().coerceIn(0, SEEKBAR_MAX)
                val maxSteps = (it.maxMonthlyGoal / STEP_VALUE).toInt().coerceIn(0, SEEKBAR_MAX)
                binding.seekBarMin.progress = minSteps
                binding.seekBarMax.progress = maxSteps
                updateMinLabel(minSteps)
                updateMaxLabel(maxSteps)
                binding.etMinGoal.setText(it.minMonthlyGoal.toString())
                binding.etMaxGoal.setText(it.maxMonthlyGoal.toString())
            }
        }
    }

    private fun setupSeekBars() {
        binding.seekBarMin.max = SEEKBAR_MAX
        binding.seekBarMax.max = SEEKBAR_MAX

        binding.seekBarMin.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateMinLabel(progress)
                    binding.etMinGoal.setText((progress * STEP_VALUE).toString())
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        binding.seekBarMax.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateMaxLabel(progress)
                    binding.etMaxGoal.setText((progress * STEP_VALUE).toString())
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }

    private fun updateMinLabel(steps: Int) {
        val amount = steps * STEP_VALUE
        binding.tvMinGoalValue.text = "Min: ${DateUtils.formatCurrency(amount)}"
    }

    private fun updateMaxLabel(steps: Int) {
        val amount = steps * STEP_VALUE
        binding.tvMaxGoalValue.text = "Max: ${DateUtils.formatCurrency(amount)}"
    }

    private fun saveGoals() {
        val minText = binding.etMinGoal.text.toString().trim()
        val maxText = binding.etMaxGoal.text.toString().trim()

        val min = minText.toDoubleOrNull()
        val max = maxText.toDoubleOrNull()

        if (min == null || min < 0) {
            binding.tilMinGoal.error = "Enter a valid minimum amount"
            return
        }
        binding.tilMinGoal.error = null

        if (max == null || max < 0) {
            binding.tilMaxGoal.error = "Enter a valid maximum amount"
            return
        }
        binding.tilMaxGoal.error = null

        if (max > 0 && min > max) {
            Toast.makeText(this, "Minimum cannot exceed maximum goal", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            // We need to get the user first then update
            val prefs = getSharedPreferences("goals_${userId}", MODE_PRIVATE)
            prefs.edit().putFloat("min", min.toFloat()).putFloat("max", max.toFloat()).apply()

            // Update in DB
            val user = db.userDao().getUserById(userId)
            user.observe(this@GoalSettingsActivity) { u ->
                if (u != null) {
                    lifecycleScope.launch {
                        db.userDao().updateUser(u.copy(minMonthlyGoal = min, maxMonthlyGoal = max))
                        runOnUiThread {
                            Toast.makeText(this@GoalSettingsActivity, "Goals saved!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
