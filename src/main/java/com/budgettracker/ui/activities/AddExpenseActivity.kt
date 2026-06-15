package com.budgettracker.ui.activities

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.budgettracker.data.database.AppDatabase
import com.budgettracker.data.entities.Category
import com.budgettracker.data.entities.Expense
import com.budgettracker.databinding.ActivityAddExpenseBinding
import com.budgettracker.utils.DateUtils
import com.budgettracker.utils.SessionManager
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var db: AppDatabase

    private var selectedDate = DateUtils.getTodayDbString()
    private var selectedStartTime = "08:00"
    private var selectedEndTime = "09:00"
    private var photoPath: String? = null
    private var currentPhotoUri: Uri? = null
    private var categories: List<Category> = emptyList()

    // Camera launcher
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoPath = currentPhotoUri?.path
                loadPhotoPreview()
            }
        }

    // Gallery launcher
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                photoPath = getRealPathFromUri(it)
                currentPhotoUri = it
                loadPhotoPreview()
            }
        }

    // Permission launchers
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) openCamera()
            else Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }

    private val requestGalleryPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) pickImageLauncher.launch("image/*")
            else Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        db = AppDatabase.getDatabase(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Expense"

        updateDateDisplay()
        updateTimeDisplays()
        loadCategories()
        setupClickListeners()
    }

    private fun loadCategories() {
        db.categoryDao().getCategoriesForUser(sessionManager.getUserId())
            .observe(this) { cats ->
                categories = cats
                val names = cats.map { it.name }.toMutableList()
                names.add(0, "Select category (optional)")
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerCategory.adapter = adapter
            }
    }

    private fun setupClickListeners() {
        binding.btnDate.setOnClickListener { showDatePicker() }
        binding.btnStartTime.setOnClickListener { showTimePicker(true) }
        binding.btnEndTime.setOnClickListener { showTimePicker(false) }
        binding.btnTakePhoto.setOnClickListener { checkCameraPermissionAndOpen() }
        binding.btnPickPhoto.setOnClickListener { checkGalleryPermissionAndOpen() }
        binding.btnRemovePhoto.setOnClickListener { removePhoto() }
        binding.btnSave.setOnClickListener { saveExpense() }
    }

    private fun showDatePicker() {
        val parts = selectedDate.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt() - 1
        val day = parts[2].toInt()

        DatePickerDialog(this, { _, y, m, d ->
            selectedDate = "%04d-%02d-%02d".format(y, m + 1, d)
            updateDateDisplay()
        }, year, month, day).show()
    }

    private fun showTimePicker(isStart: Boolean) {
        val time = if (isStart) selectedStartTime else selectedEndTime
        val (hour, minute) = DateUtils.parseTime(time)

        TimePickerDialog(this, { _, h, m ->
            val formatted = DateUtils.formatTime(h, m)
            if (isStart) {
                selectedStartTime = formatted
            } else {
                selectedEndTime = formatted
            }
            updateTimeDisplays()
        }, hour, minute, true).show()
    }

    private fun updateDateDisplay() {
        binding.btnDate.text = DateUtils.dbStringToDisplay(selectedDate)
    }

    private fun updateTimeDisplays() {
        binding.btnStartTime.text = "Start: $selectedStartTime"
        binding.btnEndTime.text = "End:   $selectedEndTime"
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> openCamera()
            else -> requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkGalleryPermissionAndOpen() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED -> pickImageLauncher.launch("image/*")
            else -> requestGalleryPermission.launch(permission)
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        currentPhotoUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            photoFile
        )
        photoPath = photoFile.absolutePath
        takePictureLauncher.launch(currentPhotoUri)
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "expense_${System.currentTimeMillis()}_",
            ".jpg",
            storageDir
        )
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { out -> inputStream?.copyTo(out) }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun loadPhotoPreview() {
        photoPath?.let { path ->
            binding.ivPhotoPreview.visibility = View.VISIBLE
            binding.btnRemovePhoto.visibility = View.VISIBLE
            Glide.with(this)
                .load(File(path))
                .centerCrop()
                .into(binding.ivPhotoPreview)
        }
    }

    private fun removePhoto() {
        photoPath = null
        currentPhotoUri = null
        binding.ivPhotoPreview.visibility = View.GONE
        binding.btnRemovePhoto.visibility = View.GONE
    }

    private fun saveExpense() {
        val description = binding.etDescription.text.toString().trim()
        val amountText = binding.etAmount.text.toString().trim()

        // Validate
        var valid = true

        if (description.isEmpty()) {
            binding.tilDescription.error = "Description is required"
            valid = false
        } else {
            binding.tilDescription.error = null
        }

        if (amountText.isEmpty()) {
            binding.tilAmount.error = "Amount is required"
            valid = false
        } else {
            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                binding.tilAmount.error = "Enter a valid positive amount"
                valid = false
            } else {
                binding.tilAmount.error = null
            }
        }

        if (!DateUtils.isEndTimeAfterStartTime(selectedStartTime, selectedEndTime)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            valid = false
        }

        if (!valid) return

        val amount = amountText.toDouble()
        val categoryIndex = binding.spinnerCategory.selectedItemPosition
        val categoryId = if (categoryIndex > 0 && categories.isNotEmpty()) {
            categories[categoryIndex - 1].id
        } else null

        val expense = Expense(
            userId = sessionManager.getUserId(),
            categoryId = categoryId,
            date = selectedDate,
            startTime = selectedStartTime,
            endTime = selectedEndTime,
            description = description,
            amount = amount,
            photoPath = photoPath
        )

        lifecycleScope.launch {
            db.expenseDao().insertExpense(expense)
            runOnUiThread {
                Toast.makeText(this@AddExpenseActivity, "Expense saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
