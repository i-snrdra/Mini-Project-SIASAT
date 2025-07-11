package com.isa.minisiasat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.isa.minisiasat.adapters.MahasiswaNilaiAdapter
import com.isa.minisiasat.databinding.ActivityDosenNilaiBinding
import com.isa.minisiasat.models.Matkul
import com.isa.minisiasat.models.NilaiAkhir
import com.isa.minisiasat.repository.DosenRepository
import com.isa.minisiasat.utils.SessionManager
import com.isa.minisiasat.utils.User
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DosenNilaiActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDosenNilaiBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var dosenRepository: DosenRepository
    
    private lateinit var mahasiswaAdapter: MahasiswaNilaiAdapter
    
    private var matkulList = listOf<Matkul>()
    private var selectedMatkul: Matkul? = null
    private var mahasiswaList = listOf<User>()
    private var currentDosenId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDosenNilaiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sessionManager = SessionManager(this)
        dosenRepository = DosenRepository()
        currentDosenId = sessionManager.getCurrentUserId()
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        
        loadMatkulList()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupRecyclerView() {
        mahasiswaAdapter = MahasiswaNilaiAdapter()
        
        binding.rvMahasiswaNilai.apply {
            layoutManager = LinearLayoutManager(this@DosenNilaiActivity)
            adapter = mahasiswaAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnLoadMahasiswa.setOnClickListener {
            loadMahasiswa()
        }
        
        binding.btnSimpanNilai.setOnClickListener {
            simpanNilai()
        }
    }
    
    private fun loadMatkulList() {
        lifecycleScope.launch {
            showLoading(true)
            
            try {
                matkulList = dosenRepository.getMatkulByDosen(currentDosenId)
                setupMatkulSpinner()
                
            } catch (e: Exception) {
                Log.e("DosenNilai", "Error loading matkul list", e)
                Toast.makeText(this@DosenNilaiActivity, 
                    "Gagal memuat mata kuliah: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun setupMatkulSpinner() {
        val matkulNames = matkulList.map { "${it.kodeMatkul} - ${it.namaMatkul}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, matkulNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMataKuliah.adapter = adapter
    }
    
    private fun loadMahasiswa() {
        val selectedPosition = binding.spinnerMataKuliah.selectedItemPosition
        
        if (selectedPosition < 0 || selectedPosition >= matkulList.size) {
            Toast.makeText(this, "Pilih mata kuliah terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        
        selectedMatkul = matkulList[selectedPosition]
        
        lifecycleScope.launch {
            showLoading(true)
            
            try {
                mahasiswaList = dosenRepository.getMahasiswaByMatkul(selectedMatkul!!.kodeMatkul)
                
                if (mahasiswaList.isEmpty()) {
                    Toast.makeText(this@DosenNilaiActivity, 
                        "Tidak ada mahasiswa terdaftar di mata kuliah ini", Toast.LENGTH_SHORT).show()
                    hideAllContent()
                } else {
                    showContent()
                    updateUI()
                    loadExistingNilai()
                }
                
            } catch (e: Exception) {
                Log.e("DosenNilai", "Error loading mahasiswa", e)
                Toast.makeText(this@DosenNilaiActivity, 
                    "Gagal memuat mahasiswa: ${e.message}", Toast.LENGTH_SHORT).show()
                hideAllContent()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun loadExistingNilai() {
        lifecycleScope.launch {
            try {
                val existingNilai = dosenRepository.getNilaiByMatkul(selectedMatkul!!.kodeMatkul)
                mahasiswaAdapter.setExistingNilai(existingNilai)
                
            } catch (e: Exception) {
                Log.e("DosenNilai", "Error loading existing nilai", e)
            }
        }
    }
    
    private fun updateUI() {
        selectedMatkul?.let { matkul ->
            binding.tvNamaMatkulInfo.text = "${matkul.kodeMatkul} - ${matkul.namaMatkul}"
            binding.tvJumlahMahasiswa.text = "${mahasiswaList.size} Mahasiswa"
            
            mahasiswaAdapter.updateData(mahasiswaList)
        }
    }
    
    private fun showContent() {
        binding.cardInfoMatkul.visibility = View.VISIBLE
        binding.tvTitleMahasiswa.visibility = View.VISIBLE
        binding.btnSimpanNilai.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
    }
    
    private fun hideAllContent() {
        binding.cardInfoMatkul.visibility = View.GONE
        binding.tvTitleMahasiswa.visibility = View.GONE
        binding.btnSimpanNilai.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
    }
    
    private fun simpanNilai() {
        if (selectedMatkul == null) {
            Toast.makeText(this, "Pilih mata kuliah terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }
        
        val nilaiList = mahasiswaAdapter.getAllNilai().map { item ->
            NilaiAkhir(
                id = "${selectedMatkul!!.kodeMatkul}_${item.mahasiswa.id}",
                matkulCode = selectedMatkul!!.kodeMatkul,
                dosenId = currentDosenId,
                mahasiswaId = item.mahasiswa.id,
                mahasiswaNama = item.mahasiswa.nama,
                nilaiAkhir = item.nilai,
                tanggal = getCurrentDate()
            )
        }.filter { it.nilaiAkhir.isNotEmpty() } // Hanya simpan yang sudah ada nilainya
        
        if (nilaiList.isEmpty()) {
            Toast.makeText(this, "Belum ada nilai yang diinput", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            showLoading(true)
            
            try {
                val success = dosenRepository.simpanNilai(nilaiList)
                
                if (success) {
                    Toast.makeText(this@DosenNilaiActivity, 
                        "Nilai berhasil disimpan untuk ${nilaiList.size} mahasiswa", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@DosenNilaiActivity, 
                        "Gagal menyimpan nilai", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                Log.e("DosenNilai", "Error saving nilai", e)
                Toast.makeText(this@DosenNilaiActivity, 
                    "Gagal menyimpan nilai: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLoadMahasiswa.isEnabled = !show
        binding.btnSimpanNilai.isEnabled = !show
    }
    
    private fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }
} 