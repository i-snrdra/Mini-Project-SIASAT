package com.isa.minisiasat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapters.MatkulDetailAdapter
import com.isa.minisiasat.databinding.ActivityDosenMatkulBinding
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.repository.DosenRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch

class DosenMatkulActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDosenMatkulBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var dosenRepository: DosenRepository
    private lateinit var matkulAdapter: MatkulDetailAdapter
    
    private var currentDosenId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenMatkulBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        dosenRepository = DosenRepository()
        currentDosenId = sessionManager.getCurrentUserId()
        
        setupUI()
        setupRecyclerView()
        loadMatkulData()
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        matkulAdapter = MatkulDetailAdapter { matkul ->
            showMatkulDetail(matkul)
        }
        
        binding.rvMatkul.apply {
            layoutManager = LinearLayoutManager(this@DosenMatkulActivity)
            adapter = matkulAdapter
        }
    }
    
    private fun loadMatkulData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                Log.d("DosenMatkul", "Loading matkul for dosen: $currentDosenId")
                
                val matkulList = dosenRepository.getMatkulByDosen(currentDosenId)
                Log.d("DosenMatkul", "Loaded ${matkulList.size} matkul")
                
                if (matkulList.isEmpty()) {
                    showEmptyState()
                } else {
                    showMatkulList(matkulList)
                }
                
            } catch (e: Exception) {
                Log.e("DosenMatkul", "Error loading matkul", e)
                Toast.makeText(this@DosenMatkulActivity, 
                    "Gagal memuat mata kuliah: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showMatkulList(matkulList: List<Matkul>) {
        binding.rvMatkul.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
        matkulAdapter.updateData(matkulList)
        
        // Update counter
        binding.tvMatkulCount.text = "${matkulList.size} Mata Kuliah"
    }
    
    private fun showEmptyState() {
        binding.rvMatkul.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.tvMatkulCount.text = "0 Mata Kuliah"
    }
    
    private fun showMatkulDetail(matkul: Matkul) {
        lifecycleScope.launch {
            try {
                // Get additional info
                val mahasiswaCount = dosenRepository.getMahasiswaByMatkul(matkul.kodeMatkul).size
                val historyList = dosenRepository.getHistoryAbsensi(currentDosenId)
                val sessionCount = historyList.filter { it.matkulCode == matkul.kodeMatkul }.size
                
                val detailMessage = """
                    Detail Mata Kuliah
                    
                    Kode: ${matkul.kodeMatkul}
                    Nama: ${matkul.namaMatkul}
                    SKS: ${matkul.sks}
                    Semester: ${matkul.semester}
                    
                    Jadwal:
                    ${matkul.hari}, ${matkul.jamMulai} - ${matkul.jamSelesai}
                    Ruang: ${matkul.ruang}
                    
                    Statistik:
                    • $mahasiswaCount mahasiswa terdaftar
                    • $sessionCount sesi absensi telah dilakukan
                """.trimIndent()
                
                androidx.appcompat.app.AlertDialog.Builder(this@DosenMatkulActivity)
                    .setTitle("Detail Mata Kuliah")
                    .setMessage(detailMessage)
                    .setPositiveButton("OK", null)
                    .show()
                
            } catch (e: Exception) {
                Log.e("DosenMatkul", "Error showing detail", e)
                Toast.makeText(this@DosenMatkulActivity, 
                    "Gagal menampilkan detail", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvMatkul.visibility = if (show) View.GONE else View.VISIBLE
    }
} 