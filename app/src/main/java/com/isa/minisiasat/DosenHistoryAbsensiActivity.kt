package com.isa.minisiasat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapters.HistoryAbsensiAdapter
import com.isa.minisiasat.databinding.ActivityDosenHistoryAbsensiBinding
import com.isa.minisiasat.models.AbsensiSession
import com.isa.minisiasat.repository.DosenRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch

class DosenHistoryAbsensiActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDosenHistoryAbsensiBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var dosenRepository: DosenRepository
    private lateinit var historyAdapter: HistoryAbsensiAdapter
    
    private var currentDosenId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenHistoryAbsensiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        dosenRepository = DosenRepository()
        currentDosenId = sessionManager.getCurrentUserId()
        
        setupUI()
        setupRecyclerView()
        loadHistoryData()
    }
    
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        historyAdapter = HistoryAbsensiAdapter { session ->
            showSessionDetail(session)
        }
        
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(this@DosenHistoryAbsensiActivity)
            adapter = historyAdapter
        }
    }
    
    private fun loadHistoryData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                Log.d("HistoryAbsensi", "Loading history for dosen: $currentDosenId")
                
                val historyList = dosenRepository.getHistoryAbsensi(currentDosenId)
                Log.d("HistoryAbsensi", "Loaded ${historyList.size} history items")
                
                if (historyList.isEmpty()) {
                    showEmptyState()
                } else {
                    showHistoryList(historyList)
                }
                
            } catch (e: Exception) {
                Log.e("HistoryAbsensi", "Error loading history", e)
                Toast.makeText(this@DosenHistoryAbsensiActivity, 
                    "Gagal memuat riwayat absensi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showHistoryList(historyList: List<AbsensiSession>) {
        binding.rvHistory.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
        historyAdapter.updateData(historyList)
    }
    
    private fun showEmptyState() {
        binding.rvHistory.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
    }
    
    private fun showSessionDetail(session: AbsensiSession) {
        lifecycleScope.launch {
            try {
                // Ambil detail mata kuliah
                val allMatkul = dosenRepository.getMatkulByDosen(currentDosenId)
                val matkul = allMatkul.find { it.kodeMatkul == session.matkulCode }
                
                val matkulName = matkul?.namaMatkul ?: session.matkulCode
                val jumlahHadir = session.mahasiswaHadir.size
                val duration = if (session.jamTutup != null) {
                    val jamBuka = session.jamBuka.split(":").map { it.toInt() }
                    val jamTutup = session.jamTutup.split(":").map { it.toInt() }
                    val durasi = (jamTutup[0] * 60 + jamTutup[1]) - (jamBuka[0] * 60 + jamBuka[1])
                    "${durasi / 60} jam ${durasi % 60} menit"
                } else {
                    "Masih aktif"
                }
                
                val detailMessage = """
                    Detail Sesi Absensi
                    
                    Mata Kuliah: ${session.matkulCode} - $matkulName
                    Tanggal: ${session.tanggal}
                    Jam Buka: ${session.jamBuka}
                    Jam Tutup: ${session.jamTutup ?: "Belum ditutup"}
                    Durasi: $duration
                    
                    Mahasiswa Hadir: $jumlahHadir orang
                    ${if (session.mahasiswaHadir.isNotEmpty()) {
                        "\nDaftar Hadir:\n" + session.mahasiswaHadir.joinToString("\n") { "• $it" }
                    } else {
                        "\nBelum ada mahasiswa yang absen"
                    }}
                """.trimIndent()
                
                androidx.appcompat.app.AlertDialog.Builder(this@DosenHistoryAbsensiActivity)
                    .setTitle("Detail Absensi")
                    .setMessage(detailMessage)
                    .setPositiveButton("OK", null)
                    .show()
                
            } catch (e: Exception) {
                Log.e("HistoryAbsensi", "Error showing detail", e)
                Toast.makeText(this@DosenHistoryAbsensiActivity, 
                    "Gagal menampilkan detail", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvHistory.visibility = if (show) View.GONE else View.VISIBLE
    }
} 