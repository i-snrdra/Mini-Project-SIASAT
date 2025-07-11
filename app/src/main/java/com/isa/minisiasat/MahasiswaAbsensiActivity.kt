package com.isa.minisiasat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapters.AbsensiMahasiswaAdapter
import com.isa.minisiasat.databinding.ActivityMahasiswaAbsensiBinding
import com.isa.minisiasat.models.AbsensiSession
import com.isa.minisiasat.repository.MahasiswaRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch

class MahasiswaAbsensiActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMahasiswaAbsensiBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var mahasiswaRepository: MahasiswaRepository
    private lateinit var absensiAdapter: AbsensiMahasiswaAdapter
    
    private var currentMahasiswaId: String = ""
    private var currentMahasiswaNama: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMahasiswaAbsensiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        mahasiswaRepository = MahasiswaRepository()
        currentMahasiswaId = sessionManager.getCurrentUserId()
        currentMahasiswaNama = sessionManager.getCurrentUserName()
        
        setupUI()
        setupRecyclerView()
        loadAbsensiData()
    }
    
    override fun onResume() {
        super.onResume()
        loadAbsensiData() // Refresh saat kembali ke activity
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        
        binding.swipeRefresh.setOnRefreshListener {
            loadAbsensiData()
        }
    }
    
    private fun setupRecyclerView() {
        absensiAdapter = AbsensiMahasiswaAdapter { session ->
            joinAbsensi(session)
        }
        
        binding.rvAbsensi.apply {
            layoutManager = LinearLayoutManager(this@MahasiswaAbsensiActivity)
            adapter = absensiAdapter
        }
    }
    
    private fun loadAbsensiData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                Log.d("MahasiswaAbsensi", "Loading absensi for mahasiswa: $currentMahasiswaId")
                
                val absensiAktif = mahasiswaRepository.getAbsensiAktif(currentMahasiswaId)
                Log.d("MahasiswaAbsensi", "Loaded ${absensiAktif.size} active sessions")
                
                if (absensiAktif.isEmpty()) {
                    showEmptyState()
                } else {
                    showAbsensiList(absensiAktif)
                }
                
            } catch (e: Exception) {
                Log.e("MahasiswaAbsensi", "Error loading absensi", e)
                Toast.makeText(this@MahasiswaAbsensiActivity, 
                    "Gagal memuat data absensi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }
    
    private fun showAbsensiList(absensiList: List<AbsensiSession>) {
        binding.rvAbsensi.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
        
        // Get mata kuliah info untuk setiap session
        lifecycleScope.launch {
            try {
                val allMatkul = mahasiswaRepository.getMatkulByMahasiswa(currentMahasiswaId)
                absensiAdapter.updateData(absensiList, allMatkul, currentMahasiswaNama)
            } catch (e: Exception) {
                Log.e("MahasiswaAbsensi", "Error loading matkul info", e)
                absensiAdapter.updateData(absensiList, emptyList(), currentMahasiswaNama)
            }
        }
    }
    
    private fun showEmptyState() {
        binding.rvAbsensi.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
    }
    
    private fun joinAbsensi(session: AbsensiSession) {
        lifecycleScope.launch {
            showLoading(true)
            
            try {
                val success = mahasiswaRepository.joinAbsensi(session.id, currentMahasiswaId, currentMahasiswaNama)
                
                if (success) {
                    Toast.makeText(this@MahasiswaAbsensiActivity, 
                        "Berhasil absen untuk ${session.matkulCode}!", Toast.LENGTH_SHORT).show()
                    
                    // Refresh list
                    loadAbsensiData()
                } else {
                    Toast.makeText(this@MahasiswaAbsensiActivity, 
                        "Gagal absen", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("MahasiswaAbsensi", "Error joining absensi", e)
                Toast.makeText(this@MahasiswaAbsensiActivity, 
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