package com.isa.minisiasat

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.isa.minisiasat.databinding.ActivityEditMatkulBinding
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.repository.MatkulRepository
import kotlinx.coroutines.launch
import java.util.*

class EditMatkulActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditMatkulBinding
    private val matkulRepository = MatkulRepository()
    
    private val hariList = arrayOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    private var originalMatkul: Matkul? = null
    
    companion object {
        const val EXTRA_MATKUL_CODE = "extra_matkul_code"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMatkulBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val matkulCode = intent.getStringExtra(EXTRA_MATKUL_CODE)
        if (matkulCode == null) {
            showToast("Kode mata kuliah tidak valid")
            finish()
            return
        }
        
        setupUI()
        setupClickListeners()
        loadMatkulData(matkulCode)
    }
    
    private fun setupUI() {
        // Setup dropdown untuk hari
        val hariAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, hariList)
        binding.etHari.setAdapter(hariAdapter)
        binding.etHari.setOnClickListener {
            binding.etHari.showDropDown()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            finish()
        }
        
        binding.btnSave.setOnClickListener {
            updateMatkul()
        }
        
        // Time picker untuk jam mulai
        binding.etJamMulai.setOnClickListener {
            showTimePicker { time ->
                binding.etJamMulai.setText(time)
            }
        }
        
        // Time picker untuk jam selesai
        binding.etJamSelesai.setOnClickListener {
            showTimePicker { time ->
                binding.etJamSelesai.setText(time)
            }
        }
    }
    
    private fun loadMatkulData(matkulCode: String) {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val matkul = matkulRepository.getMatkulByKode(matkulCode)
                
                if (matkul != null) {
                    originalMatkul = matkul
                    populateForm(matkul)
                } else {
                    showToast("Mata kuliah tidak ditemukan")
                    finish()
                }
                
                showLoading(false)
            } catch (e: Exception) {
                showLoading(false)
                showToast("Error loading data: ${e.message}")
                finish()
            }
        }
    }
    
    private fun populateForm(matkul: Matkul) {
        binding.apply {
            etKodeMatkul.setText(matkul.kodeMatkul)
            etNamaMatkul.setText(matkul.namaMatkul)
            etHari.setText(matkul.hari, false)
            etJamMulai.setText(matkul.jamMulai)
            etJamSelesai.setText(matkul.jamSelesai)
            etKapasitas.setText(matkul.kapasitas.toString())
            etSks.setText(matkul.sks.toString())
        }
    }
    
    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(formattedTime)
            },
            hour,
            minute,
            true
        ).show()
    }
    
    private fun updateMatkul() {
        val originalMatkul = this.originalMatkul ?: return
        
        val namaMatkul = binding.etNamaMatkul.text.toString().trim()
        val hari = binding.etHari.text.toString().trim()
        val jamMulai = binding.etJamMulai.text.toString().trim()
        val jamSelesai = binding.etJamSelesai.text.toString().trim()
        val kapasitasStr = binding.etKapasitas.text.toString().trim()
        val sksStr = binding.etSks.text.toString().trim()
        
        // Validasi input
        if (namaMatkul.isEmpty()) {
            showToast("Nama mata kuliah tidak boleh kosong")
            binding.etNamaMatkul.requestFocus()
            return
        }
        
        if (hari.isEmpty()) {
            showToast("Pilih hari terlebih dahulu")
            binding.etHari.requestFocus()
            return
        }
        
        if (!hariList.contains(hari)) {
            showToast("Pilih hari yang valid")
            binding.etHari.requestFocus()
            return
        }
        
        if (jamMulai.isEmpty()) {
            showToast("Pilih jam mulai terlebih dahulu")
            binding.etJamMulai.requestFocus()
            return
        }
        
        if (jamSelesai.isEmpty()) {
            showToast("Pilih jam selesai terlebih dahulu")
            binding.etJamSelesai.requestFocus()
            return
        }
        
        if (kapasitasStr.isEmpty()) {
            showToast("Kapasitas tidak boleh kosong")
            binding.etKapasitas.requestFocus()
            return
        }
        
        if (sksStr.isEmpty()) {
            showToast("SKS tidak boleh kosong")
            binding.etSks.requestFocus()
            return
        }
        
        val kapasitas = try {
            kapasitasStr.toInt()
        } catch (e: NumberFormatException) {
            showToast("Kapasitas harus berupa angka")
            binding.etKapasitas.requestFocus()
            return
        }
        
        val sks = try {
            sksStr.toInt()
        } catch (e: NumberFormatException) {
            showToast("SKS harus berupa angka")
            binding.etSks.requestFocus()
            return
        }
        
        if (kapasitas <= 0) {
            showToast("Kapasitas harus lebih dari 0")
            binding.etKapasitas.requestFocus()
            return
        }
        
        if (sks <= 0) {
            showToast("SKS harus lebih dari 0")
            binding.etSks.requestFocus()
            return
        }
        
        // Validasi jam
        if (!isValidTimeFormat(jamMulai)) {
            showToast("Format jam mulai tidak valid")
            return
        }
        
        if (!isValidTimeFormat(jamSelesai)) {
            showToast("Format jam selesai tidak valid")
            return
        }
        
        if (!isTimeAfter(jamMulai, jamSelesai)) {
            showToast("Jam selesai harus setelah jam mulai")
            return
        }
        
        // Update mata kuliah dengan data baru, tetapi pertahankan info dosen
        val updatedMatkul = originalMatkul.copy(
            namaMatkul = namaMatkul,
            hari = hari,
            jamMulai = jamMulai,
            jamSelesai = jamSelesai,
            kapasitas = kapasitas,
            sks = sks
        )
        
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val success = matkulRepository.updateMatkul(updatedMatkul)
                showLoading(false)
                
                if (success) {
                    showToast("Mata kuliah berhasil diupdate")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    showToast("Gagal mengupdate mata kuliah")
                }
            } catch (e: Exception) {
                showLoading(false)
                showToast("Error: ${e.message}")
            }
        }
    }
    
    private fun isValidTimeFormat(time: String): Boolean {
        return try {
            val parts = time.split(":")
            if (parts.size != 2) return false
            
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            
            hour in 0..23 && minute in 0..59
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isTimeAfter(startTime: String, endTime: String): Boolean {
        return try {
            val startParts = startTime.split(":")
            val endParts = endTime.split(":")
            
            val startHour = startParts[0].toInt()
            val startMinute = startParts[1].toInt()
            val endHour = endParts[0].toInt()
            val endMinute = endParts[1].toInt()
            
            val startTotalMinutes = startHour * 60 + startMinute
            val endTotalMinutes = endHour * 60 + endMinute
            
            endTotalMinutes > startTotalMinutes
        } catch (e: Exception) {
            false
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !show
        binding.btnCancel.isEnabled = !show
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 