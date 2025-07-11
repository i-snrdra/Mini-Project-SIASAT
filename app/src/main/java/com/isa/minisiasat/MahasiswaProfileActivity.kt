package com.isa.minisiasat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.isa.minisiasat.databinding.ActivityMahasiswaProfileBinding
import com.isa.minisiasat.repository.MahasiswaRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MahasiswaProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMahasiswaProfileBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var firestore: FirebaseFirestore
    private lateinit var mahasiswaRepository: MahasiswaRepository
    
    private var currentMahasiswaId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMahasiswaProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        firestore = FirebaseFirestore.getInstance()
        mahasiswaRepository = MahasiswaRepository()
        currentMahasiswaId = sessionManager.getCurrentUserId()
        
        setupUI()
        setupClickListeners()
        loadProfileData()
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnUpdatePassword.setOnClickListener {
            showUpdatePasswordDialog()
        }
    }
    
    private fun loadProfileData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val document = firestore.collection("users_mahasiswa")
                    .document(currentMahasiswaId)
                    .get()
                    .await()
                
                if (document.exists()) {
                    val data = document.data
                    
                    binding.tvNama.text = data?.get("nama")?.toString() ?: ""
                    binding.tvNim.text = currentMahasiswaId
                    binding.tvAngkatan.text = data?.get("angkatan")?.toString() ?: ""
                    binding.tvRole.text = "Mahasiswa"
                    binding.tvJoinDate.text = "Bergabung: ${data?.get("tanggalDaftar")?.toString() ?: ""}"
                    
                } else {
                    Toast.makeText(this@MahasiswaProfileActivity, 
                        "Data profile tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@MahasiswaProfileActivity, 
                    "Gagal memuat data profile: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showUpdatePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_password, null)
        val etCurrentPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etConfirmPassword)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Update Password")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val currentPassword = etCurrentPassword.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()
                
                if (validatePasswordInput(currentPassword, newPassword, confirmPassword)) {
                    updatePassword(currentPassword, newPassword)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun validatePasswordInput(current: String, new: String, confirm: String): Boolean {
        if (current.isEmpty()) {
            Toast.makeText(this, "Password saat ini tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (new.isEmpty()) {
            Toast.makeText(this, "Password baru tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (new.length < 6) {
            Toast.makeText(this, "Password baru minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (new != confirm) {
            Toast.makeText(this, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun updatePassword(currentPassword: String, newPassword: String) {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                // Verify current password
                val document = firestore.collection("users_mahasiswa")
                    .document(currentMahasiswaId)
                    .get()
                    .await()
                
                val storedPassword = document.data?.get("password")?.toString()
                
                if (storedPassword != currentPassword) {
                    Toast.makeText(this@MahasiswaProfileActivity, 
                        "Password saat ini salah", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // Update password
                val success = mahasiswaRepository.updatePassword(currentMahasiswaId, newPassword)
                
                if (success) {
                    Toast.makeText(this@MahasiswaProfileActivity, 
                        "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MahasiswaProfileActivity, 
                        "Gagal memperbarui password", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@MahasiswaProfileActivity, 
                    "Gagal memperbarui password: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.scrollView.visibility = if (show) View.GONE else View.VISIBLE
    }
} 