package com.isa.minisiasat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.isa.minisiasat.databinding.ActivityDosenProfileBinding
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DosenProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDosenProfileBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var firestore: FirebaseFirestore
    
    private var currentDosenId: String = ""
    private var isEditMode: Boolean = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        firestore = FirebaseFirestore.getInstance()
        currentDosenId = sessionManager.getCurrentUserId()
        
        setupUI()
        setupClickListeners()
        loadProfileData()
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        
        setEditMode(false)
    }
    
    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            setEditMode(true)
        }
        
        binding.btnSave.setOnClickListener {
            saveProfile()
        }
        
        binding.btnCancel.setOnClickListener {
            setEditMode(false)
            loadProfileData() // Reset form
        }
    }
    
    private fun setEditMode(editMode: Boolean) {
        isEditMode = editMode
        
        // Toggle visibility
        binding.btnEdit.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.layoutButtons.visibility = if (editMode) View.VISIBLE else View.GONE
        
        // Toggle edit text enable
        binding.etNama.isEnabled = editMode
        binding.etEmail.isEnabled = editMode
        binding.etNip.isEnabled = editMode
        binding.etNoTelp.isEnabled = editMode
        binding.etAlamat.isEnabled = editMode
        
        // Update button text
        binding.tvTitle.text = if (editMode) "Edit Profile" else "Profile Dosen"
    }
    
    private fun loadProfileData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val document = firestore.collection("users_dosen")
                    .document(currentDosenId)
                    .get()
                    .await()
                
                if (document.exists()) {
                    val data = document.data
                    
                    binding.etNama.setText(data?.get("nama")?.toString() ?: "")
                    binding.etEmail.setText(data?.get("email")?.toString() ?: "")
                    binding.etNip.setText(data?.get("nip")?.toString() ?: "")
                    binding.etNoTelp.setText(data?.get("noTelp")?.toString() ?: "")
                    binding.etAlamat.setText(data?.get("alamat")?.toString() ?: "")
                    
                    // Data read-only
                    binding.tvUserId.text = currentDosenId
                    binding.tvRole.text = data?.get("role")?.toString() ?: "dosen"
                    binding.tvJoinDate.text = "Bergabung: ${data?.get("tanggalDaftar")?.toString() ?: ""}"
                    
                } else {
                    Toast.makeText(this@DosenProfileActivity, 
                        "Data profile tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                
            } catch (e: Exception) {
                Toast.makeText(this@DosenProfileActivity, 
                    "Gagal memuat data profile: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun saveProfile() {
        if (!validateInput()) return
        
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val profileData = mapOf(
                    "nama" to binding.etNama.text.toString().trim(),
                    "email" to binding.etEmail.text.toString().trim(),
                    "nip" to binding.etNip.text.toString().trim(),
                    "noTelp" to binding.etNoTelp.text.toString().trim(),
                    "alamat" to binding.etAlamat.text.toString().trim(),
                    "lastUpdated" to System.currentTimeMillis()
                )
                
                firestore.collection("users_dosen")
                    .document(currentDosenId)
                    .update(profileData)
                    .await()
                
                // Update session manager dengan nama baru
                sessionManager.createSession(
                    currentDosenId,
                    binding.etNama.text.toString().trim(),
                    "dosen"
                )
                
                Toast.makeText(this@DosenProfileActivity, 
                    "Profile berhasil diperbarui", Toast.LENGTH_SHORT).show()
                
                setEditMode(false)
                
            } catch (e: Exception) {
                Toast.makeText(this@DosenProfileActivity, 
                    "Gagal menyimpan profile: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun validateInput(): Boolean {
        val nama = binding.etNama.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val nip = binding.etNip.text.toString().trim()
        
        if (nama.isEmpty()) {
            binding.etNama.error = "Nama tidak boleh kosong"
            binding.etNama.requestFocus()
            return false
        }
        
        if (email.isEmpty()) {
            binding.etEmail.error = "Email tidak boleh kosong"
            binding.etEmail.requestFocus()
            return false
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Format email tidak valid"
            binding.etEmail.requestFocus()
            return false
        }
        
        if (nip.isEmpty()) {
            binding.etNip.error = "NIP tidak boleh kosong"
            binding.etNip.requestFocus()
            return false
        }
        
        return true
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.scrollView.visibility = if (show) View.GONE else View.VISIBLE
    }
} 