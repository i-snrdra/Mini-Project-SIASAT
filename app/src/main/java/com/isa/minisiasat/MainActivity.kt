package com.isa.minisiasat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.isa.minisiasat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
        checkUserSession()
    }
    
    private fun setupClickListeners() {
        binding.btnOpenSeeder.setOnClickListener {
            val intent = Intent(this, DataSeederActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun checkUserSession() {
        val userId = intent.getStringExtra("USER_ID")
        val userName = intent.getStringExtra("USER_NAME")
        val userRole = intent.getStringExtra("USER_ROLE")
        
        if (userId != null && userName != null && userRole != null) {
            // User sudah login, update UI
            updateUIForLoggedInUser(userName, userRole)
        }
    }
    
    private fun updateUIForLoggedInUser(userName: String, userRole: String) {
        // Update title
        binding.tvTitle.text = "Selamat Datang"
        
        // Update subtitle dengan informasi user
        val roleText = when (userRole) {
            "kaprogdi" -> "Kepala Program Studi"
            "dosen" -> "Dosen"
            "mahasiswa" -> "Mahasiswa"
            else -> "Pengguna"
        }
        
        binding.tvSubtitle.text = "$roleText: $userName"
        
        // Update button text
        binding.btnLogin.text = "Logout"
        binding.btnLogin.setOnClickListener {
            logout()
        }
    }
    
    private fun logout() {
        // Restart MainActivity tanpa extras
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}