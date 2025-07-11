package com.isa.minisiasat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapters.JadwalMahasiswaAdapter
import com.isa.minisiasat.databinding.ActivityMahasiswaJadwalBinding
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.repository.MahasiswaRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch

class MahasiswaJadwalActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMahasiswaJadwalBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var mahasiswaRepository: MahasiswaRepository
    private lateinit var jadwalAdapter: JadwalMahasiswaAdapter
    
    private var currentMahasiswaId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMahasiswaJadwalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        mahasiswaRepository = MahasiswaRepository()
        currentMahasiswaId = sessionManager.getCurrentUserId()
        
        setupUI()
        setupRecyclerView()
        loadJadwalData()
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        jadwalAdapter = JadwalMahasiswaAdapter()
        
        binding.rvJadwal.apply {
            layoutManager = LinearLayoutManager(this@MahasiswaJadwalActivity)
            adapter = jadwalAdapter
        }
    }
    
    private fun loadJadwalData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                Log.d("MahasiswaJadwal", "Loading jadwal for mahasiswa: $currentMahasiswaId")
                
                val jadwalList = mahasiswaRepository.getJadwalKuliah(currentMahasiswaId)
                Log.d("MahasiswaJadwal", "Loaded ${jadwalList.size} jadwal")
                
                if (jadwalList.isEmpty()) {
                    showEmptyState()
                } else {
                    showJadwalList(jadwalList)
                }
                
            } catch (e: Exception) {
                Log.e("MahasiswaJadwal", "Error loading jadwal", e)
                Toast.makeText(this@MahasiswaJadwalActivity, 
                    "Gagal memuat jadwal: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showJadwalList(jadwalList: List<Matkul>) {
        binding.rvJadwal.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
        
        // Group by hari untuk ditampilkan
        val groupedJadwal = jadwalList.groupBy { it.hari }
        jadwalAdapter.updateData(groupedJadwal)
        
        // Update info
        val totalJadwal = jadwalList.size
        val totalHari = groupedJadwal.size
        binding.tvJadwalInfo.text = "$totalJadwal jadwal kuliah dalam $totalHari hari"
    }
    
    private fun showEmptyState() {
        binding.rvJadwal.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.tvJadwalInfo.text = "0 jadwal kuliah"
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvJadwal.visibility = if (show) View.GONE else View.VISIBLE
    }
} 