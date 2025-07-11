package com.isa.minisiasat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapters.MatkulDosenAdapter
import com.isa.minisiasat.databinding.ActivityDosenDashboardBinding
import com.isa.minisiasat.models.AbsensiSession
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.repository.DosenRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch

class DosenDashboardActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDosenDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var dosenRepository: DosenRepository
    
    private lateinit var matkulAdapter: MatkulDosenAdapter
    private var currentDosenId: String = ""
    private var currentDosenNama: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        dosenRepository = DosenRepository()
        
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
        currentDosenId = sessionManager.getCurrentUserId()
        currentDosenNama = sessionManager.getCurrentUserName()
        
        Log.d("DosenDashboard", "Current Dosen ID: $currentDosenId")
        Log.d("DosenDashboard", "Current Dosen Name: $currentDosenNama")
        
        binding.tvWelcome.text = "Selamat datang, $currentDosenNama"
    }
    
    private fun setupRecyclerView() {
        matkulAdapter = MatkulDosenAdapter { matkul ->
            bukaAbsensi(matkul)
        }
        
        binding.rvMatkulHariIni.apply {
            layoutManager = LinearLayoutManager(this@DosenDashboardActivity)
            adapter = matkulAdapter
        }
    }
    
    private fun setupClickListeners() {
        // Menu Card Clicks
        binding.cardMataKuliah.setOnClickListener {
            val intent = Intent(this, DosenMatkulActivity::class.java)
            startActivity(intent)
        }
        
        binding.cardAbsensi.setOnClickListener {
            val intent = Intent(this, DosenAbsensiActivity::class.java)
            startActivity(intent)
        }
        
        binding.cardNilai.setOnClickListener {
            val intent = Intent(this, DosenNilaiActivity::class.java)
            startActivity(intent)
        }
        
        binding.cardProfile.setOnClickListener {
            val intent = Intent(this, DosenProfileActivity::class.java)
            startActivity(intent)
        }
        
        // Debug - Test Firestore Connection
        binding.cardProfile.setOnLongClickListener {
            testFirestoreConnection()
            true
        }
        
        // Monitor Absensi
        binding.btnMonitorAbsensi.setOnClickListener {
            val intent = Intent(this, DosenAbsensiActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun loadDashboardData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                Log.d("DosenDashboard", "Starting to load dashboard data...")
                
                // Load mata kuliah hari ini
                val matkulHariIni = dosenRepository.getMatkulHariIni(currentDosenId)
                Log.d("DosenDashboard", "Loaded ${matkulHariIni.size} matkul hari ini")
                updateMatkulHariIni(matkulHariIni)
                
                // Load absensi aktif
                val absensiAktif = dosenRepository.getAbsensiAktif(currentDosenId)
                Log.d("DosenDashboard", "Loaded ${absensiAktif.size} absensi aktif")
                updateAbsensiAktif(absensiAktif)
                
                // Create dummy enrollment untuk testing
                dosenRepository.createDummyEnrollment()
                
            } catch (e: Exception) {
                Log.e("DosenDashboard", "Error loading dashboard data", e)
                Toast.makeText(this@DosenDashboardActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
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
                    val allMatkul = dosenRepository.getMatkulByDosen(currentDosenId)
                    val matkul = allMatkul.find { it.kodeMatkul == absensi.matkulCode }
                    
                    if (matkul != null) {
                        binding.tvAbsensiMatkul.text = "${matkul.kodeMatkul} - ${matkul.namaMatkul}"
                    } else {
                        binding.tvAbsensiMatkul.text = absensi.matkulCode
                    }
                    
                    val jumlahHadir = absensi.mahasiswaHadir.size
                    binding.tvAbsensiInfo.text = "Dibuka ${absensi.jamBuka} • $jumlahHadir mahasiswa hadir"
                    
                } catch (e: Exception) {
                    Log.e("DosenDashboard", "Error loading matkul info", e)
                }
            }
        }
    }
    
    private fun bukaAbsensi(matkul: Matkul) {
        lifecycleScope.launch {
            showLoading(true)
            
            try {
                val sessionId = dosenRepository.bukaAbsensi(matkul.kodeMatkul, currentDosenId)
                
                if (sessionId != null) {
                    Toast.makeText(this@DosenDashboardActivity, 
                        "Absensi ${matkul.kodeMatkul} berhasil dibuka", Toast.LENGTH_SHORT).show()
                    
                    // Refresh dashboard
                    loadDashboardData()
                    
                    // Buka activity monitoring
                    val intent = Intent(this@DosenDashboardActivity, MonitorAbsensiActivity::class.java)
                    intent.putExtra("SESSION_ID", sessionId)
                    intent.putExtra("MATKUL_CODE", matkul.kodeMatkul)
                    intent.putExtra("MATKUL_NAME", matkul.namaMatkul)
                    startActivity(intent)
                    
                } else {
                    Toast.makeText(this@DosenDashboardActivity, 
                        "Gagal membuka absensi", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("DosenDashboard", "Error membuka absensi", e)
                Toast.makeText(this@DosenDashboardActivity, 
                    "Gagal membuka absensi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun testFirestoreConnection() {
        lifecycleScope.launch {
            showLoading(true)
            
            try {
                val result = dosenRepository.debugFirestoreConnection()
                Toast.makeText(this@DosenDashboardActivity, result, Toast.LENGTH_LONG).show()
                Log.d("DosenDashboard", "Debug result: $result")
                
            } catch (e: Exception) {
                Log.e("DosenDashboard", "Debug error", e)
                Toast.makeText(this@DosenDashboardActivity, "Debug error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }
} 