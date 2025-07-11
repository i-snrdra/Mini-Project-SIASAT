package com.isa.minisiasat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapter.MatkulViewAdapter
import com.isa.minisiasat.databinding.ActivityLihatMatkulBinding
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.repository.MatkulRepository
import kotlinx.coroutines.launch

class LihatMatkulActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLihatMatkulBinding
    private val matkulRepository = MatkulRepository()
    
    private lateinit var matkulAdapter: MatkulViewAdapter
    
    private val editMatkulLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadData() // Refresh data setelah edit
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLihatMatkulBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        setupClickListeners()
        loadData()
    }
    
    private fun setupUI() {
        // Setup RecyclerView
        matkulAdapter = MatkulViewAdapter(
            onEditClick = { matkul ->
                editMatkul(matkul)
            },
            onDeleteClick = { matkul ->
                showDeleteConfirmation(matkul)
            }
        )
        
        binding.rvMatkul.apply {
            layoutManager = LinearLayoutManager(this@LihatMatkulActivity)
            adapter = matkulAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnRefresh.setOnClickListener {
            loadData()
        }
    }
    
    private fun loadData() {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val matkulList = matkulRepository.getAllMatkul()
                
                matkulAdapter.updateList(matkulList)
                showLoading(false)
                
                if (matkulList.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.rvMatkul.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rvMatkul.visibility = View.VISIBLE
                }
                
            } catch (e: Exception) {
                showLoading(false)
                showToast("Error loading data: ${e.message}")
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun editMatkul(matkul: Matkul) {
        val intent = Intent(this, EditMatkulActivity::class.java)
        intent.putExtra(EditMatkulActivity.EXTRA_MATKUL_CODE, matkul.kodeMatkul)
        editMatkulLauncher.launch(intent)
    }
    
    private fun showDeleteConfirmation(matkul: Matkul) {
        val message = if (matkul.dosenId != null) {
            "Mata kuliah ${matkul.kodeMatkul} - ${matkul.namaMatkul} sedang diajar oleh ${matkul.dosenNama}.\n\nYakin ingin menghapus mata kuliah ini?"
        } else {
            "Yakin ingin menghapus mata kuliah ${matkul.kodeMatkul} - ${matkul.namaMatkul}?"
        }
        
        AlertDialog.Builder(this)
            .setTitle("Hapus Mata Kuliah")
            .setMessage(message)
            .setPositiveButton("Ya, Hapus") { _, _ ->
                deleteMatkul(matkul)
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun deleteMatkul(matkul: Matkul) {
        showLoading(true)
        
        lifecycleScope.launch {
            try {
                val success = matkulRepository.deleteMatkul(matkul.kodeMatkul)
                
                if (success) {
                    showToast("Mata kuliah ${matkul.kodeMatkul} berhasil dihapus")
                    loadData() // Refresh data
                } else {
                    showToast("Gagal menghapus mata kuliah")
                    showLoading(false)
                }
                
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
                showLoading(false)
            }
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 