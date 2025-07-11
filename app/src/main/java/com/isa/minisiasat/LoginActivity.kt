package com.isa.minisiasat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.isa.minisiasat.databinding.ActivityLoginBinding
import com.isa.minisiasat.utils.UserRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val userRepository = UserRepository()
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val userId = binding.etUserId.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (validateInput(userId, password)) {
                performLogin(userId, password)
            }
        }
    }
    
    private fun validateInput(userId: String, password: String): Boolean {
        when {
            userId.isEmpty() -> {
                binding.etUserId.error = "ID Pengguna tidak boleh kosong"
                binding.etUserId.requestFocus()
                return false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password tidak boleh kosong"
                binding.etPassword.requestFocus()
                return false
            }
            else -> return true
        }
    }
    
    private fun performLogin(userId: String, password: String) {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val user = userRepository.authenticateUser(userId, password)
                
                if (user != null) {
                    // Login berhasil
                    showLoading(false)
                    showToast("Selamat datang, ${user.nama}!")
                    
                    // Simpan session
                    sessionManager.createSession(user.id, user.nama, user.role)
                    
                    // Navigasi ke halaman yang sesuai berdasarkan role
                    when (user.role) {
                        "kaprogdi" -> {
                            val intent = Intent(this@LoginActivity, KaprogdiDashboardActivity::class.java)
                            intent.putExtra("USER_ID", user.id)
                            intent.putExtra("USER_NAME", user.nama)
                            intent.putExtra("USER_ROLE", user.role)
                            startActivity(intent)
                            finish()
                        }
                        "dosen" -> {
                            val intent = Intent(this@LoginActivity, DosenDashboardActivity::class.java)
                            intent.putExtra("USER_ID", user.id)
                            intent.putExtra("USER_NAME", user.nama)
                            intent.putExtra("USER_ROLE", user.role)
                            startActivity(intent)
                            finish()
                        }
                        "mahasiswa" -> {
                            // TODO: Navigasi ke halaman mahasiswa
                            showToast("Login sebagai Mahasiswa: ${user.nama}")
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("USER_ID", user.id)
                            intent.putExtra("USER_NAME", user.nama)
                            intent.putExtra("USER_ROLE", user.role)
                            startActivity(intent)
                            finish()
                        }
                    }
                    
                } else {
                    // Login gagal
                    showLoading(false)
                    showToast("ID Pengguna atau Password salah!")
                    
                    // Clear password field
                    binding.etPassword.text?.clear()
                    binding.etPassword.requestFocus()
                }
                
            } catch (e: Exception) {
                showLoading(false)
                showToast("Terjadi kesalahan: ${e.message}")
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false
            binding.btnLogin.text = "Memproses..."
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text = "Masuk"
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 