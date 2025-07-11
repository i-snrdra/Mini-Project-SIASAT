package com.isa.minisiasat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapters.MatkulDetailMahasiswaAdapter
import com.isa.minisiasat.databinding.ActivityMahasiswaMatkulBinding
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.repository.MahasiswaRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch

class MahasiswaMatkulActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMahasiswaMatkulBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var mahasiswaRepository: MahasiswaRepository
    private lateinit var matkulAdapter: MatkulDetailMahasiswaAdapter
    
    private var currentMahasiswaId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMahasiswaMatkulBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        mahasiswaRepository = MahasiswaRepository()
        currentMahasiswaId = sessionManager.getCurrentUserId()
        
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
        matkulAdapter = MatkulDetailMahasiswaAdapter { matkul ->
            showMatkulDetail(matkul)
        }
        
        binding.rvMatkul.apply {
            layoutManager = LinearLayoutManager(this@MahasiswaMatkulActivity)
            adapter = matkulAdapter
        }
    }
    
    private fun loadMatkulData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                Log.d("MahasiswaMatkul", "Loading matkul for mahasiswa: $currentMahasiswaId")
                
                val matkulList = mahasiswaRepository.getMatkulByMahasiswa(currentMahasiswaId)
                Log.d("MahasiswaMatkul", "Loaded ${matkulList.size} matkul")
                
                if (matkulList.isEmpty()) {
                    showEmptyState()
                } else {
                    showMatkulList(matkulList)
                }
                
            } catch (e: Exception) {
                Log.e("MahasiswaMatkul", "Error loading matkul", e)
                Toast.makeText(this@MahasiswaMatkulActivity, 
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
                """.trimIndent()
                
                androidx.appcompat.app.AlertDialog.Builder(this@MahasiswaMatkulActivity)
                    .setTitle("Detail Mata Kuliah")
                    .setMessage(detailMessage)
                    .setPositiveButton("OK", null)
                    .show()
                
            } catch (e: Exception) {
                Log.e("MahasiswaMatkul", "Error showing detail", e)
                Toast.makeText(this@MahasiswaMatkulActivity, 
                    "Gagal menampilkan detail", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvMatkul.visibility = if (show) View.GONE else View.VISIBLE
    }
} 