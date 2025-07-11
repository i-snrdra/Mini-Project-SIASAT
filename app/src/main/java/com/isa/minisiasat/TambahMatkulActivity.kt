package com.isa.minisiasat

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.isa.minisiasat.databinding.ActivityTambahMatkulBinding
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.repository.MatkulRepository
import kotlinx.coroutines.launch
import java.util.*

class TambahMatkulActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTambahMatkulBinding
    private val matkulRepository = MatkulRepository()
    
    private val hariList = arrayOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahMatkulBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupClickListeners()
    }
    
    private fun setupUI() {
        // Setup dropdown untuk hari
        val hariAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, hariList)
        binding.etHari.setAdapter(hariAdapter)
        binding.etHari.setOnClickListener {
            binding.etHari.showDropDown()
        }
        
        // Setup validasi kode matkul real-time
        binding.etKodeMatkul.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val kode = s.toString().trim().uppercase()
                if (kode.length >= 3) {
                    checkKodeMatkulDuplicate(kode)
                }
            }
        })
    }
    
    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            finish()
        }
        
        binding.btnSave.setOnClickListener {
            tambahMatkul()
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
    
    private fun tambahMatkul() {
        val kodeMatkul = binding.etKodeMatkul.text.toString().trim()
        val namaMatkul = binding.etNamaMatkul.text.toString().trim()
        val hari = binding.etHari.text.toString().trim()
        val jamMulai = binding.etJamMulai.text.toString().trim()
        val jamSelesai = binding.etJamSelesai.text.toString().trim()
        val kapasitasStr = binding.etKapasitas.text.toString().trim()
        val sksStr = binding.etSks.text.toString().trim()
        
        // Validasi input
        if (kodeMatkul.isEmpty()) {
            showToast("Kode mata kuliah tidak boleh kosong")
            binding.etKodeMatkul.requestFocus()
            return
        }
        
        if (binding.etKodeMatkul.error != null) {
            showToast("Perbaiki error pada kode mata kuliah")
            binding.etKodeMatkul.requestFocus()
            return
        }
        
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
        
        // Simpan mata kuliah
        val matkul = Matkul(
            kodeMatkul = kodeMatkul.uppercase(),
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
                val result = matkulRepository.tambahMatkul(matkul)
                showLoading(false)
                
                when (result) {
                    MatkulRepository.TambahMatkulResult.SUCCESS -> {
                        showToast("Mata kuliah berhasil ditambahkan")
                        finish()
                    }
                    MatkulRepository.TambahMatkulResult.DUPLICATE_CODE -> {
                        showToast("Kode mata kuliah ${matkul.kodeMatkul} sudah ada!")
                        binding.etKodeMatkul.requestFocus()
                    }
                    MatkulRepository.TambahMatkulResult.ERROR -> {
                        showToast("Gagal menambahkan mata kuliah")
                    }
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
    
    private fun checkKodeMatkulDuplicate(kode: String) {
        lifecycleScope.launch {
            try {
                val existingMatkul = matkulRepository.getMatkulByKode(kode)
                if (existingMatkul != null) {
                    binding.etKodeMatkul.error = "Kode mata kuliah sudah ada"
                } else {
                    binding.etKodeMatkul.error = null
                }
            } catch (e: Exception) {
                // Ignore error untuk validasi real-time
            }
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 