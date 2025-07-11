package com.isa.minisiasat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapters.MahasiswaHadirAdapter
import com.isa.minisiasat.databinding.ActivityMonitorAbsensiBinding
import com.isa.minisiasat.models.AbsensiSession
import com.isa.minisiasat.repository.DosenRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MonitorAbsensiActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMonitorAbsensiBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var dosenRepository: DosenRepository
    
    private lateinit var mahasiswaAdapter: MahasiswaHadirAdapter
    
    private var sessionId: String = ""
    private var matkulCode: String = ""
    private var matkulName: String = ""
    private var isRefreshing = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitorAbsensiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        dosenRepository = DosenRepository()
        
        getIntentData()
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        
        loadSessionData()
        startAutoRefresh()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRefreshing = false
    }
    
    private fun getIntentData() {
        sessionId = intent.getStringExtra("SESSION_ID") ?: ""
        matkulCode = intent.getStringExtra("MATKUL_CODE") ?: ""
        matkulName = intent.getStringExtra("MATKUL_NAME") ?: ""
        
        if (sessionId.isEmpty()) {
            Toast.makeText(this, "Session ID tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        mahasiswaAdapter = MahasiswaHadirAdapter()
        
        binding.rvMahasiswaHadir.apply {
            layoutManager = LinearLayoutManager(this@MonitorAbsensiActivity)
            adapter = mahasiswaAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnTutupAbsensi.setOnClickListener {
            showTutupAbsensiDialog()
        }
    }
    
    private fun loadSessionData() {
        lifecycleScope.launch {
            try {
                val session = dosenRepository.getAbsensiSession(sessionId)
                
                if (session != null) {
                    updateUI(session)
                } else {
                    Toast.makeText(this@MonitorAbsensiActivity, 
                        "Session tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                
            } catch (e: Exception) {
                Log.e("MonitorAbsensi", "Error loading session data", e)
                Toast.makeText(this@MonitorAbsensiActivity, 
                    "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateUI(session: AbsensiSession) {
        // Update mata kuliah info
        binding.tvMataKuliah.text = "$matkulCode - $matkulName"
        binding.tvWaktuBuka.text = session.jamBuka
        binding.tvTanggal.text = session.tanggal
        
        // Update statistik
        val jumlahHadir = session.mahasiswaHadir.size
        binding.tvJumlahHadir.text = jumlahHadir.toString()
        
        lifecycleScope.launch {
            try {
                val totalMahasiswa = dosenRepository.getMahasiswaByMatkul(matkulCode).size
                binding.tvJumlahTerdaftar.text = totalMahasiswa.toString()
            } catch (e: Exception) {
                binding.tvJumlahTerdaftar.text = "0"
            }
        }
        
        // Update daftar mahasiswa hadir
        if (session.mahasiswaHadir.isEmpty()) {
            binding.rvMahasiswaHadir.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvMahasiswaHadir.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            
            // Untuk demo, kita buat data mahasiswa hadir
            val mahasiswaHadirList = session.mahasiswaHadir.mapIndexed { index, mahasiswaId ->
                MahasiswaHadirItem(
                    id = mahasiswaId,
                    nama = when (mahasiswaId) {
                        "672022708" -> "Isa Noorendra"
                        "672022134" -> "Aghus Fajar M"
                        "672022076" -> "Ardiva Nugraheni"
                        else -> "Mahasiswa $index"
                    },
                    waktuAbsen = getCurrentTime()
                )
            }
            
            mahasiswaAdapter.updateData(mahasiswaHadirList)
        }
    }
    
    private fun startAutoRefresh() {
        lifecycleScope.launch {
            while (isRefreshing) {
                delay(5000) // Refresh setiap 5 detik
                
                try {
                    val session = dosenRepository.getAbsensiSession(sessionId)
                    if (session != null) {
                        if (session.status == "tertutup") {
                            // Session sudah ditutup, hentikan refresh
                            isRefreshing = false
                            showSessionClosedDialog()
                            return@launch
                        }
                        
                        updateUI(session)
                    }
                } catch (e: Exception) {
                    Log.e("MonitorAbsensi", "Error refreshing data", e)
                }
            }
        }
    }
    
    private fun showTutupAbsensiDialog() {
        AlertDialog.Builder(this)
            .setTitle("Tutup Absensi")
            .setMessage("Apakah Anda yakin ingin menutup absensi? Mahasiswa tidak akan bisa absen lagi setelah ini.")
            .setPositiveButton("Tutup") { _, _ ->
                tutupAbsensi()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun tutupAbsensi() {
        lifecycleScope.launch {
            showLoading(true)
            
            try {
                val success = dosenRepository.tutupAbsensi(sessionId)
                
                if (success) {
                    Toast.makeText(this@MonitorAbsensiActivity, 
                        "Absensi berhasil ditutup", Toast.LENGTH_SHORT).show()
                    
                    isRefreshing = false
                    finish()
                } else {
                    Toast.makeText(this@MonitorAbsensiActivity, 
                        "Gagal menutup absensi", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("MonitorAbsensi", "Error closing session", e)
                Toast.makeText(this@MonitorAbsensiActivity, 
                    "Gagal menutup absensi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showSessionClosedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Absensi Ditutup")
            .setMessage("Sesi absensi telah ditutup dari perangkat lain.")
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnTutupAbsensi.isEnabled = !show
    }
    
    private fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }
}

// Data class untuk mahasiswa hadir
data class MahasiswaHadirItem(
    val id: String,
    val nama: String,
    val waktuAbsen: String
) 