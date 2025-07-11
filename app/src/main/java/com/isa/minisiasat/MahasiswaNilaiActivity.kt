package com.isa.minisiasat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapters.NilaiMahasiswaAdapter
import com.isa.minisiasat.databinding.ActivityMahasiswaNilaiBinding
import com.isa.minisiasat.models.NilaiAkhir
import com.isa.minisiasat.repository.MahasiswaRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch

class MahasiswaNilaiActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMahasiswaNilaiBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var mahasiswaRepository: MahasiswaRepository
    private lateinit var nilaiAdapter: NilaiMahasiswaAdapter
    
    private var currentMahasiswaId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMahasiswaNilaiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        mahasiswaRepository = MahasiswaRepository()
        currentMahasiswaId = sessionManager.getCurrentUserId()
        
        setupUI()
        setupRecyclerView()
        loadNilaiData()
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        nilaiAdapter = NilaiMahasiswaAdapter()
        
        binding.rvNilai.apply {
            layoutManager = LinearLayoutManager(this@MahasiswaNilaiActivity)
            adapter = nilaiAdapter
        }
    }
    
    private fun loadNilaiData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                Log.d("MahasiswaNilai", "Loading nilai for mahasiswa: $currentMahasiswaId")
                
                val nilaiList = mahasiswaRepository.getNilaiByMahasiswa(currentMahasiswaId)
                Log.d("MahasiswaNilai", "Loaded ${nilaiList.size} nilai")
                
                if (nilaiList.isEmpty()) {
                    showEmptyState()
                } else {
                    showNilaiList(nilaiList)
                }
                
                // Hitung statistik
                calculateStatistics(nilaiList)
                
            } catch (e: Exception) {
                Log.e("MahasiswaNilai", "Error loading nilai", e)
                Toast.makeText(this@MahasiswaNilaiActivity, 
                    "Gagal memuat nilai: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showNilaiList(nilaiList: List<NilaiAkhir>) {
        binding.rvNilai.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
        nilaiAdapter.updateData(nilaiList)
        
        // Update counter
        binding.tvNilaiCount.text = "${nilaiList.size} Mata Kuliah"
    }
    
    private fun showEmptyState() {
        binding.rvNilai.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.tvNilaiCount.text = "0 Mata Kuliah"
    }
    
    private fun calculateStatistics(nilaiList: List<NilaiAkhir>) {
        if (nilaiList.isEmpty()) {
            binding.tvTotalSks.text = "0 SKS"
            binding.tvRataRata.text = "0.00"
            return
        }
        
        lifecycleScope.launch {
            try {
                // Get matkul info untuk SKS
                val matkulList = mahasiswaRepository.getMatkulByMahasiswa(currentMahasiswaId)
                
                var totalSks = 0
                var totalBobot = 0.0
                
                for (nilai in nilaiList) {
                    val matkul = matkulList.find { it.kodeMatkul == nilai.matkulCode }
                    val sks = matkul?.sks ?: 0
                    val bobot = getNilaiBobot(nilai.nilaiAkhir)
                    
                    totalSks += sks
                    totalBobot += bobot * sks
                }
                
                val ipk = if (totalSks > 0) totalBobot / totalSks else 0.0
                
                binding.tvTotalSks.text = "$totalSks SKS"
                binding.tvRataRata.text = String.format("%.2f", ipk)
                
            } catch (e: Exception) {
                Log.e("MahasiswaNilai", "Error calculating statistics", e)
                binding.tvTotalSks.text = "0 SKS"
                binding.tvRataRata.text = "0.00"
            }
        }
    }
    
    private fun getNilaiBobot(nilai: String): Double {
        return when (nilai.uppercase()) {
            "A" -> 4.0
            "B" -> 3.0
            "C" -> 2.0
            "D" -> 1.0
            "E" -> 0.0
            else -> 0.0
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvNilai.visibility = if (show) View.GONE else View.VISIBLE
    }
} 