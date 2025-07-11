package com.isa.minisiasat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapters.EnrollmentAdapter
import com.isa.minisiasat.databinding.ActivityMahasiswaEnrollmentBinding
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.repository.MahasiswaRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch

class MahasiswaEnrollmentActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMahasiswaEnrollmentBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var mahasiswaRepository: MahasiswaRepository
    private lateinit var enrollmentAdapter: EnrollmentAdapter
    
    private var currentMahasiswaId: String = ""
    private var availableMatkul = listOf<Matkul>()
    private var enrollmentStatus = mapOf<String, Boolean>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMahasiswaEnrollmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        mahasiswaRepository = MahasiswaRepository()
        currentMahasiswaId = sessionManager.getCurrentUserId()
        
        setupUI()
        setupRecyclerView()
        loadEnrollmentData()
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        
        binding.swipeRefresh.setOnRefreshListener {
            loadEnrollmentData()
        }
    }
    
    private fun setupRecyclerView() {
        enrollmentAdapter = EnrollmentAdapter(
            onEnrollClick = { matkul -> enrollMatkul(matkul) },
            onDropClick = { matkul -> dropMatkul(matkul) },
            onDetailClick = { matkul -> showMatkulDetail(matkul) }
        )
        
        binding.rvMatkul.apply {
            layoutManager = LinearLayoutManager(this@MahasiswaEnrollmentActivity)
            adapter = enrollmentAdapter
        }
    }
    
    private fun loadEnrollmentData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                Log.d("MahasiswaEnrollment", "Loading enrollment data for: $currentMahasiswaId")
                
                // Load available mata kuliah
                availableMatkul = mahasiswaRepository.getAvailableMatkul()
                Log.d("MahasiswaEnrollment", "Loaded ${availableMatkul.size} available matkul")
                
                // Load enrollment status
                enrollmentStatus = mahasiswaRepository.getEnrollmentStatus(currentMahasiswaId)
                Log.d("MahasiswaEnrollment", "Loaded enrollment status for ${enrollmentStatus.size} matkul")
                
                // Update UI
                updateEnrollmentList()
                
            } catch (e: Exception) {
                Log.e("MahasiswaEnrollment", "Error loading enrollment data", e)
                Toast.makeText(this@MahasiswaEnrollmentActivity, 
                    "Gagal memuat data enrollment: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
    
    private fun updateEnrollmentList() {
        if (availableMatkul.isEmpty()) {
            showEmptyState()
        } else {
            showEnrollmentList()
        }
        
        // Update statistics
        val totalMatkul = availableMatkul.size
        val enrolledCount = enrollmentStatus.values.count { it }
        val availableCount = totalMatkul - enrolledCount
        
        binding.tvTotalMatkul.text = "$totalMatkul mata kuliah tersedia"
        binding.tvEnrolledCount.text = "$enrolledCount diambil"
        binding.tvAvailableCount.text = "$availableCount tersedia"
    }
    
    private fun showEnrollmentList() {
        binding.rvMatkul.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
        enrollmentAdapter.updateData(availableMatkul, enrollmentStatus)
    }
    
    private fun showEmptyState() {
        binding.rvMatkul.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
    }
    
    private fun enrollMatkul(matkul: Matkul) {
        lifecycleScope.launch {
            showLoading(true)
            
            try {
                val success = mahasiswaRepository.enrollMatkul(currentMahasiswaId, matkul.kodeMatkul)
                
                if (success) {
                    Toast.makeText(this@MahasiswaEnrollmentActivity, 
                        "Berhasil mengambil ${matkul.kodeMatkul}", Toast.LENGTH_SHORT).show()
                    
                    // Refresh data
                    loadEnrollmentData()
                } else {
                    Toast.makeText(this@MahasiswaEnrollmentActivity, 
                        "Anda sudah mengambil mata kuliah ini", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("MahasiswaEnrollment", "Error enrolling matkul", e)
                Toast.makeText(this@MahasiswaEnrollmentActivity, 
                    "Gagal mengambil mata kuliah: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun dropMatkul(matkul: Matkul) {
        // Konfirmasi drop
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Batalkan Mata Kuliah")
            .setMessage("Apakah Anda yakin ingin membatalkan mata kuliah ${matkul.kodeMatkul} - ${matkul.namaMatkul}?")
            .setPositiveButton("Ya") { _, _ ->
                performDropMatkul(matkul)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
    
    private fun performDropMatkul(matkul: Matkul) {
        lifecycleScope.launch {
            showLoading(true)
            
            try {
                val success = mahasiswaRepository.dropMatkul(currentMahasiswaId, matkul.kodeMatkul)
                
                if (success) {
                    Toast.makeText(this@MahasiswaEnrollmentActivity, 
                        "Berhasil membatalkan ${matkul.kodeMatkul}", Toast.LENGTH_SHORT).show()
                    
                    // Refresh data
                    loadEnrollmentData()
                } else {
                    Toast.makeText(this@MahasiswaEnrollmentActivity, 
                        "Gagal membatalkan mata kuliah", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("MahasiswaEnrollment", "Error dropping matkul", e)
                Toast.makeText(this@MahasiswaEnrollmentActivity, 
                    "Gagal membatalkan mata kuliah: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showMatkulDetail(matkul: Matkul) {
        val isEnrolled = enrollmentStatus[matkul.kodeMatkul] == true
        val statusText = if (isEnrolled) "Sudah Diambil" else "Belum Diambil"
        
        val detailMessage = """
            Detail Mata Kuliah
            
            Kode: ${matkul.kodeMatkul}
            Nama: ${matkul.namaMatkul}
            SKS: ${matkul.sks}
            Semester: ${matkul.semester}
            
            Jadwal:
            ${matkul.hari}, ${matkul.jamMulai} - ${matkul.jamSelesai}
            Ruang: ${matkul.ruang}
            
            Dosen Pengampu:
            ${matkul.dosenNama ?: "TBA"}
            
            Status: $statusText
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Detail Mata Kuliah")
            .setMessage(detailMessage)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
} 