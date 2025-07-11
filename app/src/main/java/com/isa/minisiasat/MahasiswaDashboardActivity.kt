package com.isa.minisiasat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapters.MatkulMahasiswaAdapter
import com.isa.minisiasat.databinding.ActivityMahasiswaDashboardBinding
import com.isa.minisiasat.models.AbsensiSession
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.repository.MahasiswaRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch

class MahasiswaDashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMahasiswaDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var mahasiswaRepository: MahasiswaRepository
    
    private lateinit var matkulAdapter: MatkulMahasiswaAdapter
    private var currentMahasiswaId: String = ""
    private var currentMahasiswaNama: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMahasiswaDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        mahasiswaRepository = MahasiswaRepository()
        
        initializeUserData()
        setupRecyclerView()
        setupClickListeners()
        
        loadDashboardData()
    }
    
    override fun onResume() {
        super.onResume()
        loadDashboardData() // Refresh data saat kembali ke dashboard
    }
    
    private fun initializeUserData() {
        currentMahasiswaId = sessionManager.getCurrentUserId()
        currentMahasiswaNama = sessionManager.getCurrentUserName()
        
        Log.d("MahasiswaDashboard", "Current Mahasiswa ID: $currentMahasiswaId")
        Log.d("MahasiswaDashboard", "Current Mahasiswa Name: $currentMahasiswaNama")
        
        binding.tvWelcome.text = "Selamat datang, $currentMahasiswaNama"
        binding.tvNim.text = "NIM: $currentMahasiswaId"
    }
    
    private fun setupRecyclerView() {
        matkulAdapter = MatkulMahasiswaAdapter()
        
        binding.rvMatkulHariIni.apply {
            layoutManager = LinearLayoutManager(this@MahasiswaDashboardActivity)
            adapter = matkulAdapter
        }
    }
    
    private fun setupClickListeners() {
        // Menu Card Clicks
        binding.cardMataKuliah.setOnClickListener {
            val intent = Intent(this, MahasiswaMatkulActivity::class.java)
            startActivity(intent)
        }
        
        binding.cardAbsensi.setOnClickListener {
            val intent = Intent(this, MahasiswaAbsensiActivity::class.java)
            startActivity(intent)
        }
        
        binding.cardNilai.setOnClickListener {
            val intent = Intent(this, MahasiswaNilaiActivity::class.java)
            startActivity(intent)
        }
        
        binding.cardJadwal.setOnClickListener {
            val intent = Intent(this, MahasiswaJadwalActivity::class.java)
            startActivity(intent)
        }
        
        binding.cardProfile.setOnClickListener {
            val intent = Intent(this, MahasiswaProfileActivity::class.java)
            startActivity(intent)
        }
        
        binding.cardEnrollment.setOnClickListener {
            val intent = Intent(this, MahasiswaEnrollmentActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun loadDashboardData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                Log.d("MahasiswaDashboard", "Starting to load dashboard data...")
                
                // Load mata kuliah hari ini
                val matkulHariIni = mahasiswaRepository.getMatkulHariIni(currentMahasiswaId)
                Log.d("MahasiswaDashboard", "Loaded ${matkulHariIni.size} matkul hari ini")
                updateMatkulHariIni(matkulHariIni)
                
                // Load absensi aktif
                val absensiAktif = mahasiswaRepository.getAbsensiAktif(currentMahasiswaId)
                Log.d("MahasiswaDashboard", "Loaded ${absensiAktif.size} absensi aktif")
                updateAbsensiAktif(absensiAktif)
                
            } catch (e: Exception) {
                Log.e("MahasiswaDashboard", "Error loading dashboard data", e)
                Toast.makeText(this@MahasiswaDashboardActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun updateMatkulHariIni(matkulList: List<Matkul>) {
        if (matkulList.isEmpty()) {
            binding.rvMatkulHariIni.visibility = View.GONE
            binding.layoutEmptyMatkul.visibility = View.VISIBLE
        } else {
            binding.rvMatkulHariIni.visibility = View.VISIBLE
            binding.layoutEmptyMatkul.visibility = View.GONE
            matkulAdapter.updateData(matkulList)
        }
    }
    
    private fun updateAbsensiAktif(absensiList: List<AbsensiSession>) {
        if (absensiList.isEmpty()) {
            binding.layoutAbsensiAktif.visibility = View.GONE
        } else {
            binding.layoutAbsensiAktif.visibility = View.VISIBLE
            
            // Ambil absensi aktif pertama
            val absensi = absensiList.first()
            
            // Cari nama mata kuliah
            lifecycleScope.launch {
                try {
                    val allMatkul = mahasiswaRepository.getMatkulByMahasiswa(currentMahasiswaId)
                    val matkul = allMatkul.find { it.kodeMatkul == absensi.matkulCode }
                    
                    if (matkul != null) {
                        binding.tvAbsensiMatkul.text = "${matkul.kodeMatkul} - ${matkul.namaMatkul}"
                    } else {
                        binding.tvAbsensiMatkul.text = absensi.matkulCode
                    }
                    
                    binding.tvAbsensiInfo.text = "Dibuka: ${absensi.jamBuka}"
                    
                    // Button untuk join absensi
                    binding.btnJoinAbsensi.setOnClickListener {
                        joinAbsensi(absensi.id)
                    }
                    
                } catch (e: Exception) {
                    Log.e("MahasiswaDashboard", "Error loading matkul info", e)
                }
            }
        }
    }
    
    private fun joinAbsensi(sessionId: String) {
        lifecycleScope.launch {
            showLoading(true)
            
            try {
                val success = mahasiswaRepository.joinAbsensi(sessionId, currentMahasiswaId, currentMahasiswaNama)
                
                if (success) {
                    Toast.makeText(this@MahasiswaDashboardActivity, 
                        "Berhasil absen!", Toast.LENGTH_SHORT).show()
                    
                    // Refresh dashboard
                    loadDashboardData()
                } else {
                    Toast.makeText(this@MahasiswaDashboardActivity, 
                        "Gagal absen", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("MahasiswaDashboard", "Error joining absensi", e)
                Toast.makeText(this@MahasiswaDashboardActivity, 
                    "Gagal absen: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
} 