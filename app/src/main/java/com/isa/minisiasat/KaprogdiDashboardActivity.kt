package com.isa.minisiasat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.isa.minisiasat.databinding.ActivityKaprogdiDashboardBinding
import com.isa.minisiasat.repository.MatkulRepository
import kotlinx.coroutines.launch

class KaprogdiDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKaprogdiDashboardBinding
    private val matkulRepository = MatkulRepository()
    
    private var userName: String = ""
    private var userId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKaprogdiDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get user data from intent
        userName = intent.getStringExtra("USER_NAME") ?: ""
        userId = intent.getStringExtra("USER_ID") ?: ""
        
        setupUI()
        setupClickListeners()
        loadDashboardData()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning from other activities
        loadDashboardData()
    }
    
    private fun setupUI() {
        if (userName.isNotEmpty()) {
            binding.tvUserName.text = "Selamat datang, $userName"
        }
    }
    
    private fun setupClickListeners() {
        binding.btnTambahMatkul.setOnClickListener {
            val intent = Intent(this, TambahMatkulActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnAssignDosen.setOnClickListener {
            val intent = Intent(this, AssignDosenActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnLihatMatkul.setOnClickListener {
            val intent = Intent(this, LihatMatkulActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }
    
    private fun loadDashboardData() {
        lifecycleScope.launch {
            try {
                val stats = matkulRepository.getDashboardStats()
                
                binding.tvTotalMatkul.text = stats.totalMatkul.toString()
                binding.tvTotalDosen.text = stats.totalDosen.toString()
                binding.tvMatkulWithDosen.text = stats.matkulWithDosen.toString()
                binding.tvMatkulWithoutDosen.text = stats.matkulWithoutDosen.toString()
                
            } catch (e: Exception) {
                showToast("Gagal memuat data dashboard: ${e.message}")
            }
        }
    }
    
    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 