package com.isa.minisiasat.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.MonitorAbsensiActivity
import com.isa.minisiasat.adapters.AbsensiAktifAdapter
import com.isa.minisiasat.adapters.MatkulAbsensiAdapter
import com.isa.minisiasat.databinding.FragmentAbsensiMatkulBinding
import com.isa.minisiasat.models.AbsensiSession
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.repository.DosenRepository
import com.isa.minisiasat.utils.SessionManager
import kotlinx.coroutines.launch

class AbsensiMatkulFragment : Fragment() {
    
    private var _binding: FragmentAbsensiMatkulBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sessionManager: SessionManager
    private lateinit var dosenRepository: DosenRepository
    
    private lateinit var absensiAktifAdapter: AbsensiAktifAdapter
    private lateinit var matkulAdapter: MatkulAbsensiAdapter
    
    private var currentDosenId: String = ""
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbsensiMatkulBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        dosenRepository = DosenRepository()
        currentDosenId = sessionManager.getCurrentUserId()
        
        setupRecyclerViews()
        loadData()
    }
    
    override fun onResume() {
        super.onResume()
        loadData() // Refresh data saat kembali ke fragment
    }
    
    private fun setupRecyclerViews() {
        // Adapter untuk absensi aktif
        absensiAktifAdapter = AbsensiAktifAdapter(
            onMonitorClick = { session -> monitorAbsensi(session) },
            onTutupClick = { session -> tutupAbsensi(session) }
        )
        
        binding.rvAbsensiAktif.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = absensiAktifAdapter
        }
        
        // Adapter untuk mata kuliah
        matkulAdapter = MatkulAbsensiAdapter { matkul ->
            bukaAbsensi(matkul)
        }
        
        binding.rvMataKuliah.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = matkulAdapter
        }
    }
    
    private fun loadData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                // Load absensi aktif
                val absensiAktif = dosenRepository.getAbsensiAktif(currentDosenId)
                updateAbsensiAktif(absensiAktif)
                
                // Load mata kuliah
                val matkulList = dosenRepository.getMatkulByDosen(currentDosenId)
                updateMataKuliah(matkulList)
                
            } catch (e: Exception) {
                Log.e("AbsensiMatkulFragment", "Error loading data", e)
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun updateAbsensiAktif(absensiList: List<AbsensiSession>) {
        if (absensiList.isEmpty()) {
            binding.layoutAbsensiAktif.visibility = View.GONE
        } else {
            binding.layoutAbsensiAktif.visibility = View.VISIBLE
            absensiAktifAdapter.updateData(absensiList)
        }
    }
    
    private fun updateMataKuliah(matkulList: List<Matkul>) {
        if (matkulList.isEmpty()) {
            binding.rvMataKuliah.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvMataKuliah.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            matkulAdapter.updateData(matkulList)
        }
    }
    
    private fun bukaAbsensi(matkul: Matkul) {
        lifecycleScope.launch {
            showLoading(true)
            
            try {
                val sessionId = dosenRepository.bukaAbsensi(matkul.kodeMatkul, currentDosenId)
                
                if (sessionId != null) {
                    Toast.makeText(requireContext(), 
                        "Absensi ${matkul.kodeMatkul} berhasil dibuka", Toast.LENGTH_SHORT).show()
                    
                    // Refresh data
                    loadData()
                    
                    // Buka activity monitoring
                    val intent = Intent(requireContext(), MonitorAbsensiActivity::class.java)
                    intent.putExtra("SESSION_ID", sessionId)
                    intent.putExtra("MATKUL_CODE", matkul.kodeMatkul)
                    intent.putExtra("MATKUL_NAME", matkul.namaMatkul)
                    startActivity(intent)
                    
                } else {
                    Toast.makeText(requireContext(), 
                        "Gagal membuka absensi", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("AbsensiMatkulFragment", "Error membuka absensi", e)
                Toast.makeText(requireContext(), 
                    "Gagal membuka absensi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun monitorAbsensi(session: AbsensiSession) {
        lifecycleScope.launch {
            try {
                // Get matkul info
                val matkulList = dosenRepository.getMatkulByDosen(currentDosenId)
                val matkul = matkulList.find { it.kodeMatkul == session.matkulCode }
                
                val intent = Intent(requireContext(), MonitorAbsensiActivity::class.java)
                intent.putExtra("SESSION_ID", session.id)
                intent.putExtra("MATKUL_CODE", session.matkulCode)
                intent.putExtra("MATKUL_NAME", matkul?.namaMatkul ?: session.matkulCode)
                startActivity(intent)
                
            } catch (e: Exception) {
                Log.e("AbsensiMatkulFragment", "Error monitoring absensi", e)
            }
        }
    }
    
    private fun tutupAbsensi(session: AbsensiSession) {
        lifecycleScope.launch {
            showLoading(true)
            
            try {
                val success = dosenRepository.tutupAbsensi(session.id)
                
                if (success) {
                    Toast.makeText(requireContext(), 
                        "Absensi ${session.matkulCode} berhasil ditutup", Toast.LENGTH_SHORT).show()
                    
                    // Refresh data
                    loadData()
                } else {
                    Toast.makeText(requireContext(), 
                        "Gagal menutup absensi", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("AbsensiMatkulFragment", "Error closing absensi", e)
                Toast.makeText(requireContext(), 
                    "Gagal menutup absensi: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 