package com.budgettracker.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.data.entities.Category
import com.budgettracker.databinding.ActivityAddCategoryBinding
import com.budgettracker.ui.adapters.CategoryAdapter
import com.budgettracker.utils.SessionManager
import kotlinx.coroutines.launch

class AddCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    private val colorOptions = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#3F51B5",
        "#2196F3", "#00BCD4", "#4CAF50", "#FF9800",
        "#795548", "#607D8B"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Categories"

        setupRecyclerView()
        observeCategories()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
    }

    private fun observeCategories() {
        db.categoryDao().getCategoriesForUser(sessionManager.getUserId())
            .observe(this) { categories ->
                val adapter = CategoryAdapter(categories) { category ->
                    deleteCategory(category)
                }
                binding.rvCategories.adapter = adapter
            }
    }

    private fun setupClickListeners() {
        binding.btnAddCategory.setOnClickListener {
            val name = binding.etCategoryName.text.toString().trim()
            if (name.isEmpty()) {
                binding.tilCategoryName.error = "Category name is required"
                return@setOnClickListener
            }
            if (name.length > 50) {
                binding.tilCategoryName.error = "Name must be under 50 characters"
                return@setOnClickListener
            }
            binding.tilCategoryName.error = null

            val colorIndex = binding.colorPicker.checkedRadioButtonId
            val selectedColor = if (colorIndex >= 0 && colorIndex < colorOptions.size)
                colorOptions[colorIndex]
            else colorOptions[0]

            val category = Category(
                userId = sessionManager.getUserId(),
                name = name,
                colorHex = selectedColor
            )

            lifecycleScope.launch {
                db.categoryDao().insertCategory(category)
                runOnUiThread {
                    binding.etCategoryName.setText("")
                    Toast.makeText(this@AddCategoryActivity, "Category added!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteCategory(category: Category) {
        lifecycleScope.launch {
            db.categoryDao().deleteCategory(category)
            runOnUiThread {
                Toast.makeText(this@AddCategoryActivity, "${category.name} deleted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
