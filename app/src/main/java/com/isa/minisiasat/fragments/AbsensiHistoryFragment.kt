package com.isa.minisiasat.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapters.HistoryAbsensiAdapter
import com.isa.minisiasat.databinding.FragmentAbsensiHistoryBinding
import com.isa.minisiasat.models.AbsensiSession
import com.isa.minisiasat.repository.DosenRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch

class AbsensiHistoryFragment : Fragment() {
    
    private var _binding: FragmentAbsensiHistoryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sessionManager: SessionManager
    private lateinit var dosenRepository: DosenRepository
    private lateinit var historyAdapter: HistoryAbsensiAdapter
    
    private var currentDosenId: String = ""
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbsensiHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeComponents()
        setupRecyclerView()
        loadHistoryData()
    }
    
    private fun initializeComponents() {
        sessionManager = SessionManager(requireContext())
        dosenRepository = DosenRepository()
        currentDosenId = sessionManager.getCurrentUserId()
    }
    
    private fun setupRecyclerView() {
        historyAdapter = HistoryAbsensiAdapter { session ->
            showSessionDetail(session)
        }
        
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }
    
    private fun loadHistoryData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                Log.d("HistoryFragment", "Loading history for dosen: $currentDosenId")
                
                val historyList = dosenRepository.getHistoryAbsensi(currentDosenId)
                Log.d("HistoryFragment", "Loaded ${historyList.size} history items")
                
                if (historyList.isEmpty()) {
                    showEmptyState()
                } else {
                    showHistoryList(historyList)
                }
                
            } catch (e: Exception) {
                Log.e("HistoryFragment", "Error loading history", e)
                Toast.makeText(requireContext(), 
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
                
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Detail Absensi")
                    .setMessage(detailMessage)
                    .setPositiveButton("OK", null)
                    .show()
                
            } catch (e: Exception) {
                Log.e("HistoryFragment", "Error showing detail", e)
                Toast.makeText(requireContext(), 
                    "Gagal menampilkan detail", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.rvHistory.visibility = if (show) View.GONE else View.VISIBLE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 