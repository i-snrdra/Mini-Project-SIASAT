package com.isa.minisiasat

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapter.MatkulAssignAdapter
import com.isa.minisiasat.databinding.ActivityAssignDosenBinding
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.repository.MatkulRepository
import com.isa.minisiasat.utils.User
import kotlinx.coroutines.launch

class AssignDosenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAssignDosenBinding
    private val matkulRepository = MatkulRepository()
    
    private lateinit var matkulAdapter: MatkulAssignAdapter
    private var allMatkul = mutableListOf<Matkul>()
    private var allDosen = listOf<User>()
    
    private val filterOptions = arrayOf("Semua Mata Kuliah", "Belum Ada Dosen", "Sudah Ada Dosen")
    private var currentFilter = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignDosenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupClickListeners()
        loadData()
    }
    
    private fun setupUI() {
        // Setup filter dropdown
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, filterOptions)
        binding.spinnerFilter.setAdapter(filterAdapter)
        binding.spinnerFilter.setText(filterOptions[0], false)
        
        // Setup RecyclerView
        matkulAdapter = MatkulAssignAdapter(
            onAssignClick = { matkul ->
                showDosenSelectionDialog(matkul)
            },
            onUnassignClick = { matkul ->
                showUnassignConfirmation(matkul)
            }
        )
        
        binding.rvMatkul.apply {
            layoutManager = LinearLayoutManager(this@AssignDosenActivity)
            adapter = matkulAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnRefresh.setOnClickListener {
            loadData()
        }
        
        binding.spinnerFilter.setOnItemClickListener { _, _, position, _ ->
            currentFilter = position
            filterMatkul()
        }
    }
    
    private fun loadData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                // Load mata kuliah dan dosen secara parallel
                val matkulList = matkulRepository.getAllMatkul()
                val dosenList = matkulRepository.getAllDosen()
                
                allMatkul.clear()
                allMatkul.addAll(matkulList)
                allDosen = dosenList
                
                filterMatkul()
                showLoading(false)
                
            } catch (e: Exception) {
                showLoading(false)
                showToast("Error loading data: ${e.message}")
            }
        }
    }
    
    private fun filterMatkul() {
        val filteredList = when (currentFilter) {
            0 -> allMatkul // Semua
            1 -> allMatkul.filter { it.dosenId == null } // Belum ada dosen
            2 -> allMatkul.filter { it.dosenId != null } // Sudah ada dosen
            else -> allMatkul
        }
        
        matkulAdapter.updateList(filteredList)
        
        if (filteredList.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvMatkul.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvMatkul.visibility = View.VISIBLE
        }
    }
    
    private fun showDosenSelectionDialog(matkul: Matkul) {
        if (allDosen.isEmpty()) {
            showToast("Tidak ada dosen tersedia")
            return
        }
        
        val dosenNames = allDosen.map { "${it.nama} (${it.id})" }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("Pilih Dosen untuk ${matkul.kodeMatkul}")
            .setItems(dosenNames) { _, which ->
                val selectedDosen = allDosen[which]
                assignDosenToMatkul(matkul, selectedDosen)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun assignDosenToMatkul(matkul: Matkul, dosen: User) {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val success = matkulRepository.assignDosenToMatkul(
                    kodeMatkul = matkul.kodeMatkul,
                    dosenId = dosen.id,
                    dosenNama = dosen.nama
                )
                
                if (success) {
                    showToast("Berhasil assign dosen ${dosen.nama}")
                    loadData() // Refresh data
                } else {
                    showToast("Gagal assign dosen")
                    showLoading(false)
                }
                
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
                showLoading(false)
            }
        }
    }
    
    private fun showUnassignConfirmation(matkul: Matkul) {
        AlertDialog.Builder(this)
            .setTitle("Lepas Dosen")
            .setMessage("Yakin ingin melepas dosen ${matkul.dosenNama} dari mata kuliah ${matkul.kodeMatkul}?")
            .setPositiveButton("Ya") { _, _ ->
                unassignDosenFromMatkul(matkul)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun unassignDosenFromMatkul(matkul: Matkul) {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val success = matkulRepository.unassignDosenFromMatkul(matkul.kodeMatkul)
                
                if (success) {
                    showToast("Berhasil lepas dosen dari ${matkul.kodeMatkul}")
                    loadData() // Refresh data
                } else {
                    showToast("Gagal lepas dosen")
                    showLoading(false)
                }
                
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
                showLoading(false)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 